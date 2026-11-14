package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.entity.AsignacionEquipo;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.AsignacionEquipoRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.ChecklistPreUso;
import com.convecsa.erp.soma.domain.ResultadoPreUsoEquipo;
import com.convecsa.erp.soma.dto.SomaPreUsoBloqueoDTO;
import com.convecsa.erp.soma.dto.SomaPreUsoDTO;
import com.convecsa.erp.soma.dto.SomaPreUsoRequest;
import com.convecsa.erp.soma.entity.SomaInspeccionEquipo;
import com.convecsa.erp.soma.repository.SomaInspeccionEquipoRepository;
import com.convecsa.erp.soma.service.InterfazSomaPreUsoService;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaPreUsoServiceImpl implements InterfazSomaPreUsoService {

    private final SomaInspeccionEquipoRepository somaInspeccionEquipoRepository;
    private final ProyectoRepository proyectoRepository;
    private final AsignacionEquipoRepository asignacionEquipoRepository;
    private final PersonaRepository personaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SomaPreUsoDTO> listar(Long proyectoId) {
        exigirProyecto(proyectoId);
        return somaInspeccionEquipoRepository.findByProyectoIdOrderByFechaDescIdDesc(proyectoId).stream()
                .map(SomaPreUsoServiceImpl::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SomaPreUsoDTO crear(Long proyectoId, SomaPreUsoRequest request) {
        Proyecto proyecto = exigirProyecto(proyectoId);
        if (request == null) {
            throw new IllegalArgumentException("La inspección de pre-uso es obligatoria");
        }
        if (request.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (request.getResultado() == null) {
            throw new IllegalArgumentException("El resultado es obligatorio");
        }
        if (request.getAsignacionEquipoId() == null) {
            throw new IllegalArgumentException("La asignación de equipo es obligatoria");
        }
        AsignacionEquipo asignacion = asignacionEquipoRepository
                .findByIdAndProyectoId(request.getAsignacionEquipoId(), proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("La asignación no pertenece a esta obra"));
        Map<String, Boolean> checklist = exigirChecklist(request.getChecklist());
        if (request.getResultado() == ResultadoPreUsoEquipo.OPERATIVO && tieneItemFalso(checklist)) {
            throw new IllegalArgumentException("Un ítem en falso no permite resultado OPERATIVO");
        }

        SomaInspeccionEquipo fila = new SomaInspeccionEquipo();
        fila.setProyecto(proyecto);
        fila.setAsignacion(asignacion);
        fila.setOperador(resolverPersonaOpcional(request.getOperadorPersonaId()));
        fila.setFecha(request.getFecha());
        fila.setHorometroLectura(request.getHorometroLectura());
        fila.setChecklist(checklist);
        fila.setResultado(request.getResultado());
        fila.setObservaciones(limitar(request.getObservaciones(), 600, "Las observaciones"));
        return toDto(somaInspeccionEquipoRepository.save(fila));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SomaPreUsoBloqueoDTO> listarBloqueos(Long proyectoId, LocalDate fecha) {
        exigirProyecto(proyectoId);
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        return somaInspeccionEquipoRepository
                .findByProyectoIdAndFechaAndResultado(
                        proyectoId, fecha, ResultadoPreUsoEquipo.INOPERATIVO_BLOQUEADO)
                .stream()
                .map(SomaPreUsoServiceImpl::toBloqueoDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean equipoBloqueadoEn(Long asignacionId, LocalDate fecha) {
        if (asignacionId == null || fecha == null) {
            return false;
        }
        return somaInspeccionEquipoRepository.existsByAsignacionIdAndFechaAndResultado(
                asignacionId, fecha, ResultadoPreUsoEquipo.INOPERATIVO_BLOQUEADO);
    }

    private static Map<String, Boolean> exigirChecklist(Map<String, Boolean> origen) {
        if (origen == null) {
            throw new IllegalArgumentException("El checklist debe incluir todos los ítems");
        }
        Map<String, Boolean> destino = new LinkedHashMap<>();
        for (String clave : ChecklistPreUso.items()) {
            Boolean valor = origen.get(clave);
            if (valor == null) {
                throw new IllegalArgumentException("El checklist debe incluir todos los ítems");
            }
            destino.put(clave, valor);
        }
        return destino;
    }

    private static boolean tieneItemFalso(Map<String, Boolean> checklist) {
        for (String clave : ChecklistPreUso.items()) {
            if (!Boolean.TRUE.equals(checklist.get(clave))) {
                return true;
            }
        }
        return false;
    }

    private Persona resolverPersonaOpcional(Long personaId) {
        if (personaId == null) {
            return null;
        }
        return personaRepository
                .findById(personaId)
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
    }

    private Proyecto exigirProyecto(Long proyectoId) {
        return proyectoRepository
                .findById(proyectoId)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private static SomaPreUsoDTO toDto(SomaInspeccionEquipo fila) {
        AsignacionEquipo asignacion = fila.getAsignacion();
        Persona operador = fila.getOperador();
        return SomaPreUsoDTO.builder()
                .id(fila.getId())
                .proyectoId(fila.getProyecto() == null ? null : fila.getProyecto().getId())
                .asignacionEquipoId(asignacion == null ? null : asignacion.getId())
                .equipoCodigo(asignacion == null ? null : asignacion.getCodigo())
                .equipoDescripcion(asignacion == null ? null : asignacion.getDescripcion())
                .operadorPersonaId(operador == null ? null : operador.getId())
                .operadorNombre(nombreCompleto(operador))
                .operadorDocumento(operador == null ? null : operador.getNumeroDocumento())
                .fecha(fila.getFecha())
                .horometroLectura(fila.getHorometroLectura())
                .checklist(fila.getChecklist() == null ? Map.of() : Map.copyOf(fila.getChecklist()))
                .resultado(fila.getResultado())
                .observaciones(fila.getObservaciones())
                .createdAt(fila.getCreatedAt())
                .build();
    }

    private static SomaPreUsoBloqueoDTO toBloqueoDto(SomaInspeccionEquipo fila) {
        AsignacionEquipo asignacion = fila.getAsignacion();
        return SomaPreUsoBloqueoDTO.builder()
                .asignacionId(asignacion == null ? null : asignacion.getId())
                .codigo(asignacion == null ? null : asignacion.getCodigo())
                .descripcion(asignacion == null ? null : asignacion.getDescripcion())
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
