package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.common.exception.CuadrillaObraNoEncontradaException;
import com.convecsa.erp.common.exception.DocumentoObraNoEncontradoException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.domain.AccionAuditoriaObra;
import com.convecsa.erp.construccion.domain.TipoDocumentoObra;
import com.convecsa.erp.construccion.entity.CuadrillaObra;
import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.CuadrillaMiembroRepository;
import com.convecsa.erp.construccion.repository.CuadrillaObraRepository;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.TipoCapacitacionSoma;
import com.convecsa.erp.soma.dto.SomaCapacitacionAsistenteDTO;
import com.convecsa.erp.soma.dto.SomaCapacitacionAsistenteRequest;
import com.convecsa.erp.soma.dto.SomaCapacitacionDTO;
import com.convecsa.erp.soma.dto.SomaCapacitacionRequest;
import com.convecsa.erp.soma.entity.SomaCapacitacion;
import com.convecsa.erp.soma.entity.SomaCapacitacionAsistente;
import com.convecsa.erp.soma.repository.SomaCapacitacionRepository;
import com.convecsa.erp.soma.service.InterfazSomaCapacitacionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
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
public class SomaCapacitacionServiceImpl implements InterfazSomaCapacitacionService {

    private static final BigDecimal HORAS_MINIMAS_INDUCCION = new BigDecimal("4");
    private static final BigDecimal NOTA_APROBATORIA = new BigDecimal("11");
    private static final BigDecimal NOTA_MAXIMA = new BigDecimal("20");

    private final SomaCapacitacionRepository somaCapacitacionRepository;
    private final ProyectoRepository proyectoRepository;
    private final PersonaRepository personaRepository;
    private final DocumentoObraRepository documentoObraRepository;
    private final CuadrillaObraRepository cuadrillaObraRepository;
    private final CuadrillaMiembroRepository cuadrillaMiembroRepository;
    private final AuditoriaObraRegistrar auditoriaObraRegistrar;

