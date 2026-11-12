package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.common.exception.DocumentoObraNoEncontradoException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.common.exception.SomaSctrPolizaNoEncontradaException;
import com.convecsa.erp.construccion.domain.AccionAuditoriaObra;
import com.convecsa.erp.construccion.domain.TipoDocumentoObra;
import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.persona.domain.EstadoDocumentoChecklist;
import com.convecsa.erp.persona.domain.TipoDocumentoChecklist;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.EstadoSctrPoliza;
import com.convecsa.erp.soma.dto.SomaSctrNominaDTO;
import com.convecsa.erp.soma.dto.SomaSctrNominaFilaRequest;
import com.convecsa.erp.soma.dto.SomaSctrNominaGuardarRequest;
import com.convecsa.erp.soma.dto.SomaSctrPolizaDTO;
import com.convecsa.erp.soma.dto.SomaSctrPolizaRequest;
import com.convecsa.erp.soma.dto.SomaTrabajadorObraDTO;
import com.convecsa.erp.soma.entity.SomaSctrNomina;
import com.convecsa.erp.soma.entity.SomaSctrPoliza;
import com.convecsa.erp.soma.repository.SomaSctrPolizaRepository;
import com.convecsa.erp.soma.repository.SomaTrabajadorObraRepository;
import com.convecsa.erp.soma.service.InterfazSomaSctrService;
import com.convecsa.erp.soma.support.ChecklistPersonaActualizador;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaSctrServiceImpl implements InterfazSomaSctrService {

    private final SomaSctrPolizaRepository somaSctrPolizaRepository;
    private final SomaTrabajadorObraRepository somaTrabajadorObraRepository;
    private final ProyectoRepository proyectoRepository;
    private final PersonaRepository personaRepository;
    private final DocumentoObraRepository documentoObraRepository;
    private final AuditoriaObraRegistrar auditoriaObraRegistrar;

    @Override
    @Transactional(readOnly = true)
    public List<SomaSctrPolizaDTO> listar(Long proyectoId) {
        exigirProyecto(proyectoId);
        return somaSctrPolizaRepository.findByProyectoIdOrderByAnioDescMesDesc(proyectoId).stream()
                .map(SomaSctrServiceImpl::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SomaSctrPolizaDTO crear(Long proyectoId, SomaSctrPolizaRequest request) {
        Proyecto proyecto = exigirProyecto(proyectoId);
        if (request == null) {
            throw new IllegalArgumentException("La póliza SCTR es obligatoria");
        }
        int anio = exigirAnio(request.getAnio());
        int mes = exigirMes(request.getMes());
        if (request.getFechaInicio() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }
        if (request.getFechaFin() == null) {
            throw new IllegalArgumentException("La fecha de fin es obligatoria");
        }
        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser menor que la de inicio");
        }
        if (somaSctrPolizaRepository.existsByProyectoIdAndAnioAndMes(proyectoId, anio, mes)) {
            throw new IllegalArgumentException("Ya existe una póliza SCTR para ese mes");
        }

        SomaSctrPoliza poliza = new SomaSctrPoliza();
        poliza.setProyecto(proyecto);
        poliza.setAnio(anio);
        poliza.setMes(mes);
        poliza.setAseguradoraSalud(exigirTexto(request.getAseguradoraSalud(), "La aseguradora de salud es obligatoria", 120));
        poliza.setNumeroPolizaSalud(exigirTexto(request.getNumeroPolizaSalud(), "El número de póliza de salud es obligatorio", 60));
        poliza.setAseguradoraPension(exigirTexto(request.getAseguradoraPension(), "La aseguradora de pensión es obligatoria", 120));
        poliza.setNumeroPolizaPension(exigirTexto(request.getNumeroPolizaPension(), "El número de póliza de pensión es obligatorio", 60));
        poliza.setFechaInicio(request.getFechaInicio());
        poliza.setFechaFin(request.getFechaFin());
        poliza.setMontoPrimaSalud(exigirPrima(request.getMontoPrimaSalud(), "La prima de salud"));
        poliza.setMontoPrimaPension(exigirPrima(request.getMontoPrimaPension(), "La prima de pensión"));
        poliza.setDocumentoObra(resolverDocumento(proyectoId, request.getDocumentoObraId()));
        poliza.setEstado(EstadoSctrPoliza.VIGENTE);

        SomaSctrPoliza guardada = somaSctrPolizaRepository.save(poliza);
        auditar(proyectoId, guardada.getId(), "póliza " + anio + "-" + String.format("%02d", mes));
        return toDto(guardada);
    }

    @Override
    @Transactional
    public SomaSctrPolizaDTO guardarNomina(Long proyectoId, Long polizaId, SomaSctrNominaGuardarRequest request) {
        exigirProyecto(proyectoId);
        SomaSctrPoliza poliza = buscarPoliza(proyectoId, polizaId);
        if (poliza.getEstado() == EstadoSctrPoliza.ANULADA) {
            throw new IllegalArgumentException("No se puede editar la nómina de una póliza anulada");
        }
        List<SomaSctrNominaFilaRequest> filas = request == null || request.getFilas() == null
                ? List.of()
                : request.getFilas();
        Set<Long> vistos = new HashSet<>();
        Set<Long> personaIds = new LinkedHashSet<>();
        for (SomaSctrNominaFilaRequest fila : filas) {
            if (fila == null || fila.getPersonaId() == null) {
                throw new IllegalArgumentException("Cada fila de la nómina requiere una persona");
            }
            if (!vistos.add(fila.getPersonaId())) {
                throw new IllegalArgumentException("Hay personas duplicadas en la nómina");
            }
            personaIds.add(fila.getPersonaId());
        }
        Map<Long, Persona> personas = personaRepository.findAllById(personaIds).stream()
                .collect(Collectors.toMap(Persona::getId, Function.identity()));
        if (personas.size() != personaIds.size()) {
            throw new PersonaNoEncontradaException("No se encontró una de las personas de la nómina");
        }

        poliza.getNomina().clear();
        for (SomaSctrNominaFilaRequest fila : filas) {
            SomaSctrNomina item = new SomaSctrNomina();
            item.setPersona(personas.get(fila.getPersonaId()));
            item.setIncluidoSalud(fila.getIncluidoSalud() == null || fila.getIncluidoSalud());
            item.setIncluidoPension(fila.getIncluidoPension() == null || fila.getIncluidoPension());
            poliza.addNomina(item);
        }
        sincronizarChecklist(poliza);
        auditar(proyectoId, poliza.getId(), "nómina " + poliza.getAnio() + "-" + String.format("%02d", poliza.getMes())
                + ", " + filas.size() + " personas");
        return toDto(poliza);
    }

    @Override
    @Transactional
    public SomaSctrPolizaDTO anular(Long proyectoId, Long polizaId) {
        exigirProyecto(proyectoId);
        SomaSctrPoliza poliza = buscarPoliza(proyectoId, polizaId);
        if (poliza.getEstado() == EstadoSctrPoliza.ANULADA) {
            throw new IllegalArgumentException("La póliza ya está anulada");
        }
        poliza.setEstado(EstadoSctrPoliza.ANULADA);
        if (esMesEnCurso(poliza)) {
            for (Persona persona : personasActivasDelMes(proyectoId, poliza.getAnio(), poliza.getMes())) {
                ChecklistPersonaActualizador.aplicar(
                        persona, TipoDocumentoChecklist.SCTR, EstadoDocumentoChecklist.PENDIENTE);
            }
        }
        auditar(proyectoId, poliza.getId(), "anular " + poliza.getAnio() + "-" + String.format("%02d", poliza.getMes()));
        return toDto(poliza);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SomaTrabajadorObraDTO> sugerirNomina(Long proyectoId, Integer anio, Integer mes) {
        exigirProyecto(proyectoId);
        int anioValido = exigirAnio(anio);
        int mesValido = exigirMes(mes);
        return somaTrabajadorObraRepository.buscarActivosEnMes(proyectoId, anioValido, mesValido).stream()
                .map(SomaSctrServiceImpl::toTrabajadorDto)
                .toList();
    }

    void sincronizarChecklist(SomaSctrPoliza poliza) {
        if (poliza.getEstado() != EstadoSctrPoliza.VIGENTE || !esMesEnCurso(poliza)) {
            return;
        }
        Set<Long> cubiertos = poliza.getNomina().stream()
                .filter(fila -> fila.isIncluidoSalud() && fila.isIncluidoPension())
                .map(fila -> fila.getPersona().getId())
                .collect(Collectors.toSet());
        List<Persona> activos = personasActivasDelMes(
                poliza.getProyecto().getId(), poliza.getAnio(), poliza.getMes());
        Set<Long> idsActivos = new HashSet<>();
        for (Persona persona : activos) {
            idsActivos.add(persona.getId());
            EstadoDocumentoChecklist estado = cubiertos.contains(persona.getId())
                    ? EstadoDocumentoChecklist.CONFORME
                    : EstadoDocumentoChecklist.PENDIENTE;
            ChecklistPersonaActualizador.aplicar(persona, TipoDocumentoChecklist.SCTR, estado);
        }
        Set<Long> cubiertosFuera = new LinkedHashSet<>(cubiertos);
        cubiertosFuera.removeAll(idsActivos);
        if (!cubiertosFuera.isEmpty()) {
            for (Persona persona : personaRepository.findAllById(cubiertosFuera)) {
                ChecklistPersonaActualizador.aplicar(
                        persona, TipoDocumentoChecklist.SCTR, EstadoDocumentoChecklist.CONFORME);
            }
        }
    }

    private List<Persona> personasActivasDelMes(Long proyectoId, int anio, int mes) {
        return somaTrabajadorObraRepository.buscarActivosEnMes(proyectoId, anio, mes).stream()
                .map(item -> item.getContrato().getPersona())
                .toList();
    }

    private static boolean esMesEnCurso(SomaSctrPoliza poliza) {
        YearMonth actual = YearMonth.now();
        return poliza.getAnio() == actual.getYear() && poliza.getMes() == actual.getMonthValue();
    }

    private SomaSctrPoliza buscarPoliza(Long proyectoId, Long polizaId) {
        if (polizaId == null) {
            throw new IllegalArgumentException("La póliza es obligatoria");
        }
        return somaSctrPolizaRepository.findByIdAndProyectoId(polizaId, proyectoId)
                .orElseThrow(() -> new SomaSctrPolizaNoEncontradaException("No se encontró la póliza SCTR"));
    }

    private Proyecto exigirProyecto(Long proyectoId) {
        return proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private DocumentoObra resolverDocumento(Long proyectoId, Long documentoObraId) {
        if (documentoObraId == null) {
            return null;
        }
        DocumentoObra documento = documentoObraRepository.findByIdAndProyectoId(documentoObraId, proyectoId)
                .orElseThrow(() -> new DocumentoObraNoEncontradoException("No se encontró el documento de obra"));
        if (documento.getTipo() != TipoDocumentoObra.SCTR_POLIZA) {
            throw new IllegalArgumentException("El adjunto debe ser una póliza SCTR");
        }
        return documento;
    }

    private void auditar(Long proyectoId, Long entidadId, String detalle) {
        auditoriaObraRegistrar.registrar(
                proyectoId,
                AccionAuditoriaObra.REGISTRAR_SCTR_OBRA,
                AuditoriaObraRegistrar.ENTIDAD_SCTR_SOMA,
                entidadId,
                detalle);
    }

    private static SomaSctrPolizaDTO toDto(SomaSctrPoliza poliza) {
        Proyecto proyecto = poliza.getProyecto();
        DocumentoObra documento = poliza.getDocumentoObra();
        List<SomaSctrNominaDTO> nomina = poliza.getNomina() == null
                ? List.of()
                : poliza.getNomina().stream().map(SomaSctrServiceImpl::toNominaDto).toList();
        return SomaSctrPolizaDTO.builder()
                .id(poliza.getId())
                .proyectoId(proyecto == null ? null : proyecto.getId())
                .anio(poliza.getAnio())
                .mes(poliza.getMes())
                .aseguradoraSalud(poliza.getAseguradoraSalud())
                .numeroPolizaSalud(poliza.getNumeroPolizaSalud())
                .aseguradoraPension(poliza.getAseguradoraPension())
                .numeroPolizaPension(poliza.getNumeroPolizaPension())
                .fechaInicio(poliza.getFechaInicio())
                .fechaFin(poliza.getFechaFin())
                .montoPrimaSalud(poliza.getMontoPrimaSalud())
                .montoPrimaPension(poliza.getMontoPrimaPension())
                .documentoObraId(documento == null ? null : documento.getId())
                .estado(poliza.getEstado())
                .createdAt(poliza.getCreatedAt())
                .nomina(nomina)
                .build();
    }

    private static SomaSctrNominaDTO toNominaDto(SomaSctrNomina fila) {
        Persona persona = fila.getPersona();
        return SomaSctrNominaDTO.builder()
                .personaId(persona == null ? null : persona.getId())
                .personaNombre(nombreCompleto(persona))
                .numeroDocumento(persona == null ? null : persona.getNumeroDocumento())
                .incluidoSalud(fila.isIncluidoSalud())
                .incluidoPension(fila.isIncluidoPension())
                .build();
    }

    private static SomaTrabajadorObraDTO toTrabajadorDto(ContratoCc contratoCc) {
        Contrato contrato = contratoCc.getContrato();
        Persona persona = contrato.getPersona();
        return SomaTrabajadorObraDTO.builder()
                .personaId(persona.getId())
                .nombres(persona.getNombres())
                .apellidos(persona.getApellidos())
                .numeroDocumento(persona.getNumeroDocumento())
                .categoria(contratoCc.getCategoria())
                .fechaIngreso(contrato.getFechaInicio())
                .fechaCese(contrato.getFechaFin())
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

    private static int exigirAnio(Integer anio) {
        if (anio == null || anio < 2000 || anio > 2100) {
            throw new IllegalArgumentException("El año es obligatorio y debe ser válido");
        }
        return anio;
    }

    private static int exigirMes(Integer mes) {
        if (mes == null || mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12");
        }
        return mes;
    }

    private static BigDecimal exigirPrima(BigDecimal valor, String etiqueta) {
        BigDecimal monto = valor == null ? BigDecimal.ZERO : valor;
        if (monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(etiqueta + " no puede ser negativa");
        }
        return monto;
    }

    private static String exigirTexto(String valor, String mensaje, int maximo) {
        if (valor == null) {
            throw new IllegalArgumentException(mensaje);
        }
        String limpio = valor.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException("El texto no puede superar " + maximo + " caracteres");
        }
        return limpio;
    }
}
