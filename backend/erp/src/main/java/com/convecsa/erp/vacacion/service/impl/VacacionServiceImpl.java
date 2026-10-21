package com.convecsa.erp.vacacion.service.impl;

import com.convecsa.erp.asistencia.domain.CodigoTareo;
import com.convecsa.erp.asistencia.domain.EstadoAsistencia;
import com.convecsa.erp.asistencia.entity.AsistenciaRegistro;
import com.convecsa.erp.asistencia.repository.AsistenciaRegistroRepository;
import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.vacacion.domain.EstadoVacacion;
import com.convecsa.erp.vacacion.domain.TipoVacacion;
import com.convecsa.erp.vacacion.dto.VacacionDTO;
import com.convecsa.erp.vacacion.dto.VacacionRequest;
import com.convecsa.erp.vacacion.dto.VacacionResumenDTO;
import com.convecsa.erp.vacacion.dto.VacacionSaldoDTO;
import com.convecsa.erp.vacacion.entity.VacacionSolicitud;
import com.convecsa.erp.vacacion.repository.VacacionSolicitudRepository;
import com.convecsa.erp.vacacion.service.InterfazVacacionService;
import com.convecsa.erp.vacacion.support.CalendarioVacacionService;
import com.convecsa.erp.vacacion.support.VacacionDocumentoService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VacacionServiceImpl implements InterfazVacacionService {

    private static final List<EstadoVacacion> ESTADOS_QUE_BLOQUEAN =
            List.of(EstadoVacacion.PENDIENTE, EstadoVacacion.APROBADA);

    private final VacacionSolicitudRepository vacacionRepository;
    private final PersonaRepository personaRepository;
    private final ContratoRepository contratoRepository;
    private final AsistenciaRegistroRepository asistenciaRepository;
    private final CalendarioVacacionService calendarioVacacionService;
    private final VacacionDocumentoService documentoService;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VacacionDTO> listar(
            int page, int size, String q, TipoVacacion tipo, EstadoVacacion estado) {
        int safePage = Math.max(0, page);
        int safeSize = size < 1 ? 20 : Math.min(size, 100);
        boolean filtrarTexto = q != null && !q.isBlank();
        boolean filtrarTipo = tipo != null;
        boolean filtrarEstado = estado != null;
        String query = filtrarTexto ? q.trim() : "";
        TipoVacacion tipoConsulta = filtrarTipo ? tipo : TipoVacacion.ORDINARIA;
        EstadoVacacion estadoConsulta = filtrarEstado ? estado : EstadoVacacion.PENDIENTE;
        return PagedResponse.from(vacacionRepository.buscar(
                filtrarTexto, query,
                filtrarTipo, tipoConsulta,
                filtrarEstado, estadoConsulta,
                PageRequest.of(safePage, safeSize)).map(this::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public VacacionResumenDTO resumen() {
        LocalDate hoy = LocalDate.now();
        long aprobadas = vacacionRepository.countByEstado(EstadoVacacion.APROBADA);
        return new VacacionResumenDTO(
                vacacionRepository.count(),
                vacacionRepository.countByEstado(EstadoVacacion.PENDIENTE),
                aprobadas,
                vacacionRepository.countByEstadoAndFechaFinBefore(EstadoVacacion.APROBADA, hoy),
                vacacionRepository.countByTipo(TipoVacacion.ADELANTADA),
                vacacionRepository.sumDiasByEstado(EstadoVacacion.APROBADA));
    }

    @Override
    @Transactional(readOnly = true)
    public VacacionSaldoDTO saldo(
            Long personaId, LocalDate periodoInicio, LocalDate periodoFin, int diasDerecho) {
        validarPeriodo(periodoInicio, periodoFin, diasDerecho);
        if (!personaRepository.existsById(personaId)) {
            throw new IllegalArgumentException("No se encontró la persona");
        }
        int aprobados = Math.toIntExact(vacacionRepository.sumDiasPeriodo(
                personaId, periodoInicio, periodoFin, EstadoVacacion.APROBADA));
        int pendientes = Math.toIntExact(vacacionRepository.sumDiasPeriodo(
                personaId, periodoInicio, periodoFin, EstadoVacacion.PENDIENTE));
        return new VacacionSaldoDTO(personaId, periodoInicio, periodoFin, diasDerecho,
                aprobados, pendientes, Math.max(0, diasDerecho - aprobados));
    }

    @Override
    @Transactional
    public VacacionDTO crear(VacacionRequest request) {
        Persona persona = personaRepository.findByIdForUpdate(request.getPersonaId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la persona"));
        if (!persona.isActivo()) {
            throw new IllegalArgumentException("La persona está inactiva");
        }
        Contrato contrato = contratoRepository.findUltimoContratoActivoPorPersonaId(persona.getId())
                .orElseThrow(() -> new IllegalArgumentException("La persona no tiene un contrato activo"));
        validarPeriodo(request.getPeriodoInicio(), request.getPeriodoFin(), request.getDiasDerechoPeriodo());
        if (vacacionRepository.existeCrucePeriodo(persona.getId(), ESTADOS_QUE_BLOQUEAN,
                request.getPeriodoInicio(), request.getPeriodoFin())) {
            throw new IllegalArgumentException(
                    "El trabajador ya tiene una solicitud pendiente o aprobada en un período de derecho que se cruza con el indicado");
        }
        LocalDate fin = calendarioVacacionService.fechaFin(request.getFechaInicio(), request.getDiasSolicitados());
        validarVigenciaContrato(contrato, request.getFechaInicio(), fin);
        if (vacacionRepository.existeCruce(persona.getId(), ESTADOS_QUE_BLOQUEAN, request.getFechaInicio(), fin)) {
            throw new IllegalArgumentException("Ya existe una solicitud pendiente o aprobada que cruza esas fechas");
        }

        VacacionSolicitud solicitud = new VacacionSolicitud();
        solicitud.setPersona(persona);
        solicitud.setContrato(contrato);
        solicitud.setTipo(request.getTipo());
        solicitud.setEstado(EstadoVacacion.PENDIENTE);
        solicitud.setPeriodoInicio(request.getPeriodoInicio());
        solicitud.setPeriodoFin(request.getPeriodoFin());
        solicitud.setDiasDerechoPeriodo(request.getDiasDerechoPeriodo());
        solicitud.setDiasSolicitados(request.getDiasSolicitados());
        solicitud.setFechaInicio(request.getFechaInicio());
        solicitud.setFechaFin(fin);
        solicitud.setFechaRetorno(calendarioVacacionService.fechaRetorno(
                request.getFechaInicio(), request.getDiasSolicitados()));
        solicitud.setObservacion(trimToNull(request.getObservacion()));
        return toDto(vacacionRepository.save(solicitud));
    }

    @Override
    @Transactional
    public VacacionDTO aprobar(Long id) {
        VacacionSolicitud solicitud = cargar(id);
        if (solicitud.getEstado() != EstadoVacacion.PENDIENTE) {
            throw new IllegalArgumentException("Solo se puede aprobar una solicitud pendiente");
        }
        long usados = vacacionRepository.sumDiasPeriodo(
                solicitud.getPersona().getId(), solicitud.getPeriodoInicio(), solicitud.getPeriodoFin(),
                EstadoVacacion.APROBADA);
        if (usados + solicitud.getDiasSolicitados() > solicitud.getDiasDerechoPeriodo()) {
            throw new IllegalArgumentException("La aprobación supera los días disponibles del periodo");
        }
        solicitud.setEstado(EstadoVacacion.APROBADA);
        solicitud.setAprobadaEn(Instant.now());
        sincronizarTareo(solicitud);
        return toDto(vacacionRepository.save(solicitud));
    }

    @Override
    @Transactional
    public VacacionDTO rechazar(Long id) {
        VacacionSolicitud solicitud = cargar(id);
        if (solicitud.getEstado() != EstadoVacacion.PENDIENTE) {
            throw new IllegalArgumentException("Solo se puede rechazar una solicitud pendiente");
        }
        solicitud.setEstado(EstadoVacacion.RECHAZADA);
        solicitud.setRechazadaEn(Instant.now());
        return toDto(vacacionRepository.save(solicitud));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        VacacionSolicitud solicitud = cargar(id);
        if (solicitud.getEstado() == EstadoVacacion.APROBADA) {
            throw new IllegalArgumentException("Una solicitud aprobada no puede eliminarse porque ya afecta el tareo");
        }
        vacacionRepository.delete(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public String documento(Long id) {
        return documentoService.generar(cargar(id));
    }

    private void sincronizarTareo(VacacionSolicitud solicitud) {
        List<AsistenciaRegistro> registros = asistenciaRepository.findByPersonaIdAndFechaBetweenOrderByFechaAsc(
                solicitud.getPersona().getId(), solicitud.getFechaInicio(), solicitud.getFechaFin());
        for (AsistenciaRegistro registro : registros) {
            registro.setCodigo(CodigoTareo.VAC);
            registro.setEstado(EstadoAsistencia.PERMISO);
            registro.setHorasTrabajadas(BigDecimal.ZERO.setScale(2));
            registro.setMinutosExtras25(0);
            registro.setMinutosExtras35(0);
            registro.setMinutosTardanza(0);
            registro.setObservacion("Vacaciones aprobadas · solicitud #" + solicitud.getId());
        }
        asistenciaRepository.saveAll(registros);
    }

    private VacacionSolicitud cargar(Long id) {
        return vacacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la solicitud de vacaciones"));
    }

    private VacacionDTO toDto(VacacionSolicitud solicitud) {
        int usados = Math.toIntExact(vacacionRepository.sumDiasPeriodo(
                solicitud.getPersona().getId(), solicitud.getPeriodoInicio(), solicitud.getPeriodoFin(),
                EstadoVacacion.APROBADA));
        return new VacacionDTO(
                solicitud.getId(), solicitud.getPersona().getId(),
                solicitud.getPersona().getApellidos() + ", " + solicitud.getPersona().getNombres(),
                solicitud.getPersona().getNumeroDocumento(), solicitud.getContrato().metadata("cargo"),
                solicitud.getContrato().getId(), solicitud.getContrato().getFechaInicio(), solicitud.getTipo(),
                solicitud.getEstado(), solicitud.getEstado() == EstadoVacacion.APROBADA
                        && solicitud.getFechaFin().isBefore(LocalDate.now()),
                solicitud.getPeriodoInicio(), solicitud.getPeriodoFin(), solicitud.getDiasDerechoPeriodo(),
                solicitud.getDiasSolicitados(), usados, Math.max(0, solicitud.getDiasDerechoPeriodo() - usados),
                solicitud.getFechaInicio(), solicitud.getFechaFin(), solicitud.getFechaRetorno(),
                solicitud.getObservacion(), solicitud.getAprobadaEn(), solicitud.getCreatedAt());
    }

    private static void validarPeriodo(LocalDate inicio, LocalDate fin, int diasDerecho) {
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            throw new IllegalArgumentException("El periodo acumulativo es inválido");
        }
        if (diasDerecho < 1 || diasDerecho > 60) {
            throw new IllegalArgumentException("Los días de derecho deben estar entre 1 y 60");
        }
    }

    private static void validarVigenciaContrato(Contrato contrato, LocalDate inicio, LocalDate fin) {
        if (contrato.getFechaInicio() != null && inicio.isBefore(contrato.getFechaInicio())) {
            throw new IllegalArgumentException("Las vacaciones no pueden iniciar antes del contrato");
        }
        if (contrato.getFechaFin() != null && fin.isAfter(contrato.getFechaFin())) {
            throw new IllegalArgumentException("Las vacaciones no pueden terminar después del contrato");
        }
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
