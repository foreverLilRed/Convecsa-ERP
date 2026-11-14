package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.common.exception.CuadrillaObraNoEncontradaException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.domain.RolResponsableObra;
import com.convecsa.erp.construccion.entity.CuadrillaObra;
import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.ProyectoResponsable;
import com.convecsa.erp.construccion.repository.CuadrillaMiembroRepository;
import com.convecsa.erp.construccion.repository.CuadrillaObraRepository;
import com.convecsa.erp.construccion.repository.PartidaPresupuestoRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ProyectoResponsableRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.EstadoHabilitacionSoma;
import com.convecsa.erp.soma.domain.PasoAts;
import com.convecsa.erp.soma.dto.SomaCharlaAsistenteDTO;
import com.convecsa.erp.soma.dto.SomaCharlaAsistenteRequest;
import com.convecsa.erp.soma.dto.SomaCharlaAtsDTO;
import com.convecsa.erp.soma.dto.SomaCharlaAtsRequest;
import com.convecsa.erp.soma.dto.SomaHabilitacionDTO;
import com.convecsa.erp.soma.dto.SomaHabilitacionFilaDTO;
import com.convecsa.erp.soma.entity.SomaCharlaAsistente;
import com.convecsa.erp.soma.entity.SomaCharlaAts;
import com.convecsa.erp.soma.repository.SomaCharlaAtsRepository;
import com.convecsa.erp.soma.service.InterfazSomaCharlaAtsService;
import com.convecsa.erp.soma.service.InterfazSomaHabilitacionService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaCharlaAtsServiceImpl implements InterfazSomaCharlaAtsService {

    private final SomaCharlaAtsRepository somaCharlaAtsRepository;
    private final ProyectoRepository proyectoRepository;
    private final PersonaRepository personaRepository;
    private final CuadrillaObraRepository cuadrillaObraRepository;
    private final CuadrillaMiembroRepository cuadrillaMiembroRepository;
    private final PartidaPresupuestoRepository partidaPresupuestoRepository;
    private final ProyectoResponsableRepository proyectoResponsableRepository;
    private final InterfazSomaHabilitacionService somaHabilitacionService;

    @Override
    @Transactional(readOnly = true)
    public List<SomaCharlaAtsDTO> listar(Long proyectoId, LocalDate fecha) {
        exigirProyecto(proyectoId);
        List<SomaCharlaAts> filas = fecha == null
                ? somaCharlaAtsRepository.findByProyectoIdOrderByFechaDescHoraInicioDesc(proyectoId)
                : somaCharlaAtsRepository.findByProyectoIdAndFechaOrderByHoraInicioDesc(proyectoId, fecha);
        return filas.stream().map(charla -> toDto(charla, null)).toList();
    }

    @Override
    @Transactional
    public SomaCharlaAtsDTO crear(Long proyectoId, SomaCharlaAtsRequest request) {
        Proyecto proyecto = exigirProyecto(proyectoId);
        if (request == null) {
            throw new IllegalArgumentException("La charla ATS es obligatoria");
        }
        if (request.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (request.getHoraInicio() == null) {
            throw new IllegalArgumentException("La hora de inicio es obligatoria");
        }
        List<PasoAts> pasos = exigirPasos(request.getPasos());
        int duracion = request.getDuracionMinutos() == null ? 10 : request.getDuracionMinutos();
        if (duracion <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor que 0");
        }

        SomaCharlaAts charla = new SomaCharlaAts();
        charla.setProyecto(proyecto);
        charla.setCuadrilla(resolverCuadrilla(proyectoId, request.getCuadrillaId()));
        charla.setPartida(resolverPartida(proyectoId, request.getPartidaId()));
        charla.setFecha(request.getFecha());
        charla.setHoraInicio(request.getHoraInicio());
        charla.setDuracionMinutos(duracion);
        charla.setTemaCharla(exigirTexto(request.getTemaCharla(), "El tema de la charla es obligatorio", 240));
        charla.setExpositor(resolverPersonaOpcional(request.getExpositorPersonaId()));
        charla.setSupervisorSsoma(resolverSupervisor(proyectoId, request.getSupervisorSsomaId()));
        charla.setPasosAts(pasos);
        charla.setEppVerificado(Boolean.TRUE.equals(request.getEppVerificado()));
        charla.setHerramientasVerif(Boolean.TRUE.equals(request.getHerramientasVerif()));
        charla.setObservaciones(limitar(request.getObservaciones(), 1000, "Las observaciones"));

        Map<Long, EstadoHabilitacionSoma> semaforo = mapaHabilitacion(proyectoId, request.getFecha());
        List<String> bloqueados = new ArrayList<>();
        Set<Long> vistos = new HashSet<>();
        if (request.getAsistentes() != null) {
            for (SomaCharlaAsistenteRequest fila : request.getAsistentes()) {
                if (fila == null || fila.getPersonaId() == null || !vistos.add(fila.getPersonaId())) {
                    continue;
                }
                Persona persona = exigirPersona(fila.getPersonaId());
                boolean habilitado = semaforo.get(persona.getId()) == EstadoHabilitacionSoma.HABILITADO;
                SomaCharlaAsistente asistente = new SomaCharlaAsistente();
                asistente.setPersona(persona);
                asistente.setHabilitadoSoma(habilitado);
                asistente.setObservacion(limitar(fila.getObservacion(), 240, "La observación"));
                charla.addAsistente(asistente);
                if (!habilitado) {
                    String nombre = nombreCompleto(persona);
                    bloqueados.add(nombre == null ? "persona " + persona.getId() : nombre);
                }
            }
        }

        SomaCharlaAts guardada = somaCharlaAtsRepository.save(charla);
        return toDto(guardada, advertencia(bloqueados));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SomaCharlaAsistenteDTO> precargarCuadrilla(Long proyectoId, Long cuadrillaId, LocalDate fecha) {
        exigirProyecto(proyectoId);
        if (cuadrillaId == null) {
            throw new IllegalArgumentException("La cuadrilla es obligatoria");
        }
        CuadrillaObra cuadrilla = cuadrillaObraRepository
                .findByIdAndProyectoId(cuadrillaId, proyectoId)
                .orElseThrow(() -> new CuadrillaObraNoEncontradaException("No se encontró la cuadrilla"));
        LocalDate corte = fecha == null ? LocalDate.now() : fecha;
        Map<Long, EstadoHabilitacionSoma> semaforo = mapaHabilitacion(proyectoId, corte);
        return cuadrillaMiembroRepository.findByProyectoId(proyectoId).stream()
                .filter(miembro -> miembro.getCuadrilla() != null
                        && cuadrilla.getId().equals(miembro.getCuadrilla().getId()))
                .map(miembro -> {
                    Persona persona = miembro.getPersona();
                    boolean habilitado = persona != null
                            && semaforo.get(persona.getId()) == EstadoHabilitacionSoma.HABILITADO;
                    return toAsistenteDto(null, persona, habilitado, null);
                })
                .toList();
    }

    private Map<Long, EstadoHabilitacionSoma> mapaHabilitacion(Long proyectoId, LocalDate fecha) {
        SomaHabilitacionDTO semaforo = somaHabilitacionService.evaluarObra(proyectoId, fecha);
        List<SomaHabilitacionFilaDTO> filas = semaforo == null || semaforo.getFilas() == null
                ? List.of()
                : semaforo.getFilas();
        return filas.stream()
                .filter(fila -> fila.getPersonaId() != null)
                .collect(Collectors.toMap(
                        SomaHabilitacionFilaDTO::getPersonaId,
                        SomaHabilitacionFilaDTO::getEstado,
                        (primero, segundo) -> primero));
    }

    private Persona resolverSupervisor(Long proyectoId, Long supervisorSsomaId) {
        if (supervisorSsomaId != null) {
            return exigirPersona(supervisorSsomaId);
        }
        return proyectoResponsableRepository.findByProyectoIdOrderByRolAsc(proyectoId).stream()
                .filter(fila -> fila.getRol() == RolResponsableObra.SUPERVISOR_SSOMA)
                .map(ProyectoResponsable::getPersona)
                .findFirst()
                .orElse(null);
    }

    private Persona resolverPersonaOpcional(Long personaId) {
        if (personaId == null) {
            return null;
        }
        return exigirPersona(personaId);
    }

    private Persona exigirPersona(Long personaId) {
        return personaRepository
                .findById(personaId)
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
    }

    private CuadrillaObra resolverCuadrilla(Long proyectoId, Long cuadrillaId) {
        if (cuadrillaId == null) {
            return null;
        }
        return cuadrillaObraRepository
                .findByIdAndProyectoId(cuadrillaId, proyectoId)
                .orElseThrow(() -> new CuadrillaObraNoEncontradaException("No se encontró la cuadrilla"));
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

    private Proyecto exigirProyecto(Long proyectoId) {
        return proyectoRepository
                .findById(proyectoId)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private static List<PasoAts> exigirPasos(List<PasoAts> pasos) {
        if (pasos == null || pasos.isEmpty()) {
            throw new IllegalArgumentException("El ATS debe tener al menos un paso");
        }
        List<PasoAts> limpios = new ArrayList<>();
        for (PasoAts paso : pasos) {
            if (paso == null) {
                continue;
            }
            String texto = blankToNull(paso.paso());
            if (texto == null) {
                continue;
            }
            if (texto.length() > 240) {
                throw new IllegalArgumentException("El paso no puede superar 240 caracteres");
            }
            limpios.add(new PasoAts(
                    texto, limitar(paso.peligros(), 1000, "Los peligros"), limitar(paso.controles(), 1000, "Los controles")));
        }
        if (limpios.isEmpty()) {
            throw new IllegalArgumentException("El ATS debe tener al menos un paso");
        }
        return limpios;
    }

    private static SomaCharlaAtsDTO toDto(SomaCharlaAts charla, String advertencia) {
        CuadrillaObra cuadrilla = charla.getCuadrilla();
        PartidaPresupuesto partida = charla.getPartida();
        Persona expositor = charla.getExpositor();
        Persona supervisor = charla.getSupervisorSsoma();
        List<PasoAts> pasos = charla.getPasosAts() == null ? List.of() : List.copyOf(charla.getPasosAts());
        List<SomaCharlaAsistenteDTO> asistentes = charla.getAsistentes() == null
                ? List.of()
                : charla.getAsistentes().stream()
                        .map(fila -> toAsistenteDto(
                                fila.getId(), fila.getPersona(), fila.isHabilitadoSoma(), fila.getObservacion()))
                        .toList();
        return SomaCharlaAtsDTO.builder()
                .id(charla.getId())
                .proyectoId(charla.getProyecto() == null ? null : charla.getProyecto().getId())
                .cuadrillaId(cuadrilla == null ? null : cuadrilla.getId())
                .cuadrillaNombre(cuadrilla == null ? null : cuadrilla.getNombre())
                .partidaId(partida == null ? null : partida.getId())
                .partidaCodigo(partida == null ? null : partida.getCodigo())
                .partidaDescripcion(partida == null ? null : partida.getDescripcion())
                .fecha(charla.getFecha())
                .horaInicio(charla.getHoraInicio())
                .duracionMinutos(charla.getDuracionMinutos())
                .temaCharla(charla.getTemaCharla())
                .expositorPersonaId(expositor == null ? null : expositor.getId())
                .expositorNombre(nombreCompleto(expositor))
                .supervisorSsomaId(supervisor == null ? null : supervisor.getId())
                .supervisorSsomaNombre(nombreCompleto(supervisor))
                .pasos(pasos)
                .eppVerificado(charla.isEppVerificado())
                .herramientasVerif(charla.isHerramientasVerif())
                .observaciones(charla.getObservaciones())
                .createdAt(charla.getCreatedAt())
                .asistentes(asistentes)
                .advertencia(advertencia)
                .build();
    }

    private static SomaCharlaAsistenteDTO toAsistenteDto(
            Long id, Persona persona, boolean habilitadoSoma, String observacion) {
        return SomaCharlaAsistenteDTO.builder()
                .id(id)
                .personaId(persona == null ? null : persona.getId())
                .personaNombre(nombreCompleto(persona))
                .numeroDocumento(persona == null ? null : persona.getNumeroDocumento())
                .habilitadoSoma(habilitadoSoma)
                .observacion(observacion)
                .build();
    }

    private static String advertencia(List<String> bloqueados) {
        if (bloqueados == null || bloqueados.isEmpty()) {
            return null;
        }
        return "Asistentes no habilitados SOMA: " + String.join("; ", bloqueados);
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
