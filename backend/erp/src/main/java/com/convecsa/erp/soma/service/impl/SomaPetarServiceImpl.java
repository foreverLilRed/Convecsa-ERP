package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.common.exception.CuadrillaObraNoEncontradaException;
import com.convecsa.erp.common.exception.DocumentoObraNoEncontradoException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.domain.AccionAuditoriaObra;
import com.convecsa.erp.construccion.domain.EstadoPeriodoCc;
import com.convecsa.erp.construccion.domain.RolResponsableObra;
import com.convecsa.erp.construccion.entity.CuadrillaObra;
import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.ProyectoResponsable;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import com.convecsa.erp.construccion.repository.CuadrillaObraRepository;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.PartidaPresupuestoRepository;
import com.convecsa.erp.construccion.repository.PeriodoCcRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ProyectoResponsableRepository;
import com.convecsa.erp.construccion.repository.TareoCcDiaRepository;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.AptitudEmo;
import com.convecsa.erp.soma.domain.ChecklistPetar;
import com.convecsa.erp.soma.domain.EstadoHabilitacionSoma;
import com.convecsa.erp.soma.domain.EstadoPetar;
import com.convecsa.erp.soma.domain.TipoTrabajoPetar;
import com.convecsa.erp.soma.dto.SomaHabilitacionDTO;
import com.convecsa.erp.soma.dto.SomaHabilitacionFilaDTO;
import com.convecsa.erp.soma.dto.SomaPetarChecklistItemDTO;
import com.convecsa.erp.soma.dto.SomaPetarDTO;
import com.convecsa.erp.soma.dto.SomaPetarFaltanteTareoDTO;
import com.convecsa.erp.soma.dto.SomaPetarRequest;
import com.convecsa.erp.soma.dto.SomaPetarTrabajadorDTO;
import com.convecsa.erp.soma.dto.SomaPetarTrabajadorRequest;
import com.convecsa.erp.soma.entity.SomaEmoExamen;
import com.convecsa.erp.soma.entity.SomaPetar;
import com.convecsa.erp.soma.entity.SomaPetarTrabajador;
import com.convecsa.erp.soma.repository.SomaEmoExamenRepository;
import com.convecsa.erp.soma.repository.SomaPetarRepository;
import com.convecsa.erp.soma.service.InterfazSomaHabilitacionService;
import com.convecsa.erp.soma.service.InterfazSomaPetarService;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaPetarServiceImpl implements InterfazSomaPetarService {

    private static final List<EstadoPetar> ESTADOS_CUBRE_TAREO = List.of(EstadoPetar.AUTORIZADO, EstadoPetar.CERRADO);

    private final SomaPetarRepository somaPetarRepository;
    private final ProyectoRepository proyectoRepository;
    private final PersonaRepository personaRepository;
    private final CuadrillaObraRepository cuadrillaObraRepository;
    private final PartidaPresupuestoRepository partidaPresupuestoRepository;
    private final ProyectoResponsableRepository proyectoResponsableRepository;
    private final DocumentoObraRepository documentoObraRepository;
    private final PeriodoCcRepository periodoCcRepository;
    private final TareoCcDiaRepository tareoCcDiaRepository;
    private final SomaEmoExamenRepository somaEmoExamenRepository;
    private final InterfazSomaHabilitacionService somaHabilitacionService;
    private final AuditoriaObraRegistrar auditoriaObraRegistrar;

    @Override
    @Transactional(readOnly = true)
    public List<SomaPetarDTO> listar(Long proyectoId, LocalDate fecha) {
        exigirProyecto(proyectoId);
        List<SomaPetar> filas = fecha == null
                ? somaPetarRepository.findByProyectoIdOrderByFechaDescNumeroDesc(proyectoId)
                : somaPetarRepository.findByProyectoIdAndFechaOrderByNumeroDesc(proyectoId, fecha);
        return filas.stream().map(SomaPetarServiceImpl::toDto).toList();
    }

    @Override
    @Transactional
    public SomaPetarDTO crear(Long proyectoId, SomaPetarRequest request) {
        Proyecto proyecto = exigirProyecto(proyectoId);
        validarCabecera(request);
        SomaPetar petar = new SomaPetar();
        petar.setProyecto(proyecto);
        petar.setNumero(somaPetarRepository.findMaxNumero(proyectoId) + 1);
        petar.setEstado(EstadoPetar.BORRADOR);
        aplicarCabecera(petar, proyectoId, request);
        SomaPetar guardado = somaPetarRepository.save(petar);
        return toDto(guardado);
    }

    @Override
    @Transactional
    public SomaPetarDTO actualizar(Long proyectoId, Long petarId, SomaPetarRequest request) {
        exigirProyecto(proyectoId);
        validarCabecera(request);
        SomaPetar petar = exigirPetar(proyectoId, petarId);
        if (petar.getEstado() != EstadoPetar.BORRADOR) {
            throw new IllegalArgumentException("Solo se puede editar un PETAR en BORRADOR");
        }
        aplicarCabecera(petar, proyectoId, request);
        return toDto(petar);
    }

    @Override
    @Transactional
    public SomaPetarDTO autorizar(Long proyectoId, Long petarId) {
        exigirProyecto(proyectoId);
        SomaPetar petar = exigirPetar(proyectoId, petarId);
        exigirTransicion(petar.getEstado(), EstadoPetar.AUTORIZADO, EstadoPetar.BORRADOR);
        validarAutorizacion(proyectoId, petar);
        petar.setEstado(EstadoPetar.AUTORIZADO);
        petar.setAutorizadoPor(AuditoriaObraRegistrar.usuarioActual());
        petar.setAutorizadoAt(Instant.now());
        auditoriaObraRegistrar.registrar(
                proyectoId,
                AccionAuditoriaObra.AUTORIZAR_PETAR,
                AuditoriaObraRegistrar.ENTIDAD_PETAR_SOMA,
                petar.getId(),
                "PETAR N° " + petar.getNumero() + " " + petar.getTipoTrabajo());
        return toDto(petar);
    }

    @Override
    @Transactional
    public SomaPetarDTO cerrar(Long proyectoId, Long petarId) {
        exigirProyecto(proyectoId);
        SomaPetar petar = exigirPetar(proyectoId, petarId);
        exigirTransicion(petar.getEstado(), EstadoPetar.CERRADO, EstadoPetar.AUTORIZADO);
        petar.setEstado(EstadoPetar.CERRADO);
        return toDto(petar);
    }

    @Override
    @Transactional
    public SomaPetarDTO suspender(Long proyectoId, Long petarId) {
        exigirProyecto(proyectoId);
        SomaPetar petar = exigirPetar(proyectoId, petarId);
        exigirTransicion(petar.getEstado(), EstadoPetar.SUSPENDIDO, EstadoPetar.AUTORIZADO);
        petar.setEstado(EstadoPetar.SUSPENDIDO);
        return toDto(petar);
    }

    @Override
    @Transactional
    public SomaPetarDTO reanudar(Long proyectoId, Long petarId) {
        exigirProyecto(proyectoId);
        SomaPetar petar = exigirPetar(proyectoId, petarId);
        exigirTransicion(petar.getEstado(), EstadoPetar.AUTORIZADO, EstadoPetar.SUSPENDIDO);
        petar.setEstado(EstadoPetar.AUTORIZADO);
        return toDto(petar);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SomaPetarChecklistItemDTO> checklist(TipoTrabajoPetar tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de trabajo es obligatorio");
        }
        return ChecklistPetar.itemsPara(tipo).stream()
                .map(clave -> SomaPetarChecklistItemDTO.builder()
                        .clave(clave)
                        .etiqueta(ChecklistPetar.etiqueta(clave))
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SomaPetarFaltanteTareoDTO> faltantesTareo(Long proyectoId) {
        exigirProyecto(proyectoId);
        List<PeriodoCc> periodos = periodoCcRepository.findByProyectoIdAndEstado(proyectoId, EstadoPeriodoCc.BORRADOR);
        if (periodos.isEmpty()) {
            return List.of();
        }
        List<Long> periodoIds = periodos.stream().map(PeriodoCc::getId).toList();
        List<TareoCcDia> dias = tareoCcDiaRepository.findByPeriodoIdIn(periodoIds);
        Set<LocalDate> fechasAltura = new HashSet<>();
        Set<LocalDate> fechasZanja = new HashSet<>();
        for (TareoCcDia dia : dias) {
            if (dia == null || dia.getFecha() == null) {
                continue;
            }
            if (dia.isAltura()) {
                fechasAltura.add(dia.getFecha());
            }
            if (dia.isCotaCero()) {
                fechasZanja.add(dia.getFecha());
            }
        }
        Set<LocalDate> cubiertasAltura = fechasCubiertas(proyectoId, TipoTrabajoPetar.ALTURA);
        Set<LocalDate> cubiertasZanja = fechasCubiertas(proyectoId, TipoTrabajoPetar.EXCAVACION_ZANJA);
        List<SomaPetarFaltanteTareoDTO> faltantes = new ArrayList<>();
        for (LocalDate fecha : fechasAltura) {
            if (!cubiertasAltura.contains(fecha)) {
                faltantes.add(SomaPetarFaltanteTareoDTO.builder()
                        .fecha(fecha)
                        .tipoTrabajo(TipoTrabajoPetar.ALTURA)
                        .build());
            }
        }
        for (LocalDate fecha : fechasZanja) {
            if (!cubiertasZanja.contains(fecha)) {
                faltantes.add(SomaPetarFaltanteTareoDTO.builder()
                        .fecha(fecha)
                        .tipoTrabajo(TipoTrabajoPetar.EXCAVACION_ZANJA)
                        .build());
            }
        }
        faltantes.sort(Comparator.comparing(SomaPetarFaltanteTareoDTO::getFecha)
                .thenComparing(SomaPetarFaltanteTareoDTO::getTipoTrabajo));
        return faltantes;
    }

    private Set<LocalDate> fechasCubiertas(Long proyectoId, TipoTrabajoPetar tipo) {
        return somaPetarRepository
                .findByProyectoIdAndTipoTrabajoAndEstadoIn(proyectoId, tipo, ESTADOS_CUBRE_TAREO)
                .stream()
                .map(SomaPetar::getFecha)
                .collect(Collectors.toSet());
    }

    private void aplicarCabecera(SomaPetar petar, Long proyectoId, SomaPetarRequest request) {
        petar.setTipoTrabajo(request.getTipoTrabajo());
        petar.setPartida(resolverPartida(proyectoId, request.getPartidaId()));
        petar.setCuadrilla(resolverCuadrilla(proyectoId, request.getCuadrillaId()));
        petar.setFecha(request.getFecha());
        petar.setHoraInicio(request.getHoraInicio());
        petar.setHoraFin(request.getHoraFin());
        petar.setUbicacionFrente(
                exigirTexto(request.getUbicacionFrente(), "La ubicación / frente es obligatorio", 200));
        petar.setDescripcionTrabajo(
                exigirTexto(request.getDescripcionTrabajo(), "La descripción del trabajo es obligatoria", 1000));
        petar.setChecklist(copiarChecklist(request.getChecklist()));
        petar.setMediciones(copiarMediciones(request.getMediciones()));
        petar.setResidente(resolverResponsable(proyectoId, request.getResidentePersonaId(), RolResponsableObra.RESIDENTE));
        petar.setSupervisorSsoma(
                resolverResponsable(proyectoId, request.getSupervisorSsomaId(), RolResponsableObra.SUPERVISOR_SSOMA));
        petar.setDocumento(resolverDocumento(proyectoId, request.getDocumentoObraId()));
        reemplazarTrabajadores(petar, request.getTrabajadores());
    }

    private void validarCabecera(SomaPetarRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El PETAR es obligatorio");
        }
        if (request.getTipoTrabajo() == null) {
            throw new IllegalArgumentException("El tipo de trabajo es obligatorio");
        }
        if (request.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (request.getHoraInicio() == null) {
            throw new IllegalArgumentException("La hora de inicio es obligatoria");
        }
        if (request.getHoraFin() == null) {
            throw new IllegalArgumentException("La hora de fin es obligatoria");
        }
        if (!request.getHoraFin().isAfter(request.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la de inicio");
        }
    }

    private void validarAutorizacion(Long proyectoId, SomaPetar petar) {
        List<SomaPetarTrabajador> trabajadores =
                petar.getTrabajadores() == null ? List.of() : petar.getTrabajadores();
        if (trabajadores.isEmpty()) {
            throw new IllegalArgumentException("El PETAR debe tener al menos un trabajador");
        }
        Map<Long, EstadoHabilitacionSoma> semaforo = mapaHabilitacion(proyectoId, petar.getFecha());
        for (SomaPetarTrabajador fila : trabajadores) {
            Persona persona = fila.getPersona();
            Long personaId = persona == null ? null : persona.getId();
            if (semaforo.get(personaId) != EstadoHabilitacionSoma.HABILITADO) {
                String nombre = nombreCompleto(persona);
                throw new IllegalArgumentException(
                        "Todos los trabajadores deben estar HABILITADO en SOMA"
                                + (nombre == null ? "" : ": " + nombre));
            }
            if (petar.getTipoTrabajo() == TipoTrabajoPetar.ALTURA && restringidoEnAltura(personaId)) {
                throw new IllegalArgumentException(
                        "Un trabajador tiene restricción de altura en el último EMO");
            }
        }
        if (petar.getResidente() == null) {
            throw new IllegalArgumentException("El residente es obligatorio para autorizar");
        }
        if (petar.getSupervisorSsoma() == null) {
            throw new IllegalArgumentException("El supervisor SSOMA es obligatorio para autorizar");
        }
        Map<String, Boolean> checklist = petar.getChecklist() == null ? Map.of() : petar.getChecklist();
        for (String clave : ChecklistPetar.itemsPara(petar.getTipoTrabajo())) {
            if (!Boolean.TRUE.equals(checklist.get(clave))) {
                throw new IllegalArgumentException("El checklist del tipo debe estar completo");
            }
        }
        exigirMediciones(petar.getTipoTrabajo(), petar.getMediciones());
    }

    private boolean restringidoEnAltura(Long personaId) {
        if (personaId == null) {
            return false;
        }
        SomaEmoExamen ultimo = somaEmoExamenRepository
                .findFirstByPersonaIdOrderByFechaExamenDescIdDesc(personaId)
                .orElse(null);
        if (ultimo == null || ultimo.getAptitud() != AptitudEmo.APTO_CON_RESTRICCION) {
            return false;
        }
        return sinTildesMinusculas(ultimo.getRestricciones()).contains("altura");
    }

    private static void exigirMediciones(TipoTrabajoPetar tipo, Map<String, BigDecimal> mediciones) {
        Map<String, BigDecimal> valores = mediciones == null ? Map.of() : mediciones;
        if (tipo == TipoTrabajoPetar.ESPACIO_CONFINADO) {
            if (valores.get("oxigenoPct") == null || valores.get("lelPct") == null) {
                throw new IllegalArgumentException("Espacio confinado exige oxigenoPct y lelPct");
            }
        }
        if (tipo == TipoTrabajoPetar.ALTURA || tipo == TipoTrabajoPetar.IZAJE_CARGAS) {
            if (valores.get("vientoKmh") == null) {
                throw new IllegalArgumentException("Este tipo de PETAR exige vientoKmh");
            }
        }
    }

    private static void exigirTransicion(EstadoPetar actual, EstadoPetar destino, EstadoPetar... origenes) {
        for (EstadoPetar origen : origenes) {
            if (actual == origen) {
                return;
            }
        }
        throw new IllegalArgumentException("Transición no permitida: " + actual + " → " + destino);
    }

    private void reemplazarTrabajadores(SomaPetar petar, List<SomaPetarTrabajadorRequest> filas) {
        petar.clearTrabajadores();
        if (filas == null || filas.isEmpty()) {
            return;
        }
        Set<Long> vistos = new HashSet<>();
        for (SomaPetarTrabajadorRequest fila : filas) {
            if (fila == null || fila.getPersonaId() == null || !vistos.add(fila.getPersonaId())) {
                continue;
            }
            SomaPetarTrabajador trabajador = new SomaPetarTrabajador();
            trabajador.setPersona(exigirPersona(fila.getPersonaId()));
            String rol = blankToNull(fila.getRolEnTarea());
            trabajador.setRolEnTarea(rol == null ? "EJECUTANTE" : limitar(rol, 60, "El rol en la tarea"));
            petar.addTrabajador(trabajador);
        }
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

    private Persona resolverResponsable(Long proyectoId, Long personaId, RolResponsableObra rol) {
        if (personaId != null) {
            return exigirPersona(personaId);
        }
        return proyectoResponsableRepository.findByProyectoIdOrderByRolAsc(proyectoId).stream()
                .filter(fila -> fila.getRol() == rol)
                .map(ProyectoResponsable::getPersona)
                .findFirst()
                .orElse(null);
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

    private DocumentoObra resolverDocumento(Long proyectoId, Long documentoObraId) {
        if (documentoObraId == null) {
            return null;
        }
        return documentoObraRepository
                .findByIdAndProyectoId(documentoObraId, proyectoId)
                .orElseThrow(() -> new DocumentoObraNoEncontradoException("No se encontró el documento de obra"));
    }

    private SomaPetar exigirPetar(Long proyectoId, Long petarId) {
        if (petarId == null) {
            throw new IllegalArgumentException("No se encontró el PETAR");
        }
        return somaPetarRepository
                .findByIdAndProyectoId(petarId, proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el PETAR"));
    }

    private Proyecto exigirProyecto(Long proyectoId) {
        return proyectoRepository
                .findById(proyectoId)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private static Map<String, Boolean> copiarChecklist(Map<String, Boolean> origen) {
        Map<String, Boolean> destino = new LinkedHashMap<>();
        if (origen == null) {
            return destino;
        }
        origen.forEach((clave, valor) -> {
            if (clave != null && valor != null) {
                destino.put(clave, valor);
            }
        });
        return destino;
    }

    private static Map<String, BigDecimal> copiarMediciones(Map<String, BigDecimal> origen) {
        Map<String, BigDecimal> destino = new LinkedHashMap<>();
        if (origen == null) {
            return destino;
        }
        origen.forEach((clave, valor) -> {
            if (clave != null && valor != null) {
                destino.put(clave, valor);
            }
        });
        return destino;
    }

    private static SomaPetarDTO toDto(SomaPetar petar) {
        PartidaPresupuesto partida = petar.getPartida();
        CuadrillaObra cuadrilla = petar.getCuadrilla();
        Persona residente = petar.getResidente();
        Persona supervisor = petar.getSupervisorSsoma();
        DocumentoObra documento = petar.getDocumento();
        List<SomaPetarTrabajadorDTO> trabajadores = petar.getTrabajadores() == null
                ? List.of()
                : petar.getTrabajadores().stream().map(SomaPetarServiceImpl::toTrabajadorDto).toList();
        return SomaPetarDTO.builder()
                .id(petar.getId())
                .proyectoId(petar.getProyecto() == null ? null : petar.getProyecto().getId())
                .numero(petar.getNumero())
                .tipoTrabajo(petar.getTipoTrabajo())
                .partidaId(partida == null ? null : partida.getId())
                .partidaCodigo(partida == null ? null : partida.getCodigo())
                .partidaDescripcion(partida == null ? null : partida.getDescripcion())
                .cuadrillaId(cuadrilla == null ? null : cuadrilla.getId())
                .cuadrillaNombre(cuadrilla == null ? null : cuadrilla.getNombre())
                .fecha(petar.getFecha())
                .horaInicio(petar.getHoraInicio())
                .horaFin(petar.getHoraFin())
                .ubicacionFrente(petar.getUbicacionFrente())
                .descripcionTrabajo(petar.getDescripcionTrabajo())
                .checklist(petar.getChecklist() == null ? Map.of() : Map.copyOf(petar.getChecklist()))
                .mediciones(petar.getMediciones() == null ? Map.of() : Map.copyOf(petar.getMediciones()))
                .residentePersonaId(residente == null ? null : residente.getId())
                .residenteNombre(nombreCompleto(residente))
                .supervisorSsomaId(supervisor == null ? null : supervisor.getId())
                .supervisorSsomaNombre(nombreCompleto(supervisor))
                .estado(petar.getEstado())
                .autorizadoPor(petar.getAutorizadoPor())
                .autorizadoAt(petar.getAutorizadoAt())
                .documentoObraId(documento == null ? null : documento.getId())
                .createdAt(petar.getCreatedAt())
                .trabajadores(trabajadores)
                .build();
    }

    private static SomaPetarTrabajadorDTO toTrabajadorDto(SomaPetarTrabajador fila) {
        Persona persona = fila.getPersona();
        return SomaPetarTrabajadorDTO.builder()
                .id(fila.getId())
                .personaId(persona == null ? null : persona.getId())
                .personaNombre(nombreCompleto(persona))
                .numeroDocumento(persona == null ? null : persona.getNumeroDocumento())
                .rolEnTarea(fila.getRolEnTarea())
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

    private static String sinTildesMinusculas(String valor) {
        if (valor == null) {
            return "";
        }
        String nfd = Normalizer.normalize(valor, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
}
