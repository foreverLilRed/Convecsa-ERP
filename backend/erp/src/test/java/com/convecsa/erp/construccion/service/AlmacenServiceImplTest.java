package com.convecsa.erp.construccion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.domain.EstadoOrdenCompra;
import com.convecsa.erp.construccion.domain.EstadoRequerimientoObra;
import com.convecsa.erp.construccion.domain.EstadoVersionPresupuesto;
import com.convecsa.erp.construccion.domain.TipoVersionPresupuesto;
import com.convecsa.erp.construccion.dto.AjusteAlmacenRequest;
import com.convecsa.erp.construccion.dto.AlmacenDTO;
import com.convecsa.erp.construccion.dto.CierreInventarioRequest;
import com.convecsa.erp.construccion.dto.DevolucionItemRequest;
import com.convecsa.erp.construccion.dto.DevolucionRequest;
import com.convecsa.erp.construccion.dto.MaterialRequest;
import com.convecsa.erp.construccion.dto.OrdenCompraDTO;
import com.convecsa.erp.construccion.dto.OrdenCompraItemRequest;
import com.convecsa.erp.construccion.dto.OrdenCompraRequest;
import com.convecsa.erp.construccion.dto.RecepcionItemRequest;
import com.convecsa.erp.construccion.dto.RecepcionRequest;
import com.convecsa.erp.construccion.dto.RequerimientoDTO;
import com.convecsa.erp.construccion.dto.RequerimientoItemRequest;
import com.convecsa.erp.construccion.dto.RequerimientoRequest;
import com.convecsa.erp.construccion.dto.StockItemDTO;
import com.convecsa.erp.construccion.dto.ValeSalidaDTO;
import com.convecsa.erp.construccion.dto.ValeSalidaItemRequest;
import com.convecsa.erp.construccion.dto.ValeSalidaRequest;
import com.convecsa.erp.construccion.entity.AlmacenObra;
import com.convecsa.erp.construccion.entity.Material;
import com.convecsa.erp.construccion.entity.MovimientoAlmacen;
import com.convecsa.erp.construccion.entity.OrdenCompra;
import com.convecsa.erp.construccion.entity.OrdenCompraItem;
import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.PresupuestoVersion;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.RecepcionAlmacen;
import com.convecsa.erp.construccion.entity.RequerimientoItem;
import com.convecsa.erp.construccion.entity.RequerimientoObra;
import com.convecsa.erp.construccion.entity.ValeSalida;
import com.convecsa.erp.construccion.repository.AlmacenObraRepository;
import com.convecsa.erp.construccion.repository.InventarioCierreRepository;
import com.convecsa.erp.construccion.repository.MaterialRepository;
import com.convecsa.erp.construccion.repository.MovimientoAlmacenRepository;
import com.convecsa.erp.construccion.repository.OrdenCompraRepository;
import com.convecsa.erp.construccion.repository.PartidaPresupuestoRepository;
import com.convecsa.erp.construccion.repository.PresupuestoVersionRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.RecepcionAlmacenRepository;
import com.convecsa.erp.construccion.repository.RequerimientoItemRepository;
import com.convecsa.erp.construccion.repository.RequerimientoObraRepository;
import com.convecsa.erp.construccion.repository.ValeSalidaRepository;
import com.convecsa.erp.construccion.service.impl.AlmacenServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
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
class AlmacenServiceImplTest {

    @Mock
    private ProyectoRepository proyectoRepository;
    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private AlmacenObraRepository almacenObraRepository;
    @Mock
    private RequerimientoObraRepository requerimientoObraRepository;
    @Mock
    private RequerimientoItemRepository requerimientoItemRepository;
    @Mock
    private OrdenCompraRepository ordenCompraRepository;
    @Mock
    private RecepcionAlmacenRepository recepcionAlmacenRepository;
    @Mock
    private ValeSalidaRepository valeSalidaRepository;
    @Mock
    private MovimientoAlmacenRepository movimientoAlmacenRepository;
    @Mock
    private InventarioCierreRepository inventarioCierreRepository;
    @Mock
    private PresupuestoVersionRepository presupuestoVersionRepository;
    @Mock
    private PartidaPresupuestoRepository partidaPresupuestoRepository;

