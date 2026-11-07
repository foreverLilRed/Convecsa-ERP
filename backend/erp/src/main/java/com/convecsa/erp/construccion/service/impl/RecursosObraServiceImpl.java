package com.convecsa.erp.construccion.service.impl;

import com.convecsa.erp.common.exception.AsignacionEquipoNoEncontradaException;
import com.convecsa.erp.common.exception.CuadrillaObraNoEncontradaException;
import com.convecsa.erp.common.exception.ImputacionEquipoNoEncontradaException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.common.exception.SubcontratoAvanceNoEncontradoException;
import com.convecsa.erp.common.exception.SubcontratoObraNoEncontradoException;
import com.convecsa.erp.construccion.domain.CategoriaObrera;
import com.convecsa.erp.construccion.domain.EstadoVersionPresupuesto;
import com.convecsa.erp.construccion.domain.ModoTarifaEquipo;
import com.convecsa.erp.construccion.domain.OrigenEquipo;
import com.convecsa.erp.construccion.dto.AsignacionEquipoDTO;
import com.convecsa.erp.construccion.dto.AsignacionEquipoRequest;
import com.convecsa.erp.construccion.dto.CuadrillaMiembroDTO;
import com.convecsa.erp.construccion.dto.CuadrillaMiembroRequest;
import com.convecsa.erp.construccion.dto.CuadrillaObraDTO;
import com.convecsa.erp.construccion.dto.CuadrillaObraRequest;
import com.convecsa.erp.construccion.dto.ImputacionEquipoDTO;
import com.convecsa.erp.construccion.dto.ImputacionEquipoRequest;
import com.convecsa.erp.construccion.dto.SubcontratoAvanceDTO;
import com.convecsa.erp.construccion.dto.SubcontratoAvanceRequest;
import com.convecsa.erp.construccion.dto.SubcontratoObraDTO;
import com.convecsa.erp.construccion.dto.SubcontratoObraRequest;
import com.convecsa.erp.construccion.entity.AsignacionEquipo;
import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.entity.CuadrillaMiembro;
import com.convecsa.erp.construccion.entity.CuadrillaObra;
import com.convecsa.erp.construccion.entity.ImputacionEquipo;
import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.PresupuestoVersion;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.SubcontratoAvance;
import com.convecsa.erp.construccion.entity.SubcontratoObra;
import com.convecsa.erp.construccion.repository.AsignacionEquipoRepository;
import com.convecsa.erp.construccion.repository.ContratoCcRepository;
import com.convecsa.erp.construccion.repository.CuadrillaMiembroRepository;
import com.convecsa.erp.construccion.repository.CuadrillaObraRepository;
import com.convecsa.erp.construccion.repository.ImputacionEquipoRepository;
import com.convecsa.erp.construccion.repository.PartidaPresupuestoRepository;
import com.convecsa.erp.construccion.repository.PresupuestoVersionRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.SubcontratoAvanceRepository;
import com.convecsa.erp.construccion.repository.SubcontratoObraRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.construccion.service.InterfazRecursosObraService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecursosObraServiceImpl implements InterfazRecursosObraService {

    private final ProyectoRepository proyectoRepository;
    private final AsignacionEquipoRepository asignacionEquipoRepository;
    private final ImputacionEquipoRepository imputacionEquipoRepository;
    private final SubcontratoObraRepository subcontratoObraRepository;
    private final SubcontratoAvanceRepository subcontratoAvanceRepository;
    private final PresupuestoVersionRepository presupuestoVersionRepository;
    private final PartidaPresupuestoRepository partidaPresupuestoRepository;
    private final ContratoCcRepository contratoCcRepository;
    private final CuadrillaObraRepository cuadrillaObraRepository;
    private final CuadrillaMiembroRepository cuadrillaMiembroRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionEquipoDTO> listarEquipos(Long proyectoId) {
        buscarProyecto(proyectoId);
        Map<Long, List<ImputacionEquipo>> porAsignacion = imputacionEquipoRepository.findByProyectoId(proyectoId)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getAsignacion().getId()));
        return asignacionEquipoRepository.findByProyectoIdOrderByFechaIngresoDescIdDesc(proyectoId).stream()
                .map(item -> toEquipoDto(item, porAsignacion.getOrDefault(item.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional
    public AsignacionEquipoDTO crearEquipo(Long proyectoId, AsignacionEquipoRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        AsignacionEquipo equipo = new AsignacionEquipo();
        equipo.setProyecto(proyecto);
        aplicarCabeceraEquipo(equipo, proyecto, request);
        asignacionEquipoRepository.save(equipo);
        return toEquipoDto(equipo, List.of());
    }

    @Override
    @Transactional
    public AsignacionEquipoDTO actualizarEquipo(Long proyectoId, Long equipoId, AsignacionEquipoRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        AsignacionEquipo equipo = buscarEquipo(proyectoId, equipoId);
        aplicarCabeceraEquipo(equipo, proyecto, request);
        asignacionEquipoRepository.save(equipo);
        return toEquipoDto(equipo, imputacionEquipoRepository.findByAsignacionIdOrderByFechaDescIdDesc(equipo.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImputacionEquipoDTO> listarImputaciones(Long proyectoId, Long equipoId) {
        buscarEquipo(proyectoId, equipoId);
        return imputacionEquipoRepository.findByAsignacionIdOrderByFechaDescIdDesc(equipoId).stream()
                .map(RecursosObraServiceImpl::toImputacionDto)
                .toList();
    }

    @Override
    @Transactional
    public ImputacionEquipoDTO registrarImputacion(Long proyectoId, Long equipoId, ImputacionEquipoRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        AsignacionEquipo equipo = buscarEquipo(proyectoId, equipoId);
        BigDecimal cantidad = qty(request.getCantidad());
        if (cantidad.signum() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        LocalDate fecha = request.getFecha() == null ? LocalDate.now() : request.getFecha();
        asegurarFechaImputacion(equipo, fecha);
        PartidaPresupuesto partida = resolverPartida(proyectoId, request.getPartidaId(), equipo.getPartida());
        BigDecimal tarifa = money(equipo.getTarifa());
        ImputacionEquipo imputacion = new ImputacionEquipo();
        imputacion.setAsignacion(equipo);
        imputacion.setProyecto(proyecto);
        imputacion.setFecha(fecha);
        imputacion.setCantidad(cantidad);
        imputacion.setTarifa(tarifa);
        imputacion.setCostoTotal(money(cantidad.multiply(tarifa)));
        imputacion.setPartida(partida);
        imputacion.setCentroCosto(centroDe(partida, proyecto, equipo.getCentroCosto()));
        imputacion.setObservaciones(blankToNull(request.getObservaciones()));
        imputacionEquipoRepository.save(imputacion);
        return toImputacionDto(imputacion);
    }

    @Override
    @Transactional
    public void eliminarImputacion(Long proyectoId, Long equipoId, Long imputacionId) {
        buscarEquipo(proyectoId, equipoId);
        ImputacionEquipo imputacion = imputacionEquipoRepository
                .findByIdAndAsignacionIdAndProyectoId(imputacionId, equipoId, proyectoId)
                .orElseThrow(() -> new ImputacionEquipoNoEncontradaException("No se encontró la imputación"));
        imputacionEquipoRepository.delete(imputacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubcontratoObraDTO> listarSubcontratos(Long proyectoId) {
        buscarProyecto(proyectoId);
        Map<Long, List<SubcontratoAvance>> porSubcontrato = subcontratoAvanceRepository.findByProyectoId(proyectoId)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getSubcontrato().getId()));
        return subcontratoObraRepository.findByProyectoIdOrderByIdDesc(proyectoId).stream()
                .map(item -> toSubcontratoDto(item, porSubcontrato.getOrDefault(item.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional
    public SubcontratoObraDTO crearSubcontrato(Long proyectoId, SubcontratoObraRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        SubcontratoObra subcontrato = new SubcontratoObra();
        subcontrato.setProyecto(proyecto);
        aplicarCabeceraSubcontrato(subcontrato, proyecto, request);
        subcontratoObraRepository.save(subcontrato);
        return toSubcontratoDto(subcontrato, List.of());
    }

    @Override
    @Transactional
    public SubcontratoObraDTO actualizarSubcontrato(
            Long proyectoId, Long subcontratoId, SubcontratoObraRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        SubcontratoObra subcontrato = buscarSubcontrato(proyectoId, subcontratoId);
        List<SubcontratoAvance> avances = subcontratoAvanceRepository
                .findBySubcontratoIdOrderByFechaDescIdDesc(subcontrato.getId());
        aplicarCabeceraSubcontrato(subcontrato, proyecto, request);
        BigDecimal avanzado = sumaAvances(avances);
        if (money(subcontrato.getMontoContratado()).compareTo(avanzado) < 0) {
            throw new IllegalArgumentException("El monto contratado no puede ser menor que el avance registrado");
        }
        subcontratoObraRepository.save(subcontrato);
        return toSubcontratoDto(subcontrato, avances);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubcontratoAvanceDTO> listarAvances(Long proyectoId, Long subcontratoId) {
        buscarSubcontrato(proyectoId, subcontratoId);
        return subcontratoAvanceRepository.findBySubcontratoIdOrderByFechaDescIdDesc(subcontratoId).stream()
                .map(RecursosObraServiceImpl::toAvanceDto)
                .toList();
    }

    @Override
    @Transactional
    public SubcontratoAvanceDTO registrarAvance(
            Long proyectoId, Long subcontratoId, SubcontratoAvanceRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        SubcontratoObra subcontrato = buscarSubcontrato(proyectoId, subcontratoId);
        BigDecimal monto = money(request.getMonto());
        if (monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto del avance debe ser mayor que cero");
        }
        BigDecimal avanzado = sumaAvances(
                subcontratoAvanceRepository.findBySubcontratoIdOrderByFechaDescIdDesc(subcontrato.getId()));
        BigDecimal saldo = money(subcontrato.getMontoContratado()).subtract(avanzado);
        if (monto.compareTo(saldo) > 0) {
            throw new IllegalArgumentException("El avance no puede superar el saldo del subcontrato");
        }
        PartidaPresupuesto partida = resolverPartida(proyectoId, request.getPartidaId(), subcontrato.getPartida());
        SubcontratoAvance avance = new SubcontratoAvance();
        avance.setSubcontrato(subcontrato);
        avance.setProyecto(proyecto);
        avance.setFecha(request.getFecha() == null ? LocalDate.now() : request.getFecha());
        avance.setMonto(monto);
        avance.setPartida(partida);
        avance.setCentroCosto(centroDe(partida, proyecto, subcontrato.getCentroCosto()));
        avance.setObservaciones(blankToNull(request.getObservaciones()));
        subcontratoAvanceRepository.save(avance);
        return toAvanceDto(avance);
    }

    @Override
    @Transactional
    public void eliminarAvance(Long proyectoId, Long subcontratoId, Long avanceId) {
        buscarSubcontrato(proyectoId, subcontratoId);
        SubcontratoAvance avance = subcontratoAvanceRepository
                .findByIdAndSubcontratoIdAndProyectoId(avanceId, subcontratoId, proyectoId)
                .orElseThrow(() -> new SubcontratoAvanceNoEncontradoException("No se encontró el avance"));
        subcontratoAvanceRepository.delete(avance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuadrillaObraDTO> listarCuadrillas(Long proyectoId) {
        buscarProyecto(proyectoId);
        return armarCuadrillas(proyectoId);
    }

    @Override
    @Transactional
    public CuadrillaObraDTO crearCuadrilla(Long proyectoId, CuadrillaObraRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        CuadrillaObra cuadrilla = new CuadrillaObra();
        cuadrilla.setProyecto(proyecto);
        aplicarCabeceraCuadrilla(cuadrilla, proyectoId, request, null);
        cuadrillaObraRepository.save(cuadrilla);
        return toCuadrillaDto(cuadrilla, List.of(), Map.of());
    }

    @Override
    @Transactional
    public CuadrillaObraDTO actualizarCuadrilla(Long proyectoId, Long cuadrillaId, CuadrillaObraRequest request) {
        buscarProyecto(proyectoId);
        CuadrillaObra cuadrilla = buscarCuadrilla(proyectoId, cuadrillaId);
        aplicarCabeceraCuadrilla(cuadrilla, proyectoId, request, cuadrillaId);
        cuadrillaObraRepository.save(cuadrilla);
        return dtoDeCuadrilla(proyectoId, cuadrilla);
    }

    @Override
    @Transactional
    public void eliminarCuadrilla(Long proyectoId, Long cuadrillaId) {
        CuadrillaObra cuadrilla = buscarCuadrilla(proyectoId, cuadrillaId);
        cuadrillaObraRepository.delete(cuadrilla);
    }

    @Override
    @Transactional
    public CuadrillaObraDTO agregarMiembro(Long proyectoId, Long cuadrillaId, CuadrillaMiembroRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        CuadrillaObra cuadrilla = buscarCuadrilla(proyectoId, cuadrillaId);
        if (request == null || request.getPersonaId() == null) {
            throw new IllegalArgumentException("El obrero es obligatorio");
        }
        ContratoCc contratoCc = contratoActivo(proyectoId, request.getPersonaId());
        Persona persona = contratoCc.getContrato().getPersona();
        cuadrillaMiembroRepository.findByProyectoIdAndPersonaId(proyectoId, persona.getId()).ifPresent(actual -> {
            if (actual.getCuadrilla().getId().equals(cuadrilla.getId())) {
                throw new IllegalArgumentException("El obrero ya pertenece a esta cuadrilla");
            }
            cuadrillaMiembroRepository.delete(actual);
        });
        CuadrillaMiembro miembro = new CuadrillaMiembro();
        miembro.setCuadrilla(cuadrilla);
        miembro.setProyecto(proyecto);
        miembro.setPersona(persona);
        cuadrillaMiembroRepository.save(miembro);
        return dtoDeCuadrilla(proyectoId, cuadrilla);
    }

    @Override
    @Transactional
    public void quitarMiembro(Long proyectoId, Long cuadrillaId, Long personaId) {
        buscarCuadrilla(proyectoId, cuadrillaId);
        CuadrillaMiembro miembro = cuadrillaMiembroRepository
                .findByCuadrillaIdAndPersonaId(cuadrillaId, personaId)
                .orElseThrow(() -> new IllegalArgumentException("El obrero no pertenece a esta cuadrilla"));
        cuadrillaMiembroRepository.delete(miembro);
    }

    private void aplicarCabeceraCuadrilla(
            CuadrillaObra cuadrilla, Long proyectoId, CuadrillaObraRequest request, Long cuadrillaId) {
        String codigo = blankToNull(request.getCodigo());
        if (codigo == null) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
        String nombre = blankToNull(request.getNombre());
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        boolean duplicado = cuadrillaId == null
                ? cuadrillaObraRepository.existsByProyectoIdAndCodigoIgnoreCase(proyectoId, codigo)
                : cuadrillaObraRepository.existsByProyectoIdAndCodigoIgnoreCaseAndIdNot(proyectoId, codigo, cuadrillaId);
        if (duplicado) {
            throw new IllegalArgumentException("Ya existe una cuadrilla con ese código");
        }
        cuadrilla.setCodigo(codigo);
        cuadrilla.setNombre(nombre);
        cuadrilla.setCentroCosto(blankToNull(request.getCentroCosto()));
        cuadrilla.setObservaciones(blankToNull(request.getObservaciones()));
    }

    private ContratoCc contratoActivo(Long proyectoId, Long personaId) {
        return contratoCcRepository.findActivosByProyectoId(proyectoId).stream()
                .filter(item -> item.getContrato().getPersona().getId().equals(personaId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El obrero no tiene contrato activo en esta obra"));
    }

    private List<CuadrillaObraDTO> armarCuadrillas(Long proyectoId) {
        Map<Long, CategoriaObrera> categoriaPorPersona = categoriaPorPersona(proyectoId);
        Map<Long, List<CuadrillaMiembro>> porCuadrilla = cuadrillaMiembroRepository.findByProyectoId(proyectoId)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getCuadrilla().getId()));
        return cuadrillaObraRepository.findByProyectoIdOrderByCodigoAsc(proyectoId).stream()
                .map(item -> toCuadrillaDto(
                        item, porCuadrilla.getOrDefault(item.getId(), List.of()), categoriaPorPersona))
                .toList();
    }

    private CuadrillaObraDTO dtoDeCuadrilla(Long proyectoId, CuadrillaObra cuadrilla) {
        Map<Long, CategoriaObrera> categoriaPorPersona = categoriaPorPersona(proyectoId);
        List<CuadrillaMiembro> miembros = cuadrillaMiembroRepository.findByProyectoId(proyectoId).stream()
                .filter(item -> item.getCuadrilla().getId().equals(cuadrilla.getId()))
                .toList();
        return toCuadrillaDto(cuadrilla, miembros, categoriaPorPersona);
    }

    private Map<Long, CategoriaObrera> categoriaPorPersona(Long proyectoId) {
        return contratoCcRepository.findActivosByProyectoId(proyectoId).stream()
                .collect(Collectors.toMap(
                        item -> item.getContrato().getPersona().getId(),
                        ContratoCc::getCategoria,
                        (primero, segundo) -> primero));
    }

    private CuadrillaObra buscarCuadrilla(Long proyectoId, Long cuadrillaId) {
        buscarProyecto(proyectoId);
        return cuadrillaObraRepository.findByIdAndProyectoId(cuadrillaId, proyectoId)
                .orElseThrow(() -> new CuadrillaObraNoEncontradaException("No se encontró la cuadrilla"));
    }

    private static CuadrillaObraDTO toCuadrillaDto(
            CuadrillaObra cuadrilla,
            List<CuadrillaMiembro> miembros,
            Map<Long, CategoriaObrera> categoriaPorPersona) {
        List<CuadrillaMiembroDTO> miembrosDto = miembros.stream()
                .map(item -> {
                    Persona persona = item.getPersona();
                    return CuadrillaMiembroDTO.builder()
                            .personaId(persona.getId())
                            .nombres(persona.getNombres())
                            .apellidos(persona.getApellidos())
                            .numeroDocumento(persona.getNumeroDocumento())
                            .categoria(categoriaPorPersona.get(persona.getId()))
                            .build();
                })
                .toList();
        return CuadrillaObraDTO.builder()
                .id(cuadrilla.getId())
                .proyectoId(cuadrilla.getProyecto().getId())
                .codigo(cuadrilla.getCodigo())
                .nombre(cuadrilla.getNombre())
                .centroCosto(cuadrilla.getCentroCosto())
                .observaciones(cuadrilla.getObservaciones())
                .createdAt(cuadrilla.getCreatedAt())
                .miembros(miembrosDto)
                .build();
    }

    private void aplicarCabeceraEquipo(AsignacionEquipo equipo, Proyecto proyecto, AsignacionEquipoRequest request) {
        String codigo = blankToNull(request.getCodigo());
        if (codigo == null) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
        String descripcion = blankToNull(request.getDescripcion());
        if (descripcion == null) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        OrigenEquipo origen = request.getOrigen() == null ? OrigenEquipo.PROPIO : request.getOrigen();
        String proveedorRazon = blankToNull(request.getProveedorRazon());
        if (origen == OrigenEquipo.ALQUILADO && proveedorRazon == null) {
            throw new IllegalArgumentException("El alquiler exige la razón social del proveedor");
        }
        LocalDate ingreso = request.getFechaIngreso() == null ? LocalDate.now() : request.getFechaIngreso();
        LocalDate salida = request.getFechaSalida();
        if (salida != null && salida.isBefore(ingreso)) {
            throw new IllegalArgumentException("La fecha de salida no puede ser anterior al ingreso");
        }
        BigDecimal tarifa = money(request.getTarifa());
        if (tarifa.signum() < 0) {
            throw new IllegalArgumentException("La tarifa no puede ser negativa");
        }
        PartidaPresupuesto partida = resolverPartida(proyecto.getId(), request.getPartidaId(), null);
        equipo.setCodigo(codigo);
        equipo.setDescripcion(descripcion);
        equipo.setOrigen(origen);
        equipo.setProveedorRazon(origen == OrigenEquipo.ALQUILADO ? proveedorRazon : null);
        equipo.setProveedorRuc(origen == OrigenEquipo.ALQUILADO ? normalizarRuc(request.getProveedorRuc()) : null);
        equipo.setFechaIngreso(ingreso);
        equipo.setFechaSalida(salida);
        equipo.setModoTarifa(request.getModoTarifa() == null ? ModoTarifaEquipo.DIA : request.getModoTarifa());
        equipo.setTarifa(tarifa);
        equipo.setPartida(partida);
        equipo.setCentroCosto(centroDe(partida, proyecto, null));
        equipo.setObservaciones(blankToNull(request.getObservaciones()));
    }

    private void aplicarCabeceraSubcontrato(
            SubcontratoObra subcontrato, Proyecto proyecto, SubcontratoObraRequest request) {
        String especialidad = blankToNull(request.getEspecialidad());
        if (especialidad == null) {
            throw new IllegalArgumentException("La especialidad es obligatoria");
        }
        String razon = blankToNull(request.getSubcontratistaRazon());
        if (razon == null) {
            throw new IllegalArgumentException("La razón social del subcontratista es obligatoria");
        }
        BigDecimal monto = money(request.getMontoContratado());
        if (monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto contratado debe ser mayor que cero");
        }
        LocalDate inicio = request.getFechaInicio();
        LocalDate fin = request.getFechaFin();
        if (inicio != null && fin != null && fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior al inicio");
        }
        PartidaPresupuesto partida = resolverPartida(proyecto.getId(), request.getPartidaId(), null);
        subcontrato.setEspecialidad(especialidad);
        subcontrato.setSubcontratistaRazon(razon);
        subcontrato.setSubcontratistaRuc(normalizarRuc(request.getSubcontratistaRuc()));
        subcontrato.setMontoContratado(monto);
        subcontrato.setFechaInicio(inicio);
        subcontrato.setFechaFin(fin);
        subcontrato.setPartida(partida);
        subcontrato.setCentroCosto(centroDe(partida, proyecto, null));
        subcontrato.setObservaciones(blankToNull(request.getObservaciones()));
    }

    private PartidaPresupuesto resolverPartida(Long proyectoId, Long partidaId, PartidaPresupuesto fallback) {
        if (partidaId == null) {
            return fallback;
        }
        return buscarPartidaVigente(proyectoId, partidaId);
    }

    private PartidaPresupuesto buscarPartidaVigente(Long proyectoId, Long partidaId) {
        PresupuestoVersion vigente = presupuestoVersionRepository
                .findByProyectoIdAndEstado(proyectoId, EstadoVersionPresupuesto.VIGENTE)
                .orElseThrow(() -> new IllegalArgumentException("La obra no tiene presupuesto vigente"));
        PartidaPresupuesto partida = partidaPresupuestoRepository.findById(partidaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la partida"));
        if (!partida.getVersion().getId().equals(vigente.getId())) {
            throw new IllegalArgumentException("La partida no pertenece al presupuesto vigente");
        }
        return partida;
    }

    private static void asegurarFechaImputacion(AsignacionEquipo equipo, LocalDate fecha) {
        if (fecha.isBefore(equipo.getFechaIngreso())) {
            throw new IllegalArgumentException("La fecha de imputación no puede ser anterior al ingreso del equipo");
        }
        if (equipo.getFechaSalida() != null && fecha.isAfter(equipo.getFechaSalida())) {
            throw new IllegalArgumentException("La fecha de imputación no puede ser posterior a la salida del equipo");
        }
    }

    private Proyecto buscarProyecto(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private AsignacionEquipo buscarEquipo(Long proyectoId, Long equipoId) {
        buscarProyecto(proyectoId);
        return asignacionEquipoRepository.findByIdAndProyectoId(equipoId, proyectoId)
                .orElseThrow(() -> new AsignacionEquipoNoEncontradaException("No se encontró la asignación de equipo"));
    }

    private SubcontratoObra buscarSubcontrato(Long proyectoId, Long subcontratoId) {
        buscarProyecto(proyectoId);
        return subcontratoObraRepository.findByIdAndProyectoId(subcontratoId, proyectoId)
                .orElseThrow(() -> new SubcontratoObraNoEncontradoException("No se encontró el subcontrato"));
    }

    private static AsignacionEquipoDTO toEquipoDto(AsignacionEquipo equipo, List<ImputacionEquipo> imputaciones) {
        PartidaPresupuesto partida = equipo.getPartida();
        BigDecimal costo = imputaciones.stream()
                .map(item -> nz(item.getCostoTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cantidad = imputaciones.stream()
                .map(item -> nz(item.getCantidad()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return AsignacionEquipoDTO.builder()
                .id(equipo.getId())
                .proyectoId(equipo.getProyecto().getId())
                .codigo(equipo.getCodigo())
                .descripcion(equipo.getDescripcion())
                .origen(equipo.getOrigen())
                .proveedorRazon(equipo.getProveedorRazon())
                .proveedorRuc(equipo.getProveedorRuc())
                .fechaIngreso(equipo.getFechaIngreso())
                .fechaSalida(equipo.getFechaSalida())
                .modoTarifa(equipo.getModoTarifa())
                .tarifa(money(equipo.getTarifa()))
                .partidaId(partida == null ? null : partida.getId())
                .codigoPartida(partida == null ? null : partida.getCodigo())
                .descripcionPartida(partida == null ? null : partida.getDescripcion())
                .centroCosto(equipo.getCentroCosto())
                .observaciones(equipo.getObservaciones())
                .createdAt(equipo.getCreatedAt())
                .costoImputado(money(costo))
                .cantidadImputada(qty(cantidad))
                .build();
    }

    private static ImputacionEquipoDTO toImputacionDto(ImputacionEquipo imputacion) {
        PartidaPresupuesto partida = imputacion.getPartida();
        return ImputacionEquipoDTO.builder()
                .id(imputacion.getId())
                .asignacionId(imputacion.getAsignacion().getId())
                .proyectoId(imputacion.getProyecto().getId())
                .fecha(imputacion.getFecha())
                .cantidad(qty(imputacion.getCantidad()))
                .tarifa(money(imputacion.getTarifa()))
                .costoTotal(money(imputacion.getCostoTotal()))
                .partidaId(partida == null ? null : partida.getId())
                .codigoPartida(partida == null ? null : partida.getCodigo())
                .descripcionPartida(partida == null ? null : partida.getDescripcion())
                .centroCosto(imputacion.getCentroCosto())
                .observaciones(imputacion.getObservaciones())
                .createdAt(imputacion.getCreatedAt())
                .build();
    }

    private static SubcontratoObraDTO toSubcontratoDto(SubcontratoObra subcontrato, List<SubcontratoAvance> avances) {
        PartidaPresupuesto partida = subcontrato.getPartida();
        BigDecimal avanzado = sumaAvances(avances);
        BigDecimal contratado = money(subcontrato.getMontoContratado());
        return SubcontratoObraDTO.builder()
                .id(subcontrato.getId())
                .proyectoId(subcontrato.getProyecto().getId())
                .especialidad(subcontrato.getEspecialidad())
                .subcontratistaRazon(subcontrato.getSubcontratistaRazon())
                .subcontratistaRuc(subcontrato.getSubcontratistaRuc())
                .montoContratado(contratado)
                .fechaInicio(subcontrato.getFechaInicio())
                .fechaFin(subcontrato.getFechaFin())
                .partidaId(partida == null ? null : partida.getId())
                .codigoPartida(partida == null ? null : partida.getCodigo())
                .descripcionPartida(partida == null ? null : partida.getDescripcion())
                .centroCosto(subcontrato.getCentroCosto())
                .observaciones(subcontrato.getObservaciones())
                .createdAt(subcontrato.getCreatedAt())
                .montoAvanzado(avanzado)
                .saldo(money(contratado.subtract(avanzado)))
                .build();
    }

    private static SubcontratoAvanceDTO toAvanceDto(SubcontratoAvance avance) {
        PartidaPresupuesto partida = avance.getPartida();
        return SubcontratoAvanceDTO.builder()
                .id(avance.getId())
                .subcontratoId(avance.getSubcontrato().getId())
                .proyectoId(avance.getProyecto().getId())
                .fecha(avance.getFecha())
                .monto(money(avance.getMonto()))
                .partidaId(partida == null ? null : partida.getId())
                .codigoPartida(partida == null ? null : partida.getCodigo())
                .descripcionPartida(partida == null ? null : partida.getDescripcion())
                .centroCosto(avance.getCentroCosto())
                .observaciones(avance.getObservaciones())
                .createdAt(avance.getCreatedAt())
                .build();
    }

    private static BigDecimal sumaAvances(List<SubcontratoAvance> avances) {
        return money(avances.stream()
                .map(item -> nz(item.getMonto()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static String centroDe(PartidaPresupuesto partida, Proyecto proyecto, String fallback) {
        if (partida != null) {
            return centroDe(partida.getCentroCosto(), proyecto.getCentroCosto());
        }
        return centroDe(fallback, proyecto.getCentroCosto());
    }

    private static String centroDe(String propio, String fallback) {
        String valor = blankToNull(propio);
        if (valor != null) {
            return valor;
        }
        String obra = blankToNull(fallback);
        return obra == null ? "SIN_CENTRO" : obra;
    }

    private static String normalizarRuc(String ruc) {
        String valor = blankToNull(ruc);
        if (valor == null) {
            return null;
        }
        if (!valor.matches("\\d{11}")) {
            throw new IllegalArgumentException("El RUC debe tener 11 dígitos");
        }
        return valor;
    }

    private static String blankToNull(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static BigDecimal money(BigDecimal valor) {
        return nz(valor).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal qty(BigDecimal valor) {
        return nz(valor).setScale(4, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