    @Override
    @Transactional(readOnly = true)
    public List<SomaCapacitacionDTO> listar(Long proyectoId) {
        exigirProyecto(proyectoId);
        return somaCapacitacionRepository.findByProyectoIdOrderByFechaDescIdDesc(proyectoId).stream()
                .map(SomaCapacitacionServiceImpl::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SomaCapacitacionDTO> listarCorporativo(Integer anio) {
        if (anio == null) {
            throw new IllegalArgumentException("El año es obligatorio");
        }
        LocalDate desde = LocalDate.of(anio, 1, 1);
        LocalDate hasta = LocalDate.of(anio, 12, 31);
        return somaCapacitacionRepository.findByFechaBetweenOrderByFechaDescIdDesc(desde, hasta).stream()
                .map(SomaCapacitacionServiceImpl::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SomaCapacitacionDTO crear(Long proyectoId, SomaCapacitacionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La capacitación es obligatoria");
        }
        if (request.getTipo() == null) {
            throw new IllegalArgumentException("El tipo es obligatorio");
        }
        if (request.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (request.getHorasDuracion() == null || request.getHorasDuracion().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Las horas de duración deben ser mayores que cero");
        }
        if (request.getTipo() == TipoCapacitacionSoma.INDUCCION_HOMBRE_NUEVO
                && request.getHorasDuracion().compareTo(HORAS_MINIMAS_INDUCCION) < 0) {
            throw new IllegalArgumentException("La inducción de hombre nuevo debe durar al menos 4 horas");
        }
        String tema = exigirTexto(request.getTema(), "El tema es obligatorio", 240);
        String expositor = exigirTexto(request.getExpositorNombre(), "El expositor es obligatorio", 160);
        List<SomaCapacitacionAsistenteRequest> asistentesRequest =
                request.getAsistentes() == null ? List.of() : request.getAsistentes();
        if (asistentesRequest.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un asistente");
        }

        Proyecto proyecto = exigirProyecto(proyectoId);
        Set<Long> vistos = new HashSet<>();
        for (SomaCapacitacionAsistenteRequest fila : asistentesRequest) {
            if (fila.getPersonaId() == null) {
                throw new IllegalArgumentException("La persona es obligatoria");
            }
            if (!vistos.add(fila.getPersonaId())) {
                throw new IllegalArgumentException("La persona ya está en la lista de asistentes");
            }
            if (fila.getNota() != null
                    && (fila.getNota().compareTo(BigDecimal.ZERO) < 0 || fila.getNota().compareTo(NOTA_MAXIMA) > 0)) {
                throw new IllegalArgumentException("La nota debe estar entre 0 y 20");
            }
        }
        Map<Long, Persona> personas = personaRepository.findAllById(vistos).stream()
                .collect(Collectors.toMap(Persona::getId, Function.identity()));
        for (Long personaId : vistos) {
            if (!personas.containsKey(personaId)) {
                throw new PersonaNoEncontradaException("No se encontró la persona");
            }
        }

        SomaCapacitacion capacitacion = new SomaCapacitacion();
        capacitacion.setProyecto(proyecto);
        capacitacion.setTipo(request.getTipo());
        capacitacion.setTema(tema);
        capacitacion.setFecha(request.getFecha());
        capacitacion.setHorasDuracion(request.getHorasDuracion());
        capacitacion.setExpositorNombre(expositor);
        capacitacion.setExpositorEntidad(limitar(request.getExpositorEntidad(), 160, "La entidad del expositor"));
        capacitacion.setPuntajeSimulacro(request.getPuntajeSimulacro());
        capacitacion.setObservaciones(limitar(request.getObservaciones(), 1000, "Las observaciones"));
        capacitacion.setEvidencia(resolverDocumento(proyectoId, request.getEvidenciaDocId()));

        for (SomaCapacitacionAsistenteRequest fila : asistentesRequest) {
            SomaCapacitacionAsistente asistente = new SomaCapacitacionAsistente();
            asistente.setPersona(personas.get(fila.getPersonaId()));
            asistente.setNota(fila.getNota());
            asistente.setAprobado(resolverAprobado(fila));
            capacitacion.addAsistente(asistente);
        }

        SomaCapacitacion guardada = somaCapacitacionRepository.save(capacitacion);
        auditoriaObraRegistrar.registrar(
                proyectoId,
                AccionAuditoriaObra.REGISTRAR_CAPACITACION_SOMA,
                AuditoriaObraRegistrar.ENTIDAD_CAPACITACION_SOMA,
                guardada.getId(),
                guardada.getTipo() + " — " + guardada.getTema());
        return toDto(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SomaCapacitacionAsistenteDTO> precargarCuadrilla(Long proyectoId, Long cuadrillaId) {
        exigirProyecto(proyectoId);
        if (cuadrillaId == null) {
            throw new IllegalArgumentException("La cuadrilla es obligatoria");
        }
        CuadrillaObra cuadrilla = cuadrillaObraRepository
                .findByIdAndProyectoId(cuadrillaId, proyectoId)
                .orElseThrow(() -> new CuadrillaObraNoEncontradaException("No se encontró la cuadrilla"));
        return cuadrillaMiembroRepository.findByProyectoId(proyectoId).stream()
                .filter(miembro -> miembro.getCuadrilla() != null
                        && cuadrilla.getId().equals(miembro.getCuadrilla().getId()))
                .map(miembro -> toAsistenteDto(null, miembro.getPersona(), null, true))
                .toList();
    }

    private Proyecto exigirProyecto(Long proyectoId) {
        return proyectoRepository
                .findById(proyectoId)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private DocumentoObra resolverDocumento(Long proyectoId, Long documentoObraId) {
        if (documentoObraId == null) {
            return null;
        }
        DocumentoObra documento = documentoObraRepository
                .findByIdAndProyectoId(documentoObraId, proyectoId)
                .orElseThrow(() -> new DocumentoObraNoEncontradoException("No se encontró el documento de obra"));
        if (documento.getTipo() != TipoDocumentoObra.EVIDENCIA_SOMA) {
            throw new IllegalArgumentException("El adjunto debe ser una evidencia SOMA");
        }
        return documento;
    }

    static boolean resolverAprobado(SomaCapacitacionAsistenteRequest fila) {
        if (fila.getNota() != null) {
            return fila.getNota().compareTo(NOTA_APROBATORIA) >= 0;
        }
        return fila.getAprobado() == null || fila.getAprobado();
    }

    private static SomaCapacitacionDTO toDto(SomaCapacitacion capacitacion) {
        Proyecto proyecto = capacitacion.getProyecto();
        DocumentoObra evidencia = capacitacion.getEvidencia();
        return SomaCapacitacionDTO.builder()
                .id(capacitacion.getId())
                .proyectoId(proyecto == null ? null : proyecto.getId())
                .tipo(capacitacion.getTipo())
                .tema(capacitacion.getTema())
                .fecha(capacitacion.getFecha())
                .horasDuracion(capacitacion.getHorasDuracion())
                .expositorNombre(capacitacion.getExpositorNombre())
                .expositorEntidad(capacitacion.getExpositorEntidad())
                .puntajeSimulacro(capacitacion.getPuntajeSimulacro())
                .observaciones(capacitacion.getObservaciones())
                .evidenciaDocId(evidencia == null ? null : evidencia.getId())
                .createdAt(capacitacion.getCreatedAt())
                .asistentes(capacitacion.getAsistentes().stream()
                        .map(item -> toAsistenteDto(item.getId(), item.getPersona(), item.getNota(), item.isAprobado()))
                        .toList())
                .build();
    }

    private static SomaCapacitacionAsistenteDTO toAsistenteDto(
            Long id, Persona persona, BigDecimal nota, boolean aprobado) {
        return SomaCapacitacionAsistenteDTO.builder()
                .id(id)
                .personaId(persona == null ? null : persona.getId())
                .personaNombre(nombreCompleto(persona))
                .numeroDocumento(persona == null ? null : persona.getNumeroDocumento())
                .nota(nota)
                .aprobado(aprobado)
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