    @InjectMocks
    private AlmacenServiceImpl servicio;

    private final Map<Long, Material> materiales = new LinkedHashMap<>();
    private final Map<Long, RequerimientoObra> requerimientos = new LinkedHashMap<>();
    private final Map<Long, RequerimientoItem> reqItems = new LinkedHashMap<>();
    private final Map<Long, OrdenCompra> ordenes = new LinkedHashMap<>();
    private final List<MovimientoAlmacen> movimientos = new ArrayList<>();
    private final List<ValeSalida> vales = new ArrayList<>();
    private Proyecto proyecto;
    private AlmacenObra almacen;
    private PresupuestoVersion vigente;
    private PartidaPresupuesto partida;

    @BeforeEach
    void preparar() {
        proyecto = new Proyecto();
        proyecto.setId(7L);
        proyecto.setCodigo("OB-01");
        proyecto.setCentroCosto("CC-OBRA");

        almacen = new AlmacenObra();
        almacen.setId(1L);
        almacen.setProyecto(proyecto);
        almacen.setNombre("Almacén OB-01");

        vigente = new PresupuestoVersion();
        vigente.setId(10L);
        vigente.setProyecto(proyecto);
        vigente.setTipo(TipoVersionPresupuesto.INICIAL);
        vigente.setEstado(EstadoVersionPresupuesto.VIGENTE);
        vigente.setNumero(1);

        partida = new PartidaPresupuesto();
        partida.setId(3L);
        partida.setVersion(vigente);
        partida.setCodigo("01.01");
        partida.setDescripcion("Muros");
        partida.setCentroCosto("CC-1");

        lenient().when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        lenient().when(almacenObraRepository.findByProyectoId(7L)).thenReturn(Optional.of(almacen));
        lenient().when(almacenObraRepository.save(any(AlmacenObra.class))).thenAnswer(invocation -> {
            AlmacenObra item = invocation.getArgument(0);
            if (item.getId() == null) {
                item.setId(1L);
            }
            almacen = item;
            return item;
        });
        lenient().when(materialRepository.findById(any())).thenAnswer(invocation ->
                Optional.ofNullable(materiales.get(invocation.getArgument(0))));
        lenient().when(materialRepository.existsByCodigoIgnoreCase(any())).thenAnswer(invocation ->
                materiales.values().stream().anyMatch(item -> item.getCodigo().equalsIgnoreCase(invocation.getArgument(0))));
        lenient().when(materialRepository.existsByCodigoIgnoreCaseAndIdNot(any(), any())).thenReturn(false);
        lenient().when(materialRepository.findAllByOrderByCodigoAsc()).thenAnswer(invocation -> List.copyOf(materiales.values()));
        lenient().when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> {
            Material material = invocation.getArgument(0);
            if (material.getId() == null) {
                material.setId((long) materiales.size() + 1);
            }
            materiales.put(material.getId(), material);
            return material;
        });
        lenient().when(presupuestoVersionRepository.findByProyectoIdAndEstado(7L, EstadoVersionPresupuesto.VIGENTE))
                .thenReturn(Optional.of(vigente));
        lenient().when(partidaPresupuestoRepository.findById(3L)).thenReturn(Optional.of(partida));
        lenient().when(requerimientoObraRepository.findFirstByProyectoIdOrderByNumeroDesc(7L))
                .thenAnswer(invocation -> requerimientos.values().stream().reduce((a, b) -> b));
        lenient().when(requerimientoObraRepository.findByIdAndProyectoId(any(), eq(7L)))
                .thenAnswer(invocation -> Optional.ofNullable(requerimientos.get(invocation.getArgument(0))));
        lenient().when(requerimientoObraRepository.save(any(RequerimientoObra.class))).thenAnswer(invocation -> {
            RequerimientoObra req = invocation.getArgument(0);
            if (req.getId() == null) {
                req.setId((long) requerimientos.size() + 1);
            }
            long itemId = 1;
            for (RequerimientoItem item : req.getItems()) {
                if (item.getId() == null) {
                    item.setId(itemId++);
                }
                reqItems.put(item.getId(), item);
            }
            requerimientos.put(req.getId(), req);
            return req;
        });
        lenient().when(requerimientoItemRepository.findById(any())).thenAnswer(invocation ->
                Optional.ofNullable(reqItems.get(invocation.getArgument(0))));
        lenient().when(ordenCompraRepository.findFirstByProyectoIdOrderByNumeroDesc(7L))
                .thenAnswer(invocation -> ordenes.values().stream().reduce((a, b) -> b));
        lenient().when(ordenCompraRepository.findByIdAndProyectoId(any(), eq(7L)))
                .thenAnswer(invocation -> Optional.ofNullable(ordenes.get(invocation.getArgument(0))));
        lenient().when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(invocation -> {
            OrdenCompra oc = invocation.getArgument(0);
            if (oc.getId() == null) {
                oc.setId((long) ordenes.size() + 1);
            }
            long itemId = 1;
            for (OrdenCompraItem item : oc.getItems()) {
                if (item.getId() == null) {
                    item.setId(itemId++);
                }
            }
            ordenes.put(oc.getId(), oc);
            return oc;
        });
        lenient().when(recepcionAlmacenRepository.save(any(RecepcionAlmacen.class))).thenAnswer(invocation -> {
            RecepcionAlmacen recepcion = invocation.getArgument(0);
            if (recepcion.getId() == null) {
                recepcion.setId(50L);
            }
            long itemId = 1;
            for (var item : recepcion.getItems()) {
                if (item.getId() == null) {
                    item.setId(itemId++);
                }
            }
            return recepcion;
        });
        lenient().when(recepcionAlmacenRepository.findByOrdenCompraIdOrderByIdDesc(any())).thenReturn(List.of());
        lenient().when(movimientoAlmacenRepository.save(any(MovimientoAlmacen.class))).thenAnswer(invocation -> {
            MovimientoAlmacen movimiento = invocation.getArgument(0);
            if (movimiento.getId() == null) {
                movimiento.setId((long) movimientos.size() + 1);
            }
            if (movimiento.getCreatedAt() == null) {
                movimiento.setCreatedAt(Instant.parse("2026-11-06T20:00:00Z"));
            }
            movimientos.add(movimiento);
            return movimiento;
        });
        lenient().when(movimientoAlmacenRepository.findFirstByAlmacenIdAndMaterialIdOrderByIdDesc(any(), any()))
                .thenAnswer(invocation -> {
                    Long almacenId = invocation.getArgument(0);
                    Long materialId = invocation.getArgument(1);
                    return movimientos.stream()
                            .filter(item -> item.getAlmacen().getId().equals(almacenId)
                                    && item.getMaterial().getId().equals(materialId))
                            .reduce((a, b) -> b);
                });
        lenient().when(movimientoAlmacenRepository.findByValeIdAndTipo(any(), any())).thenReturn(List.of());
        lenient().when(valeSalidaRepository.findFirstByAlmacenIdOrderByNumeroDesc(1L))
                .thenAnswer(invocation -> vales.stream().reduce((a, b) -> b));
        lenient().when(valeSalidaRepository.save(any(ValeSalida.class))).thenAnswer(invocation -> {
            ValeSalida vale = invocation.getArgument(0);
            if (vale.getId() == null) {
                vale.setId((long) vales.size() + 1);
                vales.add(vale);
            }
            return vale;
        });
        lenient().when(valeSalidaRepository.findByIdAndAlmacenProyectoId(any(), eq(7L)))
                .thenAnswer(invocation -> vales.stream()
                        .filter(item -> item.getId().equals(invocation.getArgument(0)))
                        .findFirst());
        lenient().when(inventarioCierreRepository.existsByAlmacenIdAndAnioAndMes(1L, 2026, 11)).thenReturn(false);
        lenient().when(inventarioCierreRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void costoPromedioPonderadoYValeImputanPartida() {
        Material material = crearMaterial("CEM", "5");
        OrdenCompraDTO primera = emitirYRecepcionar(material, "10", "2");
        assertEquals(EstadoOrdenCompra.CERRADA, primera.getEstado());
        emitirYRecepcionar(material, "10", "4");

        StockItemDTO stock = stockDe(material.getId());
        assertEquals(new BigDecimal("20.0000"), stock.getCantidad());
        assertEquals(new BigDecimal("3.00"), stock.getCostoPromedio());

        ValeSalidaRequest valeReq = new ValeSalidaRequest();
        valeReq.setPartidaId(3L);
        ValeSalidaItemRequest item = new ValeSalidaItemRequest();
        item.setMaterialId(material.getId());
        item.setCantidad(new BigDecimal("5"));
        valeReq.setItems(List.of(item));

        ValeSalidaDTO vale = servicio.crearVale(7L, valeReq);
        assertEquals(new BigDecimal("15.00"), vale.getCostoTotal());
        assertEquals("CC-1", vale.getCentroCosto());
        StockItemDTO despues = stockDe(material.getId());
        assertEquals(new BigDecimal("15.0000"), despues.getCantidad());
        assertEquals(new BigDecimal("3.00"), despues.getCostoPromedio());
    }

    @Test
    void recepcionNoSuperaPendienteYValeRechazaStockInsuficiente() {
        Material material = crearMaterial("ACE", "0");
        OrdenCompraDTO oc = emitirSinRecepcionar(material, "10", "8");
        RecepcionRequest extra = recepcion(oc.getItems().get(0).getId(), "12");
        IllegalArgumentException exceso = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.recepcionar(7L, oc.getId(), extra));
        assertEquals("La recepción supera la cantidad pendiente de la orden", exceso.getMessage());

        servicio.recepcionar(7L, oc.getId(), recepcion(oc.getItems().get(0).getId(), "10"));
        ValeSalidaRequest valeReq = new ValeSalidaRequest();
        valeReq.setPartidaId(3L);
        ValeSalidaItemRequest item = new ValeSalidaItemRequest();
        item.setMaterialId(material.getId());
        item.setCantidad(new BigDecimal("11"));
        valeReq.setItems(List.of(item));
        IllegalArgumentException stock = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.crearVale(7L, valeReq));
        assertEquals("Stock insuficiente para el vale de salida", stock.getMessage());
    }

    @Test
    void requerimientoAprobadoCopiaAOcYNoSeEdita() {
        Material material = crearMaterial("FIE", "0");
        RequerimientoDTO req = crearYAprobarReq(material, "6");
        OrdenCompraRequest ocReq = new OrdenCompraRequest();
        ocReq.setRequerimientoId(req.getId());
        ocReq.setProveedorRazon("Aceros del Norte SAC");
        ocReq.setProveedorRuc("20123456789");
        OrdenCompraDTO oc = servicio.crearOrdenCompra(7L, ocReq);
        assertEquals(1, oc.getItems().size());
        assertEquals(new BigDecimal("6.0000"), oc.getItems().get(0).getCantidad());

        oc.getItems().get(0);
        OrdenCompraItemRequest precio = new OrdenCompraItemRequest();
        precio.setMaterialId(material.getId());
        precio.setRequerimientoItemId(oc.getItems().get(0).getRequerimientoItemId());
        precio.setCantidad(new BigDecimal("6"));
        precio.setPrecioUnitario(new BigDecimal("12"));
        ocReq.setItems(List.of(precio));
        servicio.guardarOrdenCompra(7L, oc.getId(), ocReq);
        OrdenCompraDTO emitida = servicio.emitirOrdenCompra(7L, oc.getId());
        assertEquals(EstadoOrdenCompra.EMITIDA, emitida.getEstado());
        assertEquals(EstadoRequerimientoObra.ATENDIDO, servicio.obtenerRequerimiento(7L, req.getId()).getEstado());

        RequerimientoRequest editar = new RequerimientoRequest();
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.guardarRequerimiento(7L, req.getId(), editar));
        assertEquals("Solo se puede editar un requerimiento en borrador", error.getMessage());
    }

    @Test
    void devolucionProveedorBajaRecibidoYStock() {
        Material material = crearMaterial("TUB", "0");
        OrdenCompraDTO oc = emitirYRecepcionar(material, "10", "5");
        DevolucionRequest devolucion = new DevolucionRequest();
        DevolucionItemRequest fila = new DevolucionItemRequest();
        fila.setOrdenCompraItemId(oc.getItems().get(0).getId());
        fila.setCantidad(new BigDecimal("3"));
        devolucion.setItems(List.of(fila));

        OrdenCompraDTO actualizada = servicio.devolverProveedor(7L, oc.getId(), devolucion);
        assertEquals(EstadoOrdenCompra.RECEPCIONADA, actualizada.getEstado());
        assertEquals(new BigDecimal("7.0000"), actualizada.getItems().get(0).getCantidadRecibida());
        assertEquals(new BigDecimal("7.0000"), stockDe(material.getId()).getCantidad());
    }

    @Test
    void cierreDeMesNoDuplicaYAlertaQuiebre() {
        Material material = crearMaterial("CAL", "8");
        assertTrue(servicio.obtenerAlmacen(7L).getStock().get(0).isQuiebre());
        assertTrue(servicio.obtenerAlmacen(7L).getStock().get(0).isCriticoSinStock());

        emitirYRecepcionar(material, "10", "4");
        CierreInventarioRequest cierre = new CierreInventarioRequest();
        cierre.setAnio(2026);
        cierre.setMes(11);
        servicio.cerrarInventario(7L, cierre);

        when(inventarioCierreRepository.existsByAlmacenIdAndAnioAndMes(1L, 2026, 11)).thenReturn(true);
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.cerrarInventario(7L, cierre));
        assertEquals("Ya existe un cierre de inventario para ese mes", error.getMessage());
    }

    @Test
    void ajusteNegativoNoDejaSaldoNegativo() {
        Material material = crearMaterial("PINT", "0");
        AjusteAlmacenRequest ajuste = new AjusteAlmacenRequest();
        ajuste.setMaterialId(material.getId());
        ajuste.setCantidad(new BigDecimal("-1"));
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.registrarAjuste(7L, ajuste));
        assertEquals("Stock insuficiente", error.getMessage());
    }

    private Material crearMaterial(String codigo, String minimo) {
        MaterialRequest request = new MaterialRequest();
        request.setCodigo(codigo);
        request.setDescripcion(codigo);
        request.setUnidad("BOL");
        request.setStockMinimo(new BigDecimal(minimo));
        request.setCritico(true);
        Long id = servicio.crearMaterial(request).getId();
        return materiales.get(id);
    }

    private RequerimientoDTO crearYAprobarReq(Material material, String cantidad) {
        RequerimientoRequest request = new RequerimientoRequest();
        RequerimientoItemRequest item = new RequerimientoItemRequest();
        item.setMaterialId(material.getId());
        item.setPartidaId(3L);
        item.setCantidad(new BigDecimal(cantidad));
        request.setItems(List.of(item));
        RequerimientoDTO creado = servicio.crearRequerimiento(7L, request);
        return servicio.aprobarRequerimiento(7L, creado.getId());
    }

    private OrdenCompraDTO emitirYRecepcionar(Material material, String cantidad, String precio) {
        OrdenCompraDTO oc = emitirSinRecepcionar(material, cantidad, precio);
        return servicio.recepcionar(7L, oc.getId(), recepcion(oc.getItems().get(0).getId(), cantidad));
    }

    private OrdenCompraDTO emitirSinRecepcionar(Material material, String cantidad, String precio) {
        OrdenCompraRequest request = new OrdenCompraRequest();
        request.setProveedorRazon("Proveedor SAC");
        request.setProveedorRuc("20111111111");
        OrdenCompraItemRequest item = new OrdenCompraItemRequest();
        item.setMaterialId(material.getId());
        item.setCantidad(new BigDecimal(cantidad));
        item.setPrecioUnitario(new BigDecimal(precio));
        request.setItems(List.of(item));
        OrdenCompraDTO oc = servicio.crearOrdenCompra(7L, request);
        return servicio.emitirOrdenCompra(7L, oc.getId());
    }

    private static RecepcionRequest recepcion(Long itemId, String cantidad) {
        RecepcionRequest request = new RecepcionRequest();
        RecepcionItemRequest item = new RecepcionItemRequest();
        item.setOrdenCompraItemId(itemId);
        item.setCantidad(new BigDecimal(cantidad));
        request.setItems(List.of(item));
        return request;
    }

    private StockItemDTO stockDe(Long materialId) {
        AlmacenDTO almacenDto = servicio.obtenerAlmacen(7L);
        return almacenDto.getStock().stream()
                .filter(item -> item.getMaterialId().equals(materialId))
                .findFirst()
                .orElseThrow();
    }
}
