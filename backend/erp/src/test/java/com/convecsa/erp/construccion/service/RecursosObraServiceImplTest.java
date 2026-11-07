package com.convecsa.erp.construccion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;

import com.convecsa.erp.construccion.domain.CategoriaObrera;
import com.convecsa.erp.construccion.domain.ModoTarifaEquipo;
import com.convecsa.erp.construccion.domain.OrigenEquipo;
import com.convecsa.erp.construccion.dto.AsignacionEquipoDTO;
import com.convecsa.erp.construccion.dto.AsignacionEquipoRequest;
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
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.construccion.service.impl.RecursosObraServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecursosObraServiceImplTest {

    @Mock
    private ProyectoRepository proyectoRepository;
    @Mock
    private AsignacionEquipoRepository asignacionEquipoRepository;
    @Mock
    private ImputacionEquipoRepository imputacionEquipoRepository;
    @Mock
    private SubcontratoObraRepository subcontratoObraRepository;
    @Mock
    private SubcontratoAvanceRepository subcontratoAvanceRepository;
    @Mock
    private PresupuestoVersionRepository presupuestoVersionRepository;
    @Mock
    private PartidaPresupuestoRepository partidaPresupuestoRepository;
    @Mock
    private ContratoCcRepository contratoCcRepository;
    @Mock
    private CuadrillaObraRepository cuadrillaObraRepository;
    @Mock
    private CuadrillaMiembroRepository cuadrillaMiembroRepository;

    @InjectMocks
    private RecursosObraServiceImpl servicio;

    private final Map<Long, AsignacionEquipo> equipos = new LinkedHashMap<>();
    private final Map<Long, ImputacionEquipo> imputaciones = new LinkedHashMap<>();
    private final Map<Long, SubcontratoObra> subcontratos = new LinkedHashMap<>();
    private final Map<Long, SubcontratoAvance> avances = new LinkedHashMap<>();
    private final Map<Long, CuadrillaObra> cuadrillas = new LinkedHashMap<>();
    private final Map<Long, CuadrillaMiembro> miembros = new LinkedHashMap<>();
    private Proyecto proyecto;
    private ContratoCc contratoActivo;

    @BeforeEach
    void preparar() {
        proyecto = new Proyecto();
        proyecto.setId(7L);
        proyecto.setCodigo("OB001");
        proyecto.setCentroCosto("CC");

        lenient().when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        lenient().when(asignacionEquipoRepository.save(any(AsignacionEquipo.class))).thenAnswer(invocation -> {
            AsignacionEquipo item = invocation.getArgument(0);
            if (item.getId() == null) {
                item.setId((long) equipos.size() + 1);
            }
            equipos.put(item.getId(), item);
            return item;
        });
        lenient().when(asignacionEquipoRepository.findByIdAndProyectoId(any(), eq(7L)))
                .thenAnswer(invocation -> Optional.ofNullable(equipos.get(invocation.getArgument(0))));
        lenient().when(imputacionEquipoRepository.save(any(ImputacionEquipo.class))).thenAnswer(invocation -> {
            ImputacionEquipo item = invocation.getArgument(0);
            if (item.getId() == null) {
                item.setId((long) imputaciones.size() + 1);
            }
            imputaciones.put(item.getId(), item);
            return item;
        });
        lenient().when(imputacionEquipoRepository.findByAsignacionIdOrderByFechaDescIdDesc(any()))
                .thenAnswer(invocation -> {
                    Long asignacionId = invocation.getArgument(0);
                    return imputaciones.values().stream()
                            .filter(item -> item.getAsignacion().getId().equals(asignacionId))
                            .toList();
                });
        lenient().when(subcontratoObraRepository.save(any(SubcontratoObra.class))).thenAnswer(invocation -> {
            SubcontratoObra item = invocation.getArgument(0);
            if (item.getId() == null) {
                item.setId((long) subcontratos.size() + 1);
            }
            subcontratos.put(item.getId(), item);
            return item;
        });
        lenient().when(subcontratoObraRepository.findByIdAndProyectoId(any(), eq(7L)))
                .thenAnswer(invocation -> Optional.ofNullable(subcontratos.get(invocation.getArgument(0))));
        lenient().when(subcontratoAvanceRepository.save(any(SubcontratoAvance.class))).thenAnswer(invocation -> {
            SubcontratoAvance item = invocation.getArgument(0);
            if (item.getId() == null) {
                item.setId((long) avances.size() + 1);
            }
            avances.put(item.getId(), item);
            return item;
        });
        lenient().when(subcontratoAvanceRepository.findBySubcontratoIdOrderByFechaDescIdDesc(any()))
                .thenAnswer(invocation -> {
                    Long subcontratoId = invocation.getArgument(0);
                    return avances.values().stream()
                            .filter(item -> item.getSubcontrato().getId().equals(subcontratoId))
                            .toList();
                });

        Persona persona = new Persona();
        persona.setId(11L);
        persona.setNombres("Ana");
        persona.setApellidos("Rojas");
        persona.setNumeroDocumento("87654321");
        Contrato contrato = new Contrato();
        contrato.setId(22L);
        contrato.setPersona(persona);
        contrato.setActivo(true);
        contratoActivo = new ContratoCc();
        contratoActivo.setContrato(contrato);
        contratoActivo.setCategoria(CategoriaObrera.OPERARIO);
        lenient().when(contratoCcRepository.findActivosByProyectoId(7L)).thenReturn(List.of(contratoActivo));

        lenient().when(cuadrillaObraRepository.save(any(CuadrillaObra.class))).thenAnswer(invocation -> {
            CuadrillaObra item = invocation.getArgument(0);
            if (item.getId() == null) {
                item.setId((long) cuadrillas.size() + 1);
            }
            cuadrillas.put(item.getId(), item);
            return item;
        });
        lenient().when(cuadrillaObraRepository.findByIdAndProyectoId(any(), eq(7L)))
                .thenAnswer(invocation -> Optional.ofNullable(cuadrillas.get(invocation.getArgument(0))));
        lenient().when(cuadrillaObraRepository.findByProyectoIdOrderByCodigoAsc(7L))
                .thenAnswer(invocation -> cuadrillas.values().stream()
                        .sorted((a, b) -> a.getCodigo().compareToIgnoreCase(b.getCodigo()))
                        .toList());
        lenient().when(cuadrillaObraRepository.existsByProyectoIdAndCodigoIgnoreCase(eq(7L), any()))
                .thenAnswer(invocation -> {
                    String codigo = invocation.getArgument(1);
                    return cuadrillas.values().stream()
                            .anyMatch(item -> item.getCodigo().equalsIgnoreCase(codigo));
                });
        lenient().when(cuadrillaObraRepository.existsByProyectoIdAndCodigoIgnoreCaseAndIdNot(eq(7L), any(), any()))
                .thenAnswer(invocation -> {
                    String codigo = invocation.getArgument(1);
                    Long id = invocation.getArgument(2);
                    return cuadrillas.values().stream()
                            .anyMatch(item -> !item.getId().equals(id) && item.getCodigo().equalsIgnoreCase(codigo));
                });
        lenient().doAnswer(invocation -> {
            CuadrillaObra item = invocation.getArgument(0);
            cuadrillas.remove(item.getId());
            miembros.entrySet().removeIf(entry -> entry.getValue().getCuadrilla().getId().equals(item.getId()));
            return null;
        }).when(cuadrillaObraRepository).delete(any(CuadrillaObra.class));

        lenient().when(cuadrillaMiembroRepository.save(any(CuadrillaMiembro.class))).thenAnswer(invocation -> {
            CuadrillaMiembro item = invocation.getArgument(0);
            if (item.getId() == null) {
                item.setId((long) miembros.size() + 1);
            }
            miembros.put(item.getId(), item);
            return item;
        });
        lenient().when(cuadrillaMiembroRepository.findByProyectoId(7L))
                .thenAnswer(invocation -> List.copyOf(miembros.values()));
        lenient().when(cuadrillaMiembroRepository.findByProyectoIdAndPersonaId(eq(7L), any()))
                .thenAnswer(invocation -> {
                    Long personaId = invocation.getArgument(1);
                    return miembros.values().stream()
                            .filter(item -> item.getPersona().getId().equals(personaId))
                            .findFirst();
                });
        lenient().when(cuadrillaMiembroRepository.findByCuadrillaIdAndPersonaId(any(), any()))
                .thenAnswer(invocation -> {
                    Long cuadrillaId = invocation.getArgument(0);
                    Long personaId = invocation.getArgument(1);
                    return miembros.values().stream()
                            .filter(item -> item.getCuadrilla().getId().equals(cuadrillaId)
                                    && item.getPersona().getId().equals(personaId))
                            .findFirst();
                });
        lenient().doAnswer(invocation -> {
            CuadrillaMiembro item = invocation.getArgument(0);
            miembros.entrySet().removeIf(entry -> entry.getValue() == item
                    || (item.getId() != null && item.getId().equals(entry.getKey())));
            return null;
        }).when(cuadrillaMiembroRepository).delete(any(CuadrillaMiembro.class));
    }

    @Test
    void imputacionDiaMultiplicaTarifaPorCantidad() {
        AsignacionEquipoDTO equipo = servicio.crearEquipo(7L, equipoRequest(
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), "100.00"));
        ImputacionEquipoRequest request = new ImputacionEquipoRequest();
        request.setFecha(LocalDate.of(2026, 11, 3));
        request.setCantidad(new BigDecimal("2"));

        ImputacionEquipoDTO dto = servicio.registrarImputacion(7L, equipo.getId(), request);

        assertEquals(new BigDecimal("2.0000"), dto.getCantidad());
        assertEquals(new BigDecimal("100.00"), dto.getTarifa());
        assertEquals(new BigDecimal("200.00"), dto.getCostoTotal());
        assertEquals("CC", dto.getCentroCosto());
    }

    @Test
    void rechazaImputacionFueraDelRangoDeFechas() {
        AsignacionEquipoDTO equipo = servicio.crearEquipo(7L, equipoRequest(
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10), "100.00"));
        ImputacionEquipoRequest request = new ImputacionEquipoRequest();
        request.setFecha(LocalDate.of(2026, 11, 20));
        request.setCantidad(new BigDecimal("1"));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.registrarImputacion(7L, equipo.getId(), request));
        assertEquals("La fecha de imputación no puede ser posterior a la salida del equipo", error.getMessage());
    }

    @Test
    void avanceNoPuedeSuperarElSaldo() {
        SubcontratoObraDTO subcontrato = servicio.crearSubcontrato(7L, subcontratoRequest("100.00"));
        SubcontratoAvanceRequest primero = new SubcontratoAvanceRequest();
        primero.setFecha(LocalDate.of(2026, 11, 5));
        primero.setMonto(new BigDecimal("80.00"));
        SubcontratoAvanceDTO registrado = servicio.registrarAvance(7L, subcontrato.getId(), primero);
        assertEquals(new BigDecimal("80.00"), registrado.getMonto());

        SubcontratoAvanceRequest segundo = new SubcontratoAvanceRequest();
        segundo.setFecha(LocalDate.of(2026, 11, 6));
        segundo.setMonto(new BigDecimal("30.00"));
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.registrarAvance(7L, subcontrato.getId(), segundo));
        assertEquals("El avance no puede superar el saldo del subcontrato", error.getMessage());
    }

    @Test
    void crearCuadrillaPersisteCodigoYNombre() {
        CuadrillaObraDTO dto = servicio.crearCuadrilla(7L, cuadrillaRequest("EST", "Estructura"));

        assertEquals("EST", dto.getCodigo());
        assertEquals("Estructura", dto.getNombre());
        assertEquals("CC-EST", dto.getCentroCosto());
        assertEquals(0, dto.getMiembros().size());
    }

    @Test
    void rechazaCodigoDeCuadrillaDuplicado() {
        servicio.crearCuadrilla(7L, cuadrillaRequest("EST", "Estructura"));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.crearCuadrilla(7L, cuadrillaRequest("est", "Otra")));
        assertEquals("Ya existe una cuadrilla con ese código", error.getMessage());
    }

    @Test
    void rechazaMiembroSinContratoActivoEnLaObra() {
        CuadrillaObraDTO cuadrilla = servicio.crearCuadrilla(7L, cuadrillaRequest("EST", "Estructura"));
        CuadrillaMiembroRequest request = new CuadrillaMiembroRequest();
        request.setPersonaId(99L);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.agregarMiembro(7L, cuadrilla.getId(), request));
        assertEquals("El obrero no tiene contrato activo en esta obra", error.getMessage());
    }

    @Test
    void mueveMiembroEntreCuadrillasDeLaMismaObra() {
        CuadrillaObraDTO estructura = servicio.crearCuadrilla(7L, cuadrillaRequest("EST", "Estructura"));
        CuadrillaObraDTO acabados = servicio.crearCuadrilla(7L, cuadrillaRequest("ACA", "Acabados"));
        CuadrillaMiembroRequest request = new CuadrillaMiembroRequest();
        request.setPersonaId(11L);

        CuadrillaObraDTO enEstructura = servicio.agregarMiembro(7L, estructura.getId(), request);
        assertEquals(1, enEstructura.getMiembros().size());
        assertEquals(11L, enEstructura.getMiembros().get(0).getPersonaId());

        CuadrillaObraDTO enAcabados = servicio.agregarMiembro(7L, acabados.getId(), request);
        assertEquals(1, enAcabados.getMiembros().size());
        assertEquals(11L, enAcabados.getMiembros().get(0).getPersonaId());
        assertEquals(0, servicio.listarCuadrillas(7L).stream()
                .filter(item -> item.getId().equals(estructura.getId()))
                .findFirst()
                .orElseThrow()
                .getMiembros()
                .size());
    }

    private static CuadrillaObraRequest cuadrillaRequest(String codigo, String nombre) {
        CuadrillaObraRequest request = new CuadrillaObraRequest();
        request.setCodigo(codigo);
        request.setNombre(nombre);
        request.setCentroCosto("CC-EST");
        return request;
    }

    private static AsignacionEquipoRequest equipoRequest(LocalDate ingreso, LocalDate salida, String tarifa) {
        AsignacionEquipoRequest request = new AsignacionEquipoRequest();
        request.setCodigo("EQ-01");
        request.setDescripcion("Retroexcavadora");
        request.setOrigen(OrigenEquipo.PROPIO);
        request.setFechaIngreso(ingreso);
        request.setFechaSalida(salida);
        request.setModoTarifa(ModoTarifaEquipo.DIA);
        request.setTarifa(new BigDecimal(tarifa));
        return request;
    }

    private static SubcontratoObraRequest subcontratoRequest(String monto) {
        SubcontratoObraRequest request = new SubcontratoObraRequest();
        request.setEspecialidad("Instalaciones eléctricas");
        request.setSubcontratistaRazon("Electro Sur SAC");
        request.setMontoContratado(new BigDecimal(monto));
        request.setFechaInicio(LocalDate.of(2026, 11, 1));
        return request;
    }
}
