package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.common.exception.DocumentoObraNoEncontradoException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.common.exception.SubcontratoObraNoEncontradoException;
import com.convecsa.erp.construccion.domain.AccionAuditoriaObra;
import com.convecsa.erp.construccion.domain.TipoDocumentoObra;
import com.convecsa.erp.construccion.entity.BitacoraObra;
import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.SubcontratoObra;
import com.convecsa.erp.construccion.repository.BitacoraObraRepository;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.SubcontratoObraRepository;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.EstadoHallazgoSoma;
import com.convecsa.erp.soma.domain.PotencialPerdidaSoma;
import com.convecsa.erp.soma.domain.TipoInspeccionSoma;
import com.convecsa.erp.soma.dto.SomaHallazgoDTO;
import com.convecsa.erp.soma.dto.SomaHallazgoLevantarRequest;
import com.convecsa.erp.soma.dto.SomaHallazgoRequest;
import com.convecsa.erp.soma.dto.SomaInspeccionDTO;
import com.convecsa.erp.soma.dto.SomaInspeccionRequest;
import com.convecsa.erp.soma.entity.SomaHallazgo;
import com.convecsa.erp.soma.entity.SomaInspeccion;
import com.convecsa.erp.soma.repository.SomaHallazgoRepository;
import com.convecsa.erp.soma.repository.SomaInspeccionRepository;
import com.convecsa.erp.soma.service.InterfazSomaInspeccionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaInspeccionServiceImpl implements InterfazSomaInspeccionService {

    private static final BigDecimal PUNTAJE_MIN = BigDecimal.ZERO;
    private static final BigDecimal PUNTAJE_MAX = new BigDecimal("100");

    private final SomaInspeccionRepository somaInspeccionRepository;
    private final SomaHallazgoRepository somaHallazgoRepository;
    private final ProyectoRepository proyectoRepository;
    private final PersonaRepository personaRepository;
    private final SubcontratoObraRepository subcontratoObraRepository;
    private final DocumentoObraRepository documentoObraRepository;
    private final BitacoraObraRepository bitacoraObraRepository;
    private final AuditoriaObraRegistrar auditoriaObraRegistrar;

    @Override
    @Transactional(readOnly = true)
    public List<SomaInspeccionDTO> listarInspecciones(Long proyectoId) {
        exigirProyecto(proyectoId);
        Map<Long, List<SomaHallazgoDTO>> porInspeccion = somaHallazgoRepository
                .findByProyectoIdOrderByFechaDeteccionDescIdDesc(proyectoId)
                .stream()
                .filter(fila -> fila.getInspeccion() != null && fila.getInspeccion().getId() != null)
                .map(fila -> toHallazgoDto(fila, LocalDate.now()))
                .collect(Collectors.groupingBy(SomaHallazgoDTO::getInspeccionId));
        return somaInspeccionRepository.findByProyectoIdOrderByFechaDescIdDesc(proyectoId).stream()
                .map(fila -> toInspeccionDto(fila, porInspeccion.getOrDefault(fila.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional
    public SomaInspeccionDTO crearInspeccion(Long proyectoId, SomaInspeccionRequest request) {
        Proyecto proyecto = exigirProyecto(proyectoId);
        if (request == null) {
            throw new IllegalArgumentException("La inspección es obligatoria");
        }
        if (request.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de inspección es obligatorio");
        }
        if (request.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        SomaInspeccion inspeccion = new SomaInspeccion();
        inspeccion.setProyecto(proyecto);
        inspeccion.setTipo(request.getTipo());
        inspeccion.setFecha(request.getFecha());
        inspeccion.setAreaFrente(exigirTexto(request.getAreaFrente(), "El área o frente es obligatorio", 160));
        inspeccion.setSubcontrato(resolverSubcontrato(proyectoId, request.getTipo(), request.getSubcontratoId()));
        inspeccion.setInspector(resolverPersonaOpcional(request.getInspectorPersonaId()));
        inspeccion.setPuntajeCumplimiento(resolverPuntaje(request.getPuntajeCumplimiento()));
        inspeccion.setObservaciones(limitar(request.getObservaciones(), 1000, "Las observaciones"));
        SomaInspeccion guardada = somaInspeccionRepository.save(inspeccion);
        return toInspeccionDto(guardada, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SomaHallazgoDTO> listarHallazgos(Long proyectoId, EstadoHallazgoSoma estado, Boolean vencidos) {
        exigirProyecto(proyectoId);
        LocalDate hoy = LocalDate.now();
        return somaHallazgoRepository.findByProyectoIdOrderByFechaDeteccionDescIdDesc(proyectoId).stream()
                .map(fila -> toHallazgoDto(fila, hoy))
                .filter(dto -> estado == null || dto.getEstado() == estado)
                .filter(dto -> vencidos == null || dto.isVencido() == vencidos)
                .toList();
    }

    @Override
    @Transactional
    public SomaHallazgoDTO crearHallazgo(Long proyectoId, SomaHallazgoRequest request) {
        Proyecto proyecto = exigirProyecto(proyectoId);
        if (request == null) {
            throw new IllegalArgumentException("El hallazgo es obligatorio");
        }
        if (request.getClasificacion() == null) {
            throw new IllegalArgumentException("La clasificación es obligatoria");
        }
        if (request.getPotencialPerdida() == null) {
            throw new IllegalArgumentException("El potencial de pérdida es obligatorio");
        }
        if (request.getFechaDeteccion() == null) {
            throw new IllegalArgumentException("La fecha de detección es obligatoria");
        }
        if (request.getFechaLimite() == null) {
            throw new IllegalArgumentException("La fecha límite es obligatoria");
        }
        if (request.getFechaLimite().isBefore(request.getFechaDeteccion())) {
            throw new IllegalArgumentException("La fecha límite no puede ser anterior a la de detección");
        }
        boolean paralizacion = Boolean.TRUE.equals(request.getParalizacionFrente());
        if (paralizacion && request.getPotencialPerdida() != PotencialPerdidaSoma.ALTO_GRAVE) {
            throw new IllegalArgumentException("La paralización de frente solo se permite con potencial ALTO_GRAVE");
        }

        SomaHallazgo hallazgo = new SomaHallazgo();
        hallazgo.setProyecto(proyecto);
        hallazgo.setInspeccion(resolverInspeccionOpcional(proyectoId, request.getInspeccionId()));
        hallazgo.setClasificacion(request.getClasificacion());
        hallazgo.setPotencialPerdida(request.getPotencialPerdida());
        hallazgo.setDescripcion(exigirTexto(request.getDescripcion(), "La descripción es obligatoria", 1000));
        hallazgo.setUbicacion(limitar(request.getUbicacion(), 200, "La ubicación"));
        hallazgo.setAccionCorrectiva(
                exigirTexto(request.getAccionCorrectiva(), "La acción correctiva es obligatoria", 1000));
        hallazgo.setResponsable(resolverPersonaOpcional(request.getResponsablePersonaId()));
        hallazgo.setFechaDeteccion(request.getFechaDeteccion());
        hallazgo.setFechaLimite(request.getFechaLimite());
        hallazgo.setParalizacionFrente(paralizacion);
        hallazgo.setEvidenciaAntes(resolverEvidencia(proyectoId, request.getEvidenciaAntesDocId()));
        hallazgo.setEstado(EstadoHallazgoSoma.ABIERTO);

        SomaHallazgo guardado = somaHallazgoRepository.save(hallazgo);
        if (paralizacion) {
            registrarParalizacion(proyecto, guardado);
        }
        return toHallazgoDto(guardado, LocalDate.now());
    }

    @Override
    @Transactional
    public SomaHallazgoDTO marcarEnProceso(Long proyectoId, Long hallazgoId) {
        exigirProyecto(proyectoId);
        SomaHallazgo hallazgo = exigirHallazgo(proyectoId, hallazgoId);
        if (hallazgo.getEstado() == EstadoHallazgoSoma.LEVANTADO) {
            throw new IllegalArgumentException("Un hallazgo levantado no se reabre");
        }
        if (hallazgo.getEstado() != EstadoHallazgoSoma.ABIERTO) {
            throw new IllegalArgumentException("Solo un hallazgo abierto puede pasar a en proceso");
        }
        hallazgo.setEstado(EstadoHallazgoSoma.EN_PROCESO);
        return toHallazgoDto(somaHallazgoRepository.save(hallazgo), LocalDate.now());
    }

    @Override
    @Transactional
    public SomaHallazgoDTO levantar(Long proyectoId, Long hallazgoId, SomaHallazgoLevantarRequest request) {
        exigirProyecto(proyectoId);
        SomaHallazgo hallazgo = exigirHallazgo(proyectoId, hallazgoId);
        if (hallazgo.getEstado() == EstadoHallazgoSoma.LEVANTADO) {
            throw new IllegalArgumentException("Un hallazgo levantado no se reabre");
        }
        if (request == null || request.getEvidenciaDespuesDocId() == null) {
            throw new IllegalArgumentException("La evidencia de cierre es obligatoria");
        }
        hallazgo.setEvidenciaDespues(resolverEvidencia(proyectoId, request.getEvidenciaDespuesDocId()));
        hallazgo.setFechaCierre(request.getFechaCierre() == null ? LocalDate.now() : request.getFechaCierre());
        hallazgo.setEstado(EstadoHallazgoSoma.LEVANTADO);
        return toHallazgoDto(somaHallazgoRepository.save(hallazgo), LocalDate.now());
    }

    private void registrarParalizacion(Proyecto proyecto, SomaHallazgo hallazgo) {
        String ubicacion = hallazgo.getUbicacion() == null ? "" : hallazgo.getUbicacion();
        String texto = "[SOMA] Paralización de frente: " + ubicacion + " — " + hallazgo.getDescripcion();
        if (texto.length() > 4000) {
            texto = texto.substring(0, 4000);
        }
        BitacoraObra nota = new BitacoraObra();
        nota.setProyecto(proyecto);
        nota.setFecha(hallazgo.getFechaDeteccion());
        nota.setTexto(texto);
        nota.setUsuario(AuditoriaObraRegistrar.usuarioActual());
        bitacoraObraRepository.save(nota);
        auditoriaObraRegistrar.registrar(
                proyecto.getId(),
                AccionAuditoriaObra.PARALIZAR_FRENTE_SOMA,
                AuditoriaObraRegistrar.ENTIDAD_HALLAZGO_SOMA,
                hallazgo.getId(),
                texto);
    }

    private SubcontratoObra resolverSubcontrato(Long proyectoId, TipoInspeccionSoma tipo, Long subcontratoId) {
        if (tipo == TipoInspeccionSoma.SUBCONTRATISTA && subcontratoId == null) {
            throw new IllegalArgumentException("El subcontrato es obligatorio en una inspección a subcontratista");
        }
        if (subcontratoId == null) {
            return null;
        }
        return subcontratoObraRepository
                .findByIdAndProyectoId(subcontratoId, proyectoId)
                .orElseThrow(() -> new SubcontratoObraNoEncontradoException("No se encontró el subcontrato"));
    }

    private SomaInspeccion resolverInspeccionOpcional(Long proyectoId, Long inspeccionId) {
        if (inspeccionId == null) {
            return null;
        }
        return somaInspeccionRepository
                .findByIdAndProyectoId(inspeccionId, proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la inspección"));
    }

    private SomaHallazgo exigirHallazgo(Long proyectoId, Long hallazgoId) {
        if (hallazgoId == null) {
            throw new IllegalArgumentException("El hallazgo es obligatorio");
        }
        return somaHallazgoRepository
                .findByIdAndProyectoId(hallazgoId, proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el hallazgo"));
    }

    private Persona resolverPersonaOpcional(Long personaId) {
        if (personaId == null) {
            return null;
        }
        return personaRepository
                .findById(personaId)
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
    }

    private DocumentoObra resolverEvidencia(Long proyectoId, Long documentoId) {
        if (documentoId == null) {
            return null;
        }
        DocumentoObra documento = documentoObraRepository
                .findByIdAndProyectoId(documentoId, proyectoId)
                .orElseThrow(() -> new DocumentoObraNoEncontradoException("No se encontró el documento de obra"));
        if (documento.getTipo() != TipoDocumentoObra.EVIDENCIA_SOMA) {
            throw new IllegalArgumentException("El adjunto debe ser evidencia SOMA");
        }
        return documento;
    }

    private Proyecto exigirProyecto(Long proyectoId) {
        return proyectoRepository
                .findById(proyectoId)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private static BigDecimal resolverPuntaje(BigDecimal puntaje) {
        if (puntaje == null) {
            return null;
        }
        if (puntaje.compareTo(PUNTAJE_MIN) < 0 || puntaje.compareTo(PUNTAJE_MAX) > 0) {
            throw new IllegalArgumentException("El puntaje de cumplimiento debe estar entre 0 y 100");
        }
        return puntaje;
    }

    static boolean vencido(EstadoHallazgoSoma estado, LocalDate fechaLimite, LocalDate hoy) {
        if (estado == EstadoHallazgoSoma.LEVANTADO || fechaLimite == null || hoy == null) {
            return false;
        }
        return fechaLimite.isBefore(hoy);
    }

    private static SomaInspeccionDTO toInspeccionDto(SomaInspeccion inspeccion, List<SomaHallazgoDTO> hallazgos) {
        SubcontratoObra subcontrato = inspeccion.getSubcontrato();
        Persona inspector = inspeccion.getInspector();
        return SomaInspeccionDTO.builder()
                .id(inspeccion.getId())
                .proyectoId(inspeccion.getProyecto() == null ? null : inspeccion.getProyecto().getId())
                .tipo(inspeccion.getTipo())
                .fecha(inspeccion.getFecha())
                .areaFrente(inspeccion.getAreaFrente())
                .subcontratoId(subcontrato == null ? null : subcontrato.getId())
                .subcontratoNombre(subcontrato == null ? null : subcontrato.getSubcontratistaRazon())
                .inspectorPersonaId(inspector == null ? null : inspector.getId())
                .inspectorNombre(nombreCompleto(inspector))
                .puntajeCumplimiento(inspeccion.getPuntajeCumplimiento())
                .observaciones(inspeccion.getObservaciones())
                .createdAt(inspeccion.getCreatedAt())
                .hallazgos(hallazgos == null ? List.of() : hallazgos)
                .build();
    }

    private static SomaHallazgoDTO toHallazgoDto(SomaHallazgo hallazgo, LocalDate hoy) {
        SomaInspeccion inspeccion = hallazgo.getInspeccion();
        Persona responsable = hallazgo.getResponsable();
        DocumentoObra antes = hallazgo.getEvidenciaAntes();
        DocumentoObra despues = hallazgo.getEvidenciaDespues();
        return SomaHallazgoDTO.builder()
                .id(hallazgo.getId())
                .proyectoId(hallazgo.getProyecto() == null ? null : hallazgo.getProyecto().getId())
                .inspeccionId(inspeccion == null ? null : inspeccion.getId())
                .inspeccionAreaFrente(inspeccion == null ? null : inspeccion.getAreaFrente())
                .clasificacion(hallazgo.getClasificacion())
                .potencialPerdida(hallazgo.getPotencialPerdida())
                .descripcion(hallazgo.getDescripcion())
                .ubicacion(hallazgo.getUbicacion())
                .accionCorrectiva(hallazgo.getAccionCorrectiva())
                .responsablePersonaId(responsable == null ? null : responsable.getId())
                .responsableNombre(nombreCompleto(responsable))
                .fechaDeteccion(hallazgo.getFechaDeteccion())
                .fechaLimite(hallazgo.getFechaLimite())
                .fechaCierre(hallazgo.getFechaCierre())
                .paralizacionFrente(hallazgo.isParalizacionFrente())
                .evidenciaAntesDocId(antes == null ? null : antes.getId())
                .evidenciaDespuesDocId(despues == null ? null : despues.getId())
                .estado(hallazgo.getEstado())
                .vencido(vencido(hallazgo.getEstado(), hallazgo.getFechaLimite(), hoy))
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
