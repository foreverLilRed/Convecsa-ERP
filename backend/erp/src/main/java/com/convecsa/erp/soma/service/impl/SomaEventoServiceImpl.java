package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.common.exception.DocumentoObraNoEncontradoException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.common.exception.SubcontratoObraNoEncontradoException;
import com.convecsa.erp.construccion.domain.AccionAuditoriaObra;
import com.convecsa.erp.construccion.domain.CodigoTareoCc;
import com.convecsa.erp.construccion.domain.EstadoPeriodoCc;
import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.SubcontratoObra;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.PartidaPresupuestoRepository;
import com.convecsa.erp.construccion.repository.PeriodoCcRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.SubcontratoObraRepository;
import com.convecsa.erp.construccion.repository.TareoCcDiaRepository;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.CausasBasicasEvento;
import com.convecsa.erp.soma.domain.CausasInmediatasEvento;
import com.convecsa.erp.soma.domain.EstadoHallazgoSoma;
import com.convecsa.erp.soma.domain.EstadoInvestigacionSst;
import com.convecsa.erp.soma.domain.TipoEventoSst;
import com.convecsa.erp.soma.dto.SomaEventoDTO;
import com.convecsa.erp.soma.dto.SomaEventoDmOmitidoDTO;
import com.convecsa.erp.soma.dto.SomaEventoRequest;
import com.convecsa.erp.soma.dto.SomaEventoSyncDmDTO;
import com.convecsa.erp.soma.dto.SomaHallazgoDTO;
import com.convecsa.erp.soma.dto.SomaHallazgoRequest;
import com.convecsa.erp.soma.entity.SomaEventoSst;
import com.convecsa.erp.soma.entity.SomaHallazgo;
import com.convecsa.erp.soma.repository.SomaEventoSstRepository;
import com.convecsa.erp.soma.repository.SomaHallazgoRepository;
import com.convecsa.erp.soma.service.InterfazSomaEventoService;
import com.convecsa.erp.soma.service.InterfazSomaInspeccionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaEventoServiceImpl implements InterfazSomaEventoService {

    private final SomaEventoSstRepository somaEventoSstRepository;
    private final SomaHallazgoRepository somaHallazgoRepository;
    private final ProyectoRepository proyectoRepository;
    private final PersonaRepository personaRepository;
    private final PartidaPresupuestoRepository partidaPresupuestoRepository;
    private final SubcontratoObraRepository subcontratoObraRepository;
    private final DocumentoObraRepository documentoObraRepository;
    private final PeriodoCcRepository periodoCcRepository;
    private final TareoCcDiaRepository tareoCcDiaRepository;
    private final InterfazSomaInspeccionService somaInspeccionService;
    private final AuditoriaObraRegistrar auditoriaObraRegistrar;

    @Override
    @Transactional(readOnly = true)
    public List<SomaEventoDTO> listar(Long proyectoId) {
        exigirProyecto(proyectoId);
        return somaEventoSstRepository.findByProyectoIdOrderByFechaHoraDescNumeroDesc(proyectoId).stream()
                .map(SomaEventoServiceImpl::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SomaEventoDTO crear(Long proyectoId, SomaEventoRequest request) {
        Proyecto proyecto = exigirProyecto(proyectoId);
        validarCabecera(request);
        SomaEventoSst evento = new SomaEventoSst();
        evento.setProyecto(proyecto);
        evento.setNumero(somaEventoSstRepository.findMaxNumero(proyectoId) + 1);
        evento.setEstadoInvestigacion(EstadoInvestigacionSst.EN_INVESTIGACION);
        aplicarCabecera(evento, proyectoId, request);
        SomaEventoSst guardado = somaEventoSstRepository.save(evento);
        if (guardado.getTipoEvento() != null && guardado.getTipoEvento().esAccidenteOEnfermedad()) {
            auditoriaObraRegistrar.registrar(
                    proyectoId,
                    AccionAuditoriaObra.REGISTRAR_ACCIDENTE_SST,
                    AuditoriaObraRegistrar.ENTIDAD_EVENTO_SOMA,
                    guardado.getId(),
                    "Evento N° " + guardado.getNumero() + " " + guardado.getTipoEvento());
        }
        return toDto(guardado);
    }

    @Override
    @Transactional
    public SomaEventoDTO actualizar(Long proyectoId, Long eventoId, SomaEventoRequest request) {
        exigirProyecto(proyectoId);
        validarCabecera(request);
        SomaEventoSst evento = exigirEvento(proyectoId, eventoId);
        if (evento.getEstadoInvestigacion() != EstadoInvestigacionSst.EN_INVESTIGACION) {
            throw new IllegalArgumentException("Solo se puede editar un evento en investigación");
        }
        aplicarCabecera(evento, proyectoId, request);
        return toDto(evento);
    }

    @Override
    @Transactional
    public SomaHallazgoDTO agregarHallazgo(Long proyectoId, Long eventoId, SomaHallazgoRequest request) {
        exigirProyecto(proyectoId);
        SomaEventoSst evento = exigirEvento(proyectoId, eventoId);
        if (evento.getEstadoInvestigacion() != EstadoInvestigacionSst.EN_INVESTIGACION) {
            throw new IllegalArgumentException("Solo se puede editar un evento en investigación");
        }
        SomaHallazgoRequest medida = request == null ? new SomaHallazgoRequest() : request;
        medida.setInspeccionId(null);
        SomaHallazgoDTO dto = somaInspeccionService.crearHallazgo(proyectoId, medida);
        SomaHallazgo hallazgo = somaHallazgoRepository
                .findByIdAndProyectoId(dto.getId(), proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el hallazgo"));
        evento.getHallazgos().add(hallazgo);
        return dto;
    }

    @Override
    @Transactional
    public SomaEventoDTO cerrar(Long proyectoId, Long eventoId) {
        exigirProyecto(proyectoId);
        SomaEventoSst evento = exigirEvento(proyectoId, eventoId);
        if (evento.getEstadoInvestigacion() != EstadoInvestigacionSst.EN_INVESTIGACION) {
            throw new IllegalArgumentException("El evento ya está cerrado");
        }
        List<SomaHallazgo> hallazgos = evento.getHallazgos() == null ? List.of() : evento.getHallazgos();
        boolean abiertos = hallazgos.stream()
                .anyMatch(fila -> fila.getEstado() != EstadoHallazgoSoma.LEVANTADO);
        if (abiertos) {
            throw new IllegalArgumentException("No se puede cerrar: hay hallazgos sin levantar");
        }
        evento.setEstadoInvestigacion(EstadoInvestigacionSst.CERRADO);
        return toDto(evento);
    }

    @Override
    @Transactional
    public SomaEventoSyncDmDTO sincronizarDm(Long proyectoId, Long eventoId) {
        exigirProyecto(proyectoId);
        SomaEventoSst evento = exigirEvento(proyectoId, eventoId);
        Persona persona = evento.getPersonaAfectada();
        if (persona == null || persona.getId() == null) {
            throw new IllegalArgumentException("El evento no tiene persona afectada");
        }
        LocalDate inicio = evento.getFechaInicioDm();
        LocalDate fin = evento.getFechaFinDm();
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Indique el rango de descanso médico");
        }
        List<PeriodoCc> periodos = periodoCcRepository.findByProyectoIdOrderByFechaDesdeDesc(proyectoId);
        List<LocalDate> aplicados = new ArrayList<>();
        List<SomaEventoDmOmitidoDTO> omitidos = new ArrayList<>();
        for (LocalDate dia = inicio; !dia.isAfter(fin); dia = dia.plusDays(1)) {
            PeriodoCc periodo = periodoQueCubre(periodos, dia);
            if (periodo == null) {
                omitidos.add(SomaEventoDmOmitidoDTO.builder().fecha(dia).motivo("sin periodo").build());
                continue;
            }
            if (periodo.getEstado() != EstadoPeriodoCc.BORRADOR) {
                omitidos.add(SomaEventoDmOmitidoDTO.builder().fecha(dia).motivo("periodo cerrado").build());
                continue;
            }
            upsertDm(periodo, persona, dia);
            aplicados.add(dia);
        }
        return SomaEventoSyncDmDTO.builder().aplicados(aplicados).omitidos(omitidos).build();
    }

    private void aplicarCabecera(SomaEventoSst evento, Long proyectoId, SomaEventoRequest request) {
        evento.setTipoEvento(request.getTipoEvento());
        evento.setGravedadIncapacidad(request.getGravedadIncapacidad());
        evento.setFechaHora(request.getFechaHora());
        evento.setLugarExacto(exigirTexto(request.getLugarExacto(), "El lugar exacto es obligatorio", 200));
        evento.setPartida(resolverPartida(proyectoId, request.getPartidaId()));
        evento.setPersonaAfectada(resolverPersonaOpcional(request.getPersonaAfectadaId()));
        evento.setSubcontrato(resolverSubcontrato(proyectoId, request.getSubcontratoId()));
        evento.setDescripcionEvento(
                exigirTexto(request.getDescripcionEvento(), "La descripción del evento es obligatoria", 2000));
        evento.setParteCuerpoLesionada(limitar(request.getParteCuerpoLesionada(), 120, "La parte del cuerpo"));
        evento.setNaturalezaLesion(limitar(request.getNaturalezaLesion(), 120, "La naturaleza de la lesión"));
        evento.setAgenteCausante(limitar(request.getAgenteCausante(), 160, "El agente causante"));
        evento.setNumeroCertificadoDm(limitar(request.getNumeroCertificadoDm(), 60, "El certificado de DM"));
        evento.setFechaInicioDm(request.getFechaInicioDm());
        evento.setFechaFinDm(request.getFechaFinDm());
        evento.setDiasDescansoMedico(calcularDiasDescanso(request.getFechaInicioDm(), request.getFechaFinDm()));
        int cargo = request.getDiasCargoEstadistico() == null ? 0 : request.getDiasCargoEstadistico();
        if (cargo < 0) {
            throw new IllegalArgumentException("Los días de cargo estadístico no pueden ser negativos");
        }
        evento.setDiasCargoEstadistico(cargo);
        evento.setCausasInmediatas(
                request.getCausasInmediatas() == null ? CausasInmediatasEvento.vacio() : request.getCausasInmediatas());
        evento.setCausasBasicas(
                request.getCausasBasicas() == null ? CausasBasicasEvento.vacio() : request.getCausasBasicas());
        evento.setNotificadoMtpe(Boolean.TRUE.equals(request.getNotificadoMtpe()));
        evento.setInforme(resolverDocumento(proyectoId, request.getInformeDocId()));
    }

    private void validarCabecera(SomaEventoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El evento es obligatorio");
        }
        if (request.getTipoEvento() == null) {
            throw new IllegalArgumentException("El tipo de evento es obligatorio");
        }
        if (request.getFechaHora() == null) {
            throw new IllegalArgumentException("La fecha y hora son obligatorias");
        }
        if (request.getTipoEvento() == TipoEventoSst.ACCIDENTE_INCAPACITANTE) {
            if (request.getGravedadIncapacidad() == null) {
                throw new IllegalArgumentException("La gravedad es obligatoria en accidente incapacitante");
            }
        } else if (request.getGravedadIncapacidad() != null) {
            throw new IllegalArgumentException("La gravedad solo aplica a accidente incapacitante");
        }
        if (request.getTipoEvento() != TipoEventoSst.INCIDENTE
                && request.getPersonaAfectadaId() == null
                && request.getSubcontratoId() == null) {
            throw new IllegalArgumentException("Indique la persona afectada o el subcontrato");
        }
        if (request.getFechaInicioDm() != null
                && request.getFechaFinDm() != null
                && request.getFechaFinDm().isBefore(request.getFechaInicioDm())) {
            throw new IllegalArgumentException("La fecha fin de DM no puede ser anterior a la de inicio");
        }
    }

    static int calcularDiasDescanso(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(inicio, fin) + 1;
    }

    private void upsertDm(PeriodoCc periodo, Persona persona, LocalDate fecha) {
        TareoCcDia dia = tareoCcDiaRepository
                .findByPeriodoIdAndPersonaIdAndFecha(periodo.getId(), persona.getId(), fecha)
                .orElseGet(() -> {
                    TareoCcDia nuevo = new TareoCcDia();
                    nuevo.setPeriodo(periodo);
                    nuevo.setPersona(persona);
                    nuevo.setFecha(fecha);
                    return nuevo;
                });
        dia.setCodigo(CodigoTareoCc.DM);
        dia.setHorasOrd(BigDecimal.ZERO);
        dia.setHe60(BigDecimal.ZERO);
        dia.setHe100(BigDecimal.ZERO);
        tareoCcDiaRepository.save(dia);
    }

    private static PeriodoCc periodoQueCubre(List<PeriodoCc> periodos, LocalDate dia) {
        for (PeriodoCc periodo : periodos) {
            if (periodo.getFechaDesde() == null || periodo.getFechaHasta() == null) {
                continue;
            }
            if (!dia.isBefore(periodo.getFechaDesde()) && !dia.isAfter(periodo.getFechaHasta())) {
                return periodo;
            }
        }
        return null;
    }

    private Persona resolverPersonaOpcional(Long personaId) {
        if (personaId == null) {
            return null;
        }
        return personaRepository
                .findById(personaId)
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
    }

    private SubcontratoObra resolverSubcontrato(Long proyectoId, Long subcontratoId) {
        if (subcontratoId == null) {
            return null;
        }
        return subcontratoObraRepository
                .findByIdAndProyectoId(subcontratoId, proyectoId)
                .orElseThrow(() -> new SubcontratoObraNoEncontradoException("No se encontró el subcontrato"));
    }

    private PartidaPresupuesto resolverPartida(Long proyectoId, Long partidaId) {
        if (partidaId == null) {
            return null;
        }
        PartidaPresupuesto partida = partidaPresupuestoRepository
                .findById(partidaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la partida"));
        Long obraId = partida.getVersion() == null || partida.getVersion().getProyecto() == null
                ? null
                : partida.getVersion().getProyecto().getId();
        if (!proyectoId.equals(obraId)) {
            throw new IllegalArgumentException("La partida no pertenece a esta obra");
        }
        return partida;
    }

    private DocumentoObra resolverDocumento(Long proyectoId, Long documentoObraId) {
        if (documentoObraId == null) {
            return null;
        }
        return documentoObraRepository
                .findByIdAndProyectoId(documentoObraId, proyectoId)
                .orElseThrow(() -> new DocumentoObraNoEncontradoException("No se encontró el documento de obra"));
    }

    private SomaEventoSst exigirEvento(Long proyectoId, Long eventoId) {
        if (eventoId == null) {
            throw new IllegalArgumentException("No se encontró el evento");
        }
        return somaEventoSstRepository
                .findByIdAndProyectoId(eventoId, proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el evento"));
    }

    private Proyecto exigirProyecto(Long proyectoId) {
        return proyectoRepository
                .findById(proyectoId)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private static SomaEventoDTO toDto(SomaEventoSst evento) {
        PartidaPresupuesto partida = evento.getPartida();
        Persona persona = evento.getPersonaAfectada();
        SubcontratoObra subcontrato = evento.getSubcontrato();
        DocumentoObra informe = evento.getInforme();
        LocalDate hoy = LocalDate.now();
        List<SomaHallazgoDTO> hallazgos = evento.getHallazgos() == null
                ? List.of()
                : evento.getHallazgos().stream().map(fila -> toHallazgoDto(fila, hoy)).toList();
        return SomaEventoDTO.builder()
                .id(evento.getId())
                .proyectoId(evento.getProyecto() == null ? null : evento.getProyecto().getId())
                .numero(evento.getNumero())
                .tipoEvento(evento.getTipoEvento())
                .gravedadIncapacidad(evento.getGravedadIncapacidad())
                .fechaHora(evento.getFechaHora())
                .lugarExacto(evento.getLugarExacto())
                .partidaId(partida == null ? null : partida.getId())
                .partidaCodigo(partida == null ? null : partida.getCodigo())
                .partidaDescripcion(partida == null ? null : partida.getDescripcion())
                .personaAfectadaId(persona == null ? null : persona.getId())
                .personaAfectadaNombre(nombreCompleto(persona))
                .subcontratoId(subcontrato == null ? null : subcontrato.getId())
                .subcontratoNombre(subcontrato == null ? null : subcontrato.getSubcontratistaRazon())
                .descripcionEvento(evento.getDescripcionEvento())
                .parteCuerpoLesionada(evento.getParteCuerpoLesionada())
                .naturalezaLesion(evento.getNaturalezaLesion())
                .agenteCausante(evento.getAgenteCausante())
                .numeroCertificadoDm(evento.getNumeroCertificadoDm())
                .diasDescansoMedico(evento.getDiasDescansoMedico())
                .diasCargoEstadistico(evento.getDiasCargoEstadistico())
                .fechaInicioDm(evento.getFechaInicioDm())
                .fechaFinDm(evento.getFechaFinDm())
                .causasInmediatas(
                        evento.getCausasInmediatas() == null
                                ? CausasInmediatasEvento.vacio()
                                : evento.getCausasInmediatas())
                .causasBasicas(
                        evento.getCausasBasicas() == null ? CausasBasicasEvento.vacio() : evento.getCausasBasicas())
                .notificadoMtpe(evento.isNotificadoMtpe())
                .estadoInvestigacion(evento.getEstadoInvestigacion())
                .informeDocId(informe == null ? null : informe.getId())
                .createdAt(evento.getCreatedAt())
                .hallazgos(hallazgos)
                .build();
    }

    private static SomaHallazgoDTO toHallazgoDto(SomaHallazgo hallazgo, LocalDate hoy) {
        return SomaHallazgoDTO.builder()
                .id(hallazgo.getId())
                .proyectoId(hallazgo.getProyecto() == null ? null : hallazgo.getProyecto().getId())
                .inspeccionId(hallazgo.getInspeccion() == null ? null : hallazgo.getInspeccion().getId())
                .inspeccionAreaFrente(hallazgo.getInspeccion() == null ? null : hallazgo.getInspeccion().getAreaFrente())
                .clasificacion(hallazgo.getClasificacion())
                .potencialPerdida(hallazgo.getPotencialPerdida())
                .descripcion(hallazgo.getDescripcion())
                .ubicacion(hallazgo.getUbicacion())
                .accionCorrectiva(hallazgo.getAccionCorrectiva())
                .responsablePersonaId(hallazgo.getResponsable() == null ? null : hallazgo.getResponsable().getId())
                .responsableNombre(nombreCompleto(hallazgo.getResponsable()))
                .fechaDeteccion(hallazgo.getFechaDeteccion())
                .fechaLimite(hallazgo.getFechaLimite())
                .fechaCierre(hallazgo.getFechaCierre())
                .paralizacionFrente(hallazgo.isParalizacionFrente())
                .estado(hallazgo.getEstado())
                .vencido(SomaInspeccionServiceImpl.vencido(hallazgo.getEstado(), hallazgo.getFechaLimite(), hoy))
                .createdAt(hallazgo.getCreatedAt())
                .build();
    }

    private static String nombreCompleto(Persona persona) {
        if (persona == null) {
            return null;
        }
        String apellidos = persona.getApellidos() == null ? "" : persona.getApellidos().trim();
        String nombres = persona.getNombres() == null ? "" : persona.getNombres().trim();
        String combinado = (apellidos + (apellidos.isEmpty() || nombres.isEmpty() ? "" : ", ") + nombres).trim();
        return combinado.isEmpty() ? null : combinado;
    }

    private static String exigirTexto(String valor, String mensaje, int maximo) {
        String limpio = blankToNull(valor);
        if (limpio == null) {
            throw new IllegalArgumentException(mensaje);
        }
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException("El texto no puede superar " + maximo + " caracteres");
        }
        return limpio;
    }

    private static String limitar(String valor, int maximo, String etiqueta) {
        String limpio = blankToNull(valor);
        if (limpio == null) {
            return null;
        }
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException(etiqueta + " no puede superar " + maximo + " caracteres");
        }
        return limpio;
    }

    private static String blankToNull(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
