package com.convecsa.erp.construccion.service.impl;

import com.convecsa.erp.common.exception.MaterialNoEncontradoException;
import com.convecsa.erp.common.exception.OrdenCompraNoEncontradaException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.common.exception.RequerimientoObraNoEncontradoException;
import com.convecsa.erp.common.exception.ValeSalidaNoEncontradoException;
import com.convecsa.erp.construccion.domain.EstadoOrdenCompra;
import com.convecsa.erp.construccion.domain.EstadoRequerimientoObra;
import com.convecsa.erp.construccion.domain.EstadoVersionPresupuesto;
import com.convecsa.erp.construccion.domain.TipoMovimientoAlmacen;
import com.convecsa.erp.construccion.domain.TipoRecursoApu;
import com.convecsa.erp.construccion.dto.AjusteAlmacenRequest;
import com.convecsa.erp.construccion.dto.AlmacenDTO;
import com.convecsa.erp.construccion.dto.CierreInventarioRequest;
import com.convecsa.erp.construccion.dto.DevolucionItemRequest;
import com.convecsa.erp.construccion.dto.DevolucionRequest;
import com.convecsa.erp.construccion.dto.InventarioCierreDTO;
import com.convecsa.erp.construccion.dto.InventarioCierreItemDTO;
import com.convecsa.erp.construccion.dto.MaterialDTO;
import com.convecsa.erp.construccion.dto.MaterialRequest;
import com.convecsa.erp.construccion.dto.MovimientoAlmacenDTO;
import com.convecsa.erp.construccion.dto.OrdenCompraDTO;
import com.convecsa.erp.construccion.dto.OrdenCompraItemDTO;
import com.convecsa.erp.construccion.dto.OrdenCompraItemRequest;
import com.convecsa.erp.construccion.dto.OrdenCompraRequest;
import com.convecsa.erp.construccion.dto.RecepcionDTO;
import com.convecsa.erp.construccion.dto.RecepcionItemDTO;
import com.convecsa.erp.construccion.dto.RecepcionItemRequest;
import com.convecsa.erp.construccion.dto.RecepcionRequest;
import com.convecsa.erp.construccion.dto.RequerimientoDTO;
import com.convecsa.erp.construccion.dto.RequerimientoItemDTO;
import com.convecsa.erp.construccion.dto.RequerimientoItemRequest;
import com.convecsa.erp.construccion.dto.RequerimientoRequest;
import com.convecsa.erp.construccion.dto.StockItemDTO;
import com.convecsa.erp.construccion.dto.SugeridoApuDTO;
import com.convecsa.erp.construccion.dto.ValeSalidaDTO;
import com.convecsa.erp.construccion.dto.ValeSalidaItemDTO;
import com.convecsa.erp.construccion.dto.ValeSalidaItemRequest;
import com.convecsa.erp.construccion.dto.ValeSalidaRequest;
import com.convecsa.erp.construccion.entity.AlmacenObra;
import com.convecsa.erp.construccion.entity.ApuPartida;
import com.convecsa.erp.construccion.entity.InventarioCierre;
import com.convecsa.erp.construccion.entity.Material;
import com.convecsa.erp.construccion.entity.MovimientoAlmacen;
import com.convecsa.erp.construccion.entity.OrdenCompra;
import com.convecsa.erp.construccion.entity.OrdenCompraItem;
import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.PresupuestoVersion;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.RecepcionAlmacen;
import com.convecsa.erp.construccion.entity.RecepcionItem;
import com.convecsa.erp.construccion.entity.RequerimientoItem;
import com.convecsa.erp.construccion.entity.RequerimientoObra;
import com.convecsa.erp.construccion.entity.ValeSalida;
import com.convecsa.erp.construccion.entity.ValeSalidaItem;
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
import com.convecsa.erp.construccion.service.InterfazAlmacenService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlmacenServiceImpl implements InterfazAlmacenService {

    private final ProyectoRepository proyectoRepository;
    private final MaterialRepository materialRepository;
    private final AlmacenObraRepository almacenObraRepository;
    private final RequerimientoObraRepository requerimientoObraRepository;
    private final RequerimientoItemRepository requerimientoItemRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final RecepcionAlmacenRepository recepcionAlmacenRepository;
    private final ValeSalidaRepository valeSalidaRepository;
    private final MovimientoAlmacenRepository movimientoAlmacenRepository;
    private final InventarioCierreRepository inventarioCierreRepository;
    private final PresupuestoVersionRepository presupuestoVersionRepository;
    private final PartidaPresupuestoRepository partidaPresupuestoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MaterialDTO> listarMateriales() {
        return materialRepository.findAllByOrderByCodigoAsc().stream().map(AlmacenServiceImpl::toMaterialDto).toList();
    }

    @Override
    @Transactional
    public MaterialDTO crearMaterial(MaterialRequest request) {
        String codigo = normalizarCodigo(request.getCodigo());
        if (materialRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new IllegalArgumentException("Ya existe un material con ese código");
        }
        Material material = new Material();
        aplicarMaterial(material, request, codigo);
        if (request.getActivo() == null) {
            material.setActivo(true);
        }
        return toMaterialDto(materialRepository.save(material));
    }

    @Override
    @Transactional
    public MaterialDTO actualizarMaterial(Long materialId, MaterialRequest request) {
        Material material = buscarMaterial(materialId);
        String codigo = normalizarCodigo(request.getCodigo());
        if (materialRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, materialId)) {
            throw new IllegalArgumentException("Ya existe un material con ese código");
        }
        aplicarMaterial(material, request, codigo);
        return toMaterialDto(materialRepository.save(material));
    }

    @Override
    @Transactional
    public AlmacenDTO obtenerAlmacen(Long proyectoId) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        AlmacenObra almacen = asegurarAlmacen(proyecto);
        return toAlmacenDto(almacen);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoAlmacenDTO> listarKardex(Long proyectoId, Long materialId) {
        AlmacenObra almacen = almacenObraRepository.findByProyectoId(proyectoId).orElse(null);
        if (almacen == null) {
            buscarProyecto(proyectoId);
            return List.of();
        }
        List<MovimientoAlmacen> movimientos = materialId == null
                ? movimientoAlmacenRepository.findByAlmacenIdOrderByIdDesc(almacen.getId())
                : movimientoAlmacenRepository.findByAlmacenIdAndMaterialIdOrderByIdDesc(almacen.getId(), materialId);
        return movimientos.stream().map(AlmacenServiceImpl::toMovimientoDto).toList();
    }

    @Override
    @Transactional
    public AlmacenDTO registrarAjuste(Long proyectoId, AjusteAlmacenRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        AlmacenObra almacen = asegurarAlmacen(proyecto);
        Material material = buscarMaterial(request.getMaterialId());
        registrarMovimiento(
                almacen,
                material,
                TipoMovimientoAlmacen.AJUSTE,
                nz(request.getCantidad()),
                request.getCostoUnitario(),
                fechaDe(request.getFecha()),
                request.getObservaciones(),
                null,
                null,
                null,
                null,
                null);
        return toAlmacenDto(almacen);
    }

    @Override
    @Transactional
    public InventarioCierreDTO cerrarInventario(Long proyectoId, CierreInventarioRequest request) {
        AlmacenObra almacen = asegurarAlmacen(buscarProyecto(proyectoId));
        int anio = request.getAnio();
        int mes = request.getMes();
        if (inventarioCierreRepository.existsByAlmacenIdAndAnioAndMes(almacen.getId(), anio, mes)) {
            throw new IllegalArgumentException("Ya existe un cierre de inventario para ese mes");
        }
        List<InventarioCierre> filas = new ArrayList<>();
        for (StockItemDTO stock : armarStock(almacen)) {
            if (stock.getCantidad().signum() == 0 && stock.getValor().signum() == 0) {
                continue;
            }
            InventarioCierre fila = new InventarioCierre();
            fila.setAlmacen(almacen);
            fila.setAnio(anio);
            fila.setMes(mes);
            fila.setMaterial(buscarMaterial(stock.getMaterialId()));
            fila.setCantidad(stock.getCantidad());
            fila.setCostoPromedio(stock.getCostoPromedio());
            fila.setValor(stock.getValor());
            filas.add(inventarioCierreRepository.save(fila));
        }
        if (filas.isEmpty()) {
            throw new IllegalArgumentException("No hay stock para cerrar en este almacén");
        }
        return toCierreDto(anio, mes, filas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioCierreDTO> listarCierres(Long proyectoId) {
        buscarProyecto(proyectoId);
        AlmacenObra almacen = almacenObraRepository.findByProyectoId(proyectoId).orElse(null);
        if (almacen == null) {
            return List.of();
        }
        Map<String, List<InventarioCierre>> agrupado = new LinkedHashMap<>();
        for (InventarioCierre fila : inventarioCierreRepository.findByAlmacenIdOrderByAnioDescMesDescIdAsc(almacen.getId())) {
            agrupado.computeIfAbsent(fila.getAnio() + "-" + fila.getMes(), clave -> new ArrayList<>()).add(fila);
        }
        return agrupado.values().stream()
                .map(filas -> toCierreDto(filas.get(0).getAnio(), filas.get(0).getMes(), filas))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequerimientoDTO> listarRequerimientos(Long proyectoId) {
        buscarProyecto(proyectoId);
        return requerimientoObraRepository.findByProyectoIdOrderByNumeroDesc(proyectoId).stream()
                .map(AlmacenServiceImpl::toRequerimientoDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RequerimientoDTO obtenerRequerimiento(Long proyectoId, Long requerimientoId) {
        return toRequerimientoDto(buscarRequerimiento(proyectoId, requerimientoId));
    }

    @Override
    @Transactional
    public RequerimientoDTO crearRequerimiento(Long proyectoId, RequerimientoRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        RequerimientoObra req = new RequerimientoObra();
        req.setProyecto(proyecto);
        req.setNumero(siguienteNumeroReq(proyectoId));
        req.setEstado(EstadoRequerimientoObra.BORRADOR);
        req.setFecha(fechaDe(request.getFecha()));
        req.setObservaciones(blankToNull(request.getObservaciones()));
        reemplazarItemsReq(req, request.getItems(), proyecto);
        return toRequerimientoDto(requerimientoObraRepository.save(req));
    }

    @Override
    @Transactional
    public RequerimientoDTO guardarRequerimiento(Long proyectoId, Long requerimientoId, RequerimientoRequest request) {
        RequerimientoObra req = buscarRequerimiento(proyectoId, requerimientoId);
        asegurarBorradorReq(req);
        req.setFecha(fechaDe(request.getFecha()));
        req.setObservaciones(blankToNull(request.getObservaciones()));
        reemplazarItemsReq(req, request.getItems(), req.getProyecto());
        return toRequerimientoDto(requerimientoObraRepository.save(req));
    }

    @Override
    @Transactional
    public RequerimientoDTO aprobarRequerimiento(Long proyectoId, Long requerimientoId) {
        RequerimientoObra req = buscarRequerimiento(proyectoId, requerimientoId);
        asegurarBorradorReq(req);
        if (req.getItems().isEmpty()) {
            throw new IllegalArgumentException("No se puede aprobar un requerimiento sin ítems");
        }
        req.setEstado(EstadoRequerimientoObra.APROBADO);
        return toRequerimientoDto(requerimientoObraRepository.save(req));
    }

    @Override
    @Transactional
    public RequerimientoDTO anularRequerimiento(Long proyectoId, Long requerimientoId) {
        RequerimientoObra req = buscarRequerimiento(proyectoId, requerimientoId);
        if (req.getEstado() == EstadoRequerimientoObra.ANULADO) {
            return toRequerimientoDto(req);
        }
        if (req.getEstado() == EstadoRequerimientoObra.ATENDIDO) {
            throw new IllegalArgumentException("No se puede anular un requerimiento atendido");
        }
        boolean atendidoParcial = req.getItems().stream()
                .anyMatch(item -> nz(item.getCantidadAtendida()).signum() > 0);
        if (atendidoParcial) {
            throw new IllegalArgumentException("No se puede anular un requerimiento con órdenes emitidas");
        }
        req.setEstado(EstadoRequerimientoObra.ANULADO);
        return toRequerimientoDto(requerimientoObraRepository.save(req));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SugeridoApuDTO> sugeridosApu(Long proyectoId) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        PresupuestoVersion vigente = presupuestoVersionRepository
                .findByProyectoIdAndEstado(proyectoId, EstadoVersionPresupuesto.VIGENTE)
                .orElse(null);
        if (vigente == null) {
            return List.of();
        }
        List<SugeridoApuDTO> sugeridos = new ArrayList<>();
        for (PartidaPresupuesto partida : partidaPresupuestoRepository.findByVersionIdOrderByOrdenAsc(vigente.getId())) {
            for (ApuPartida apu : partida.getApu()) {
                if (apu.getTipo() != TipoRecursoApu.MATERIAL) {
                    continue;
                }
                Long materialId = blankToNull(apu.getCodigo()) == null
                        ? null
                        : materialRepository.findByCodigoIgnoreCase(apu.getCodigo()).map(Material::getId).orElse(null);
                sugeridos.add(SugeridoApuDTO.builder()
                        .materialId(materialId)
                        .codigo(apu.getCodigo())
                        .descripcion(apu.getDescripcion())
                        .unidad(apu.getUnidad())
                        .partidaId(partida.getId())
                        .codigoPartida(partida.getCodigo())
                        .descripcionPartida(partida.getDescripcion())
                        .centroCosto(centroDe(partida.getCentroCosto(), proyecto.getCentroCosto()))
                        .cantidad(qty(nz(apu.getCantidad()).multiply(nz(partida.getMetrado()))))
                        .build());
            }
        }
        return sugeridos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompraDTO> listarOrdenesCompra(Long proyectoId) {
        buscarProyecto(proyectoId);
        return ordenCompraRepository.findByProyectoIdOrderByNumeroDesc(proyectoId).stream()
                .map(oc -> toOrdenDto(oc, List.of()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenCompraDTO obtenerOrdenCompra(Long proyectoId, Long ordenId) {
        OrdenCompra oc = buscarOrden(proyectoId, ordenId);
        return toOrdenDto(oc, recepcionAlmacenRepository.findByOrdenCompraIdOrderByIdDesc(oc.getId()));
    }

    @Override
    @Transactional
    public OrdenCompraDTO crearOrdenCompra(Long proyectoId, OrdenCompraRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        OrdenCompra oc = new OrdenCompra();
        oc.setProyecto(proyecto);
        oc.setNumero(siguienteNumeroOc(proyectoId));
        oc.setEstado(EstadoOrdenCompra.BORRADOR);
        aplicarCabeceraOc(oc, request);
        List<OrdenCompraItemRequest> items = resolverItemsOc(proyectoId, request);
        reemplazarItemsOc(oc, items);
        return toOrdenDto(ordenCompraRepository.save(oc), List.of());
    }

    @Override
    @Transactional
    public OrdenCompraDTO guardarOrdenCompra(Long proyectoId, Long ordenId, OrdenCompraRequest request) {
        OrdenCompra oc = buscarOrden(proyectoId, ordenId);
        asegurarBorradorOc(oc);
        aplicarCabeceraOc(oc, request);
        List<OrdenCompraItemRequest> items = resolverItemsOc(proyectoId, request);
        reemplazarItemsOc(oc, items);
        return toOrdenDto(ordenCompraRepository.save(oc), List.of());
    }

    @Override
    @Transactional
    public OrdenCompraDTO emitirOrdenCompra(Long proyectoId, Long ordenId) {
        OrdenCompra oc = buscarOrden(proyectoId, ordenId);
        asegurarBorradorOc(oc);
        if (oc.getItems().isEmpty()) {
            throw new IllegalArgumentException("No se puede emitir una orden de compra sin ítems");
        }
        if (blankToNull(oc.getProveedorRazon()) == null) {
            throw new IllegalArgumentException("La orden de compra requiere razón social del proveedor");
        }
        if (blankToNull(oc.getProveedorRuc()) == null) {
            throw new IllegalArgumentException("La orden de compra requiere RUC del proveedor");
        }
        for (OrdenCompraItem item : oc.getItems()) {
            if (nz(item.getPrecioUnitario()).signum() <= 0) {
                throw new IllegalArgumentException("Todos los ítems deben tener precio mayor a cero");
            }
        }
        oc.setEstado(EstadoOrdenCompra.EMITIDA);
        atenderRequerimientos(oc);
        return toOrdenDto(ordenCompraRepository.save(oc), List.of());
    }

    @Override
    @Transactional
    public OrdenCompraDTO cerrarOrdenCompra(Long proyectoId, Long ordenId) {
        OrdenCompra oc = buscarOrden(proyectoId, ordenId);
        if (oc.getEstado() != EstadoOrdenCompra.EMITIDA && oc.getEstado() != EstadoOrdenCompra.RECEPCIONADA) {
            throw new IllegalArgumentException("Solo se puede cerrar una orden emitida o recepcionada");
        }
        oc.setEstado(EstadoOrdenCompra.CERRADA);
        return toOrdenDto(ordenCompraRepository.save(oc), recepcionAlmacenRepository.findByOrdenCompraIdOrderByIdDesc(oc.getId()));
    }

    @Override
    @Transactional
    public OrdenCompraDTO recepcionar(Long proyectoId, Long ordenId, RecepcionRequest request) {
        OrdenCompra oc = buscarOrden(proyectoId, ordenId);
        if (oc.getEstado() != EstadoOrdenCompra.EMITIDA && oc.getEstado() != EstadoOrdenCompra.RECEPCIONADA) {
            throw new IllegalArgumentException("Solo se puede recepcionar una orden emitida o recepcionada");
        }
        AlmacenObra almacen = asegurarAlmacen(oc.getProyecto());
        RecepcionAlmacen recepcion = new RecepcionAlmacen();
        recepcion.setOrdenCompra(oc);
        recepcion.setAlmacen(almacen);
        recepcion.setNumeroGuia(blankToNull(request.getNumeroGuia()));
        recepcion.setFecha(fechaDe(request.getFecha()));
        recepcion.setObservaciones(blankToNull(request.getObservaciones()));
        for (RecepcionItemRequest fila : request.getItems()) {
            OrdenCompraItem ocItem = buscarItemOc(oc, fila.getOrdenCompraItemId());
            BigDecimal cantidad = qty(fila.getCantidad());
            BigDecimal pendiente = qty(nz(ocItem.getCantidad()).subtract(nz(ocItem.getCantidadRecibida())));
            if (cantidad.compareTo(pendiente) > 0) {
                throw new IllegalArgumentException("La recepción supera la cantidad pendiente de la orden");
            }
            ocItem.setCantidadRecibida(qty(nz(ocItem.getCantidadRecibida()).add(cantidad)));
            RecepcionItem item = new RecepcionItem();
            item.setRecepcion(recepcion);
            item.setOrdenCompraItem(ocItem);
            item.setCantidad(cantidad);
            recepcion.getItems().add(item);
        }
        RecepcionAlmacen saved = recepcionAlmacenRepository.save(recepcion);
        for (RecepcionItem item : saved.getItems()) {
            OrdenCompraItem ocItem = item.getOrdenCompraItem();
            registrarMovimiento(
                    almacen,
                    ocItem.getMaterial(),
                    TipoMovimientoAlmacen.ENTRADA,
                    item.getCantidad(),
                    ocItem.getPrecioUnitario(),
                    saved.getFecha(),
                    saved.getObservaciones(),
                    null,
                    null,
                    saved,
                    null,
                    oc);
        }
        boolean pendiente = oc.getItems().stream()
                .anyMatch(item -> nz(item.getCantidadRecibida()).compareTo(nz(item.getCantidad())) < 0);
        oc.setEstado(pendiente ? EstadoOrdenCompra.RECEPCIONADA : EstadoOrdenCompra.CERRADA);
        ordenCompraRepository.save(oc);
        return toOrdenDto(oc, recepcionAlmacenRepository.findByOrdenCompraIdOrderByIdDesc(oc.getId()));
    }

    @Override
    @Transactional
    public OrdenCompraDTO devolverProveedor(Long proyectoId, Long ordenId, DevolucionRequest request) {
        OrdenCompra oc = buscarOrden(proyectoId, ordenId);
        if (oc.getEstado() != EstadoOrdenCompra.RECEPCIONADA && oc.getEstado() != EstadoOrdenCompra.CERRADA) {
            throw new IllegalArgumentException("Solo se puede devolver material de una orden recepcionada");
        }
        AlmacenObra almacen = asegurarAlmacen(oc.getProyecto());
        List<DevolucionItemRequest> filas = request.getItems() == null ? List.of() : request.getItems();
        if (filas.isEmpty()) {
            throw new IllegalArgumentException("Indique los ítems a devolver");
        }
        for (DevolucionItemRequest fila : filas) {
            OrdenCompraItem ocItem = buscarItemOc(oc, fila.getOrdenCompraItemId());
            BigDecimal cantidad = qty(fila.getCantidad());
            if (cantidad.compareTo(nz(ocItem.getCantidadRecibida())) > 0) {
                throw new IllegalArgumentException("No se puede devolver más de lo recepcionado");
            }
            registrarMovimiento(
                    almacen,
                    ocItem.getMaterial(),
                    TipoMovimientoAlmacen.DEVOLUCION_PROVEEDOR,
                    cantidad,
                    null,
                    fechaDe(request.getFecha()),
                    request.getObservaciones(),
                    null,
                    null,
                    null,
                    null,
                    oc);
            ocItem.setCantidadRecibida(qty(nz(ocItem.getCantidadRecibida()).subtract(cantidad)));
        }
        oc.setEstado(EstadoOrdenCompra.RECEPCIONADA);
        ordenCompraRepository.save(oc);
        return toOrdenDto(oc, recepcionAlmacenRepository.findByOrdenCompraIdOrderByIdDesc(oc.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ValeSalidaDTO> listarVales(Long proyectoId) {
        buscarProyecto(proyectoId);
        AlmacenObra almacen = almacenObraRepository.findByProyectoId(proyectoId).orElse(null);
        if (almacen == null) {
            return List.of();
        }
        return valeSalidaRepository.findByAlmacenIdOrderByNumeroDesc(almacen.getId()).stream()
                .map(AlmacenServiceImpl::toValeDto)
                .toList();
    }

    @Override
    @Transactional
    public ValeSalidaDTO crearVale(Long proyectoId, ValeSalidaRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        AlmacenObra almacen = asegurarAlmacen(proyecto);
        PartidaPresupuesto partida = buscarPartidaVigente(proyectoId, request.getPartidaId());
        ValeSalida vale = new ValeSalida();
        vale.setAlmacen(almacen);
        vale.setNumero(siguienteNumeroVale(almacen.getId()));
        vale.setFecha(fechaDe(request.getFecha()));
        vale.setPartida(partida);
        vale.setCodigoPartida(partida.getCodigo());
        vale.setDescripcionPartida(partida.getDescripcion());
        vale.setCentroCosto(centroDe(partida.getCentroCosto(), proyecto.getCentroCosto()));
        vale.setObservaciones(blankToNull(request.getObservaciones()));
        ValeSalida saved = valeSalidaRepository.save(vale);
        int orden = 0;
        for (ValeSalidaItemRequest fila : request.getItems()) {
            Material material = buscarMaterial(fila.getMaterialId());
            StockSaldo saldo = stockDe(almacen.getId(), material.getId());
            BigDecimal cantidad = qty(fila.getCantidad());
            if (cantidad.compareTo(saldo.cantidad) > 0) {
                throw new IllegalArgumentException("Stock insuficiente para el vale de salida");
            }
            MovimientoAlmacen movimiento = registrarMovimiento(
                    almacen,
                    material,
                    TipoMovimientoAlmacen.SALIDA,
                    cantidad,
                    null,
                    saved.getFecha(),
                    saved.getObservaciones(),
                    partida,
                    saved.getCentroCosto(),
                    null,
                    saved,
                    null);
            ValeSalidaItem item = new ValeSalidaItem();
            item.setVale(saved);
            item.setMaterial(material);
            item.setCantidad(cantidad);
            item.setCostoUnitario(movimiento.getCostoUnitario());
            item.setCostoTotal(movimiento.getCostoTotal());
            item.setOrden(orden++);
            saved.getItems().add(item);
        }
        return toValeDto(valeSalidaRepository.save(saved));
    }

    @Override
    @Transactional
    public ValeSalidaDTO devolverVale(Long proyectoId, Long valeId, DevolucionRequest request) {
        ValeSalida vale = valeSalidaRepository.findByIdAndAlmacenProyectoId(valeId, proyectoId)
                .orElseThrow(() -> new ValeSalidaNoEncontradoException("No se encontró el vale de salida"));
        AlmacenObra almacen = vale.getAlmacen();
        List<DevolucionItemRequest> filas = request.getItems() == null || request.getItems().isEmpty()
                ? vale.getItems().stream().map(item -> {
                    DevolucionItemRequest fila = new DevolucionItemRequest();
                    fila.setMaterialId(item.getMaterial().getId());
                    fila.setCantidad(item.getCantidad());
                    return fila;
                }).toList()
                : request.getItems();
        for (DevolucionItemRequest fila : filas) {
            ValeSalidaItem origen = vale.getItems().stream()
                    .filter(item -> item.getMaterial().getId().equals(fila.getMaterialId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("El material no pertenece a este vale"));
            BigDecimal yaDevuelto = movimientoAlmacenRepository
                    .findByValeIdAndTipo(vale.getId(), TipoMovimientoAlmacen.DEVOLUCION_ALMACEN)
                    .stream()
                    .filter(mov -> mov.getMaterial().getId().equals(origen.getMaterial().getId()))
                    .map(MovimientoAlmacen::getCantidad)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal cantidad = qty(fila.getCantidad());
            if (yaDevuelto.add(cantidad).compareTo(origen.getCantidad()) > 0) {
                throw new IllegalArgumentException("No se puede devolver más de lo salido en el vale");
            }
            registrarMovimiento(
                    almacen,
                    origen.getMaterial(),
                    TipoMovimientoAlmacen.DEVOLUCION_ALMACEN,
                    cantidad,
                    origen.getCostoUnitario(),
                    fechaDe(request.getFecha()),
                    request.getObservaciones(),
                    vale.getPartida(),
                    vale.getCentroCosto(),
                    null,
                    vale,
                    null);
        }
        return toValeDto(vale);
    }

    private void aplicarMaterial(Material material, MaterialRequest request, String codigo) {
        material.setCodigo(codigo);
        material.setDescripcion(request.getDescripcion().trim());
        material.setUnidad(blankToNull(request.getUnidad()));
        material.setFamilia(blankToNull(request.getFamilia()));
        material.setStockMinimo(qty(request.getStockMinimo()));
        material.setCritico(Boolean.TRUE.equals(request.getCritico()));
        if (request.getActivo() != null) {
            material.setActivo(request.getActivo());
        }
    }

    private void reemplazarItemsReq(RequerimientoObra req, List<RequerimientoItemRequest> filas, Proyecto proyecto) {
        req.getItems().clear();
        if (filas == null) {
            return;
        }
        int orden = 0;
        for (RequerimientoItemRequest fila : filas) {
            RequerimientoItem item = new RequerimientoItem();
            item.setRequerimiento(req);
            item.setMaterial(buscarMaterial(fila.getMaterialId()));
            aplicarPartida(item, fila.getPartidaId(), proyecto);
            item.setCantidad(qty(fila.getCantidad()));
            item.setCantidadAtendida(BigDecimal.ZERO);
            item.setOrden(orden++);
            req.getItems().add(item);
        }
    }

    private void aplicarPartida(RequerimientoItem item, Long partidaId, Proyecto proyecto) {
        if (partidaId == null) {
            item.setPartida(null);
            item.setCodigoPartida(null);
            item.setDescripcionPartida(null);
            item.setCentroCosto(centroDe(null, proyecto.getCentroCosto()));
            return;
        }
        PartidaPresupuesto partida = buscarPartidaVigente(proyecto.getId(), partidaId);
        item.setPartida(partida);
        item.setCodigoPartida(partida.getCodigo());
        item.setDescripcionPartida(partida.getDescripcion());
        item.setCentroCosto(centroDe(partida.getCentroCosto(), proyecto.getCentroCosto()));
    }

    private void aplicarCabeceraOc(OrdenCompra oc, OrdenCompraRequest request) {
        oc.setProveedorRazon(blankToNull(request.getProveedorRazon()));
        oc.setProveedorRuc(normalizarRuc(request.getProveedorRuc()));
        oc.setPlazoEntrega(request.getPlazoEntrega());
        oc.setObservaciones(blankToNull(request.getObservaciones()));
    }

    private List<OrdenCompraItemRequest> resolverItemsOc(Long proyectoId, OrdenCompraRequest request) {
        List<OrdenCompraItemRequest> items = request.getItems() == null ? List.of() : request.getItems();
        if (!items.isEmpty()) {
            return items;
        }
        if (request.getRequerimientoId() == null) {
            return List.of();
        }
        RequerimientoObra req = buscarRequerimiento(proyectoId, request.getRequerimientoId());
        if (req.getEstado() != EstadoRequerimientoObra.APROBADO) {
            throw new IllegalArgumentException("Solo se puede copiar un requerimiento aprobado");
        }
        List<OrdenCompraItemRequest> copiados = new ArrayList<>();
        for (RequerimientoItem origen : req.getItems()) {
            OrdenCompraItemRequest fila = new OrdenCompraItemRequest();
            fila.setMaterialId(origen.getMaterial().getId());
            fila.setRequerimientoItemId(origen.getId());
            fila.setCantidad(origen.getCantidad());
            fila.setPrecioUnitario(BigDecimal.ZERO);
            copiados.add(fila);
        }
        return copiados;
    }

    private void reemplazarItemsOc(OrdenCompra oc, List<OrdenCompraItemRequest> filas) {
        oc.getItems().clear();
        int orden = 0;
        for (OrdenCompraItemRequest fila : filas) {
            OrdenCompraItem item = new OrdenCompraItem();
            item.setOrdenCompra(oc);
            item.setMaterial(buscarMaterial(fila.getMaterialId()));
            if (fila.getRequerimientoItemId() != null) {
                RequerimientoItem reqItem = requerimientoItemRepository.findById(fila.getRequerimientoItemId())
                        .orElseThrow(() -> new RequerimientoObraNoEncontradoException("No se encontró el ítem del requerimiento"));
                if (!reqItem.getRequerimiento().getProyecto().getId().equals(oc.getProyecto().getId())) {
                    throw new IllegalArgumentException("El ítem de requerimiento no pertenece a esta obra");
                }
                item.setRequerimientoItem(reqItem);
            }
            item.setCantidad(qty(fila.getCantidad()));
            item.setPrecioUnitario(money(fila.getPrecioUnitario()));
            item.setCantidadRecibida(BigDecimal.ZERO);
            item.setOrden(orden++);
            oc.getItems().add(item);
        }
    }

    private void atenderRequerimientos(OrdenCompra oc) {
        Map<Long, RequerimientoObra> afectados = new LinkedHashMap<>();
        for (OrdenCompraItem item : oc.getItems()) {
            RequerimientoItem reqItem = item.getRequerimientoItem();
            if (reqItem == null) {
                continue;
            }
            reqItem.setCantidadAtendida(qty(nz(reqItem.getCantidadAtendida()).add(item.getCantidad())));
            afectados.put(reqItem.getRequerimiento().getId(), reqItem.getRequerimiento());
        }
        for (RequerimientoObra req : afectados.values()) {
            boolean cubierto = req.getItems().stream()
                    .allMatch(item -> nz(item.getCantidadAtendida()).compareTo(nz(item.getCantidad())) >= 0);
            if (cubierto) {
                req.setEstado(EstadoRequerimientoObra.ATENDIDO);
            }
            requerimientoObraRepository.save(req);
        }
    }

    private MovimientoAlmacen registrarMovimiento(
            AlmacenObra almacen,
            Material material,
            TipoMovimientoAlmacen tipo,
            BigDecimal cantidad,
            BigDecimal costoEntrada,
            LocalDate fecha,
            String observaciones,
            PartidaPresupuesto partida,
            String centroCosto,
            RecepcionAlmacen recepcion,
            ValeSalida vale,
            OrdenCompra ordenCompra) {
        BigDecimal qtyAbs = qty(cantidad.abs());
        if (qtyAbs.signum() == 0) {
            throw new IllegalArgumentException("La cantidad no puede ser cero");
        }
        StockSaldo saldo = stockDe(almacen.getId(), material.getId());
        boolean entra = tipo == TipoMovimientoAlmacen.ENTRADA
                || tipo == TipoMovimientoAlmacen.DEVOLUCION_ALMACEN
                || (tipo == TipoMovimientoAlmacen.AJUSTE && cantidad.signum() > 0);
        BigDecimal costoUnitario;
        BigDecimal nuevoSaldo;
        BigDecimal nuevoCmp;
        if (entra) {
            BigDecimal precio = money(costoEntrada != null && costoEntrada.signum() > 0 ? costoEntrada : saldo.cmp);
            BigDecimal valorAnterior = saldo.cantidad.multiply(saldo.cmp);
            BigDecimal valorEntrada = qtyAbs.multiply(precio);
            nuevoSaldo = qty(saldo.cantidad.add(qtyAbs));
            nuevoCmp = nuevoSaldo.signum() == 0
                    ? saldo.cmp
                    : money(valorAnterior.add(valorEntrada).divide(nuevoSaldo, 8, RoundingMode.HALF_UP));
            costoUnitario = precio;
        } else {
            if (qtyAbs.compareTo(saldo.cantidad) > 0) {
                throw new IllegalArgumentException("Stock insuficiente");
            }
            costoUnitario = money(saldo.cmp);
            nuevoSaldo = qty(saldo.cantidad.subtract(qtyAbs));
            nuevoCmp = nuevoSaldo.signum() == 0 ? saldo.cmp : saldo.cmp;
        }
        MovimientoAlmacen movimiento = new MovimientoAlmacen();
        movimiento.setAlmacen(almacen);
        movimiento.setMaterial(material);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(tipo == TipoMovimientoAlmacen.AJUSTE ? qty(cantidad) : qtyAbs);
        movimiento.setCostoUnitario(costoUnitario);
        movimiento.setCostoTotal(money(qtyAbs.multiply(costoUnitario)));
        movimiento.setSaldoCantidad(nuevoSaldo);
        movimiento.setCostoPromedio(nuevoCmp);
        movimiento.setPartida(partida);
        movimiento.setCentroCosto(centroCosto);
        movimiento.setRecepcion(recepcion);
        movimiento.setVale(vale);
        movimiento.setOrdenCompra(ordenCompra);
        movimiento.setFecha(fecha);
        movimiento.setObservaciones(blankToNull(observaciones));
        return movimientoAlmacenRepository.save(movimiento);
    }

    private StockSaldo stockDe(Long almacenId, Long materialId) {
        return movimientoAlmacenRepository
                .findFirstByAlmacenIdAndMaterialIdOrderByIdDesc(almacenId, materialId)
                .map(mov -> new StockSaldo(nz(mov.getSaldoCantidad()), money(mov.getCostoPromedio())))
                .orElse(new StockSaldo(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)));
    }

    private List<StockItemDTO> armarStock(AlmacenObra almacen) {
        List<StockItemDTO> filas = new ArrayList<>();
        for (Material material : materialRepository.findAllByOrderByCodigoAsc()) {
            StockSaldo saldo = stockDe(almacen.getId(), material.getId());
            boolean quiebre = saldo.cantidad.compareTo(nz(material.getStockMinimo())) < 0;
            boolean criticoSinStock = material.isCritico() && saldo.cantidad.signum() == 0;
            filas.add(StockItemDTO.builder()
                    .materialId(material.getId())
                    .codigo(material.getCodigo())
                    .descripcion(material.getDescripcion())
                    .unidad(material.getUnidad())
                    .familia(material.getFamilia())
                    .stockMinimo(qty(material.getStockMinimo()))
                    .critico(material.isCritico())
                    .activo(material.isActivo())
                    .cantidad(saldo.cantidad)
                    .costoPromedio(saldo.cmp)
                    .valor(money(saldo.cantidad.multiply(saldo.cmp)))
                    .quiebre(quiebre)
                    .criticoSinStock(criticoSinStock)
                    .build());
        }
        return filas;
    }

    private AlmacenDTO toAlmacenDto(AlmacenObra almacen) {
        List<StockItemDTO> stock = armarStock(almacen);
        int quiebres = (int) stock.stream().filter(StockItemDTO::isQuiebre).count();
        int criticos = (int) stock.stream().filter(StockItemDTO::isCriticoSinStock).count();
        return AlmacenDTO.builder()
                .id(almacen.getId())
                .proyectoId(almacen.getProyecto().getId())
                .nombre(almacen.getNombre())
                .quiebres(quiebres)
                .criticosSinStock(criticos)
                .stock(stock)
                .build();
    }

    private AlmacenObra asegurarAlmacen(Proyecto proyecto) {
        return almacenObraRepository.findByProyectoId(proyecto.getId()).orElseGet(() -> {
            AlmacenObra almacen = new AlmacenObra();
            almacen.setProyecto(proyecto);
            almacen.setNombre("Almacén " + proyecto.getCodigo());
            return almacenObraRepository.save(almacen);
        });
    }

    private Proyecto buscarProyecto(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private Material buscarMaterial(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new MaterialNoEncontradoException("No se encontró el material"));
    }

    private RequerimientoObra buscarRequerimiento(Long proyectoId, Long requerimientoId) {
        return requerimientoObraRepository.findByIdAndProyectoId(requerimientoId, proyectoId)
                .orElseThrow(() -> new RequerimientoObraNoEncontradoException("No se encontró el requerimiento"));
    }

    private OrdenCompra buscarOrden(Long proyectoId, Long ordenId) {
        return ordenCompraRepository.findByIdAndProyectoId(ordenId, proyectoId)
                .orElseThrow(() -> new OrdenCompraNoEncontradaException("No se encontró la orden de compra"));
    }

    private OrdenCompraItem buscarItemOc(OrdenCompra oc, Long itemId) {
        return oc.getItems().stream()
                .filter(item -> Objects.equals(item.getId(), itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El ítem no pertenece a esta orden de compra"));
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

    private void asegurarBorradorReq(RequerimientoObra req) {
        if (req.getEstado() != EstadoRequerimientoObra.BORRADOR) {
            throw new IllegalArgumentException("Solo se puede editar un requerimiento en borrador");
        }
    }

    private void asegurarBorradorOc(OrdenCompra oc) {
        if (oc.getEstado() != EstadoOrdenCompra.BORRADOR) {
            throw new IllegalArgumentException("Solo se puede editar una orden de compra en borrador");
        }
    }

    private int siguienteNumeroReq(Long proyectoId) {
        return requerimientoObraRepository.findFirstByProyectoIdOrderByNumeroDesc(proyectoId)
                .map(item -> item.getNumero() + 1)
                .orElse(1);
    }

    private int siguienteNumeroOc(Long proyectoId) {
        return ordenCompraRepository.findFirstByProyectoIdOrderByNumeroDesc(proyectoId)
                .map(item -> item.getNumero() + 1)
                .orElse(1);
    }

    private int siguienteNumeroVale(Long almacenId) {
        return valeSalidaRepository.findFirstByAlmacenIdOrderByNumeroDesc(almacenId)
                .map(item -> item.getNumero() + 1)
                .orElse(1);
    }

    private static MaterialDTO toMaterialDto(Material material) {
        return MaterialDTO.builder()
                .id(material.getId())
                .codigo(material.getCodigo())
                .descripcion(material.getDescripcion())
                .unidad(material.getUnidad())
                .familia(material.getFamilia())
                .stockMinimo(qty(material.getStockMinimo()))
                .critico(material.isCritico())
                .activo(material.isActivo())
                .build();
    }

    private static RequerimientoDTO toRequerimientoDto(RequerimientoObra req) {
        return RequerimientoDTO.builder()
                .id(req.getId())
                .proyectoId(req.getProyecto().getId())
                .numero(req.getNumero())
                .estado(req.getEstado())
                .fecha(req.getFecha())
                .observaciones(req.getObservaciones())
                .createdAt(req.getCreatedAt())
                .items(req.getItems().stream().map(AlmacenServiceImpl::toReqItemDto).toList())
                .build();
    }

    private static RequerimientoItemDTO toReqItemDto(RequerimientoItem item) {
        Material material = item.getMaterial();
        return RequerimientoItemDTO.builder()
                .id(item.getId())
                .materialId(material.getId())
                .codigoMaterial(material.getCodigo())
                .descripcionMaterial(material.getDescripcion())
                .unidad(material.getUnidad())
                .partidaId(item.getPartida() == null ? null : item.getPartida().getId())
                .codigoPartida(item.getCodigoPartida())
                .descripcionPartida(item.getDescripcionPartida())
                .centroCosto(item.getCentroCosto())
                .cantidad(qty(item.getCantidad()))
                .cantidadAtendida(qty(item.getCantidadAtendida()))
                .build();
    }

    private static OrdenCompraDTO toOrdenDto(OrdenCompra oc, List<RecepcionAlmacen> recepciones) {
        return OrdenCompraDTO.builder()
                .id(oc.getId())
                .proyectoId(oc.getProyecto().getId())
                .numero(oc.getNumero())
                .estado(oc.getEstado())
                .proveedorRazon(oc.getProveedorRazon())
                .proveedorRuc(oc.getProveedorRuc())
                .plazoEntrega(oc.getPlazoEntrega())
                .observaciones(oc.getObservaciones())
                .createdAt(oc.getCreatedAt())
                .items(oc.getItems().stream().map(AlmacenServiceImpl::toOcItemDto).toList())
                .recepciones(recepciones.stream().map(AlmacenServiceImpl::toRecepcionDto).toList())
                .build();
    }

    private static OrdenCompraItemDTO toOcItemDto(OrdenCompraItem item) {
        Material material = item.getMaterial();
        BigDecimal pendiente = qty(nz(item.getCantidad()).subtract(nz(item.getCantidadRecibida())));
        return OrdenCompraItemDTO.builder()
                .id(item.getId())
                .materialId(material.getId())
                .codigoMaterial(material.getCodigo())
                .descripcionMaterial(material.getDescripcion())
                .unidad(material.getUnidad())
                .requerimientoItemId(item.getRequerimientoItem() == null ? null : item.getRequerimientoItem().getId())
                .cantidad(qty(item.getCantidad()))
                .precioUnitario(money(item.getPrecioUnitario()))
                .cantidadRecibida(qty(item.getCantidadRecibida()))
                .cantidadPendiente(pendiente.max(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP)))
                .build();
    }

    private static RecepcionDTO toRecepcionDto(RecepcionAlmacen recepcion) {
        return RecepcionDTO.builder()
                .id(recepcion.getId())
                .numeroGuia(recepcion.getNumeroGuia())
                .fecha(recepcion.getFecha())
                .observaciones(recepcion.getObservaciones())
                .createdAt(recepcion.getCreatedAt())
                .items(recepcion.getItems().stream().map(AlmacenServiceImpl::toRecepcionItemDto).toList())
                .build();
    }

    private static RecepcionItemDTO toRecepcionItemDto(RecepcionItem item) {
        OrdenCompraItem ocItem = item.getOrdenCompraItem();
        return RecepcionItemDTO.builder()
                .id(item.getId())
                .ordenCompraItemId(ocItem.getId())
                .materialId(ocItem.getMaterial().getId())
                .codigoMaterial(ocItem.getMaterial().getCodigo())
                .cantidad(qty(item.getCantidad()))
                .build();
    }

    private static ValeSalidaDTO toValeDto(ValeSalida vale) {
        BigDecimal total = vale.getItems().stream()
                .map(item -> money(item.getCostoTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ValeSalidaDTO.builder()
                .id(vale.getId())
                .almacenId(vale.getAlmacen().getId())
                .numero(vale.getNumero())
                .fecha(vale.getFecha())
                .partidaId(vale.getPartida() == null ? null : vale.getPartida().getId())
                .codigoPartida(vale.getCodigoPartida())
                .descripcionPartida(vale.getDescripcionPartida())
                .centroCosto(vale.getCentroCosto())
                .observaciones(vale.getObservaciones())
                .createdAt(vale.getCreatedAt())
                .costoTotal(money(total))
                .items(vale.getItems().stream().map(AlmacenServiceImpl::toValeItemDto).toList())
                .build();
    }

    private static ValeSalidaItemDTO toValeItemDto(ValeSalidaItem item) {
        Material material = item.getMaterial();
        return ValeSalidaItemDTO.builder()
                .id(item.getId())
                .materialId(material.getId())
                .codigoMaterial(material.getCodigo())
                .descripcionMaterial(material.getDescripcion())
                .unidad(material.getUnidad())
                .cantidad(qty(item.getCantidad()))
                .costoUnitario(money(item.getCostoUnitario()))
                .costoTotal(money(item.getCostoTotal()))
                .build();
    }

    private static MovimientoAlmacenDTO toMovimientoDto(MovimientoAlmacen movimiento) {
        Material material = movimiento.getMaterial();
        return MovimientoAlmacenDTO.builder()
                .id(movimiento.getId())
                .materialId(material.getId())
                .codigoMaterial(material.getCodigo())
                .descripcionMaterial(material.getDescripcion())
                .tipo(movimiento.getTipo())
                .cantidad(qty(movimiento.getCantidad()))
                .costoUnitario(money(movimiento.getCostoUnitario()))
                .costoTotal(money(movimiento.getCostoTotal()))
                .saldoCantidad(qty(movimiento.getSaldoCantidad()))
                .costoPromedio(money(movimiento.getCostoPromedio()))
                .partidaId(movimiento.getPartida() == null ? null : movimiento.getPartida().getId())
                .codigoPartida(movimiento.getPartida() == null ? null : movimiento.getPartida().getCodigo())
                .centroCosto(movimiento.getCentroCosto())
                .recepcionId(movimiento.getRecepcion() == null ? null : movimiento.getRecepcion().getId())
                .valeId(movimiento.getVale() == null ? null : movimiento.getVale().getId())
                .ordenCompraId(movimiento.getOrdenCompra() == null ? null : movimiento.getOrdenCompra().getId())
                .fecha(movimiento.getFecha())
                .observaciones(movimiento.getObservaciones())
                .createdAt(movimiento.getCreatedAt())
                .build();
    }

    private static InventarioCierreDTO toCierreDto(int anio, int mes, List<InventarioCierre> filas) {
        List<InventarioCierreItemDTO> items = filas.stream()
                .map(fila -> InventarioCierreItemDTO.builder()
                        .materialId(fila.getMaterial().getId())
                        .codigo(fila.getMaterial().getCodigo())
                        .descripcion(fila.getMaterial().getDescripcion())
                        .unidad(fila.getMaterial().getUnidad())
                        .cantidad(qty(fila.getCantidad()))
                        .costoPromedio(money(fila.getCostoPromedio()))
                        .valor(money(fila.getValor()))
                        .build())
                .toList();
        BigDecimal total = items.stream().map(InventarioCierreItemDTO::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        return InventarioCierreDTO.builder()
                .anio(anio)
                .mes(mes)
                .createdAt(filas.get(0).getCreatedAt())
                .valorTotal(money(total))
                .items(items)
                .build();
    }

    private static String normalizarCodigo(String codigo) {
        String valor = blankToNull(codigo);
        if (valor == null) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
        return valor;
    }

    private static String normalizarRuc(String ruc) {
        String valor = blankToNull(ruc);
        if (valor == null) {
            return null;
        }
        if (!valor.matches("\\d{11}")) {
            throw new IllegalArgumentException("El RUC del proveedor debe tener 11 dígitos");
        }
        return valor;
    }

    private static LocalDate fechaDe(LocalDate fecha) {
        return fecha == null ? LocalDate.now() : fecha;
    }

    private static String centroDe(String propio, String fallback) {
        String valor = blankToNull(propio);
        if (valor != null) {
            return valor;
        }
        String obra = blankToNull(fallback);
        return obra == null ? "SIN_CENTRO" : obra;
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

    private record StockSaldo(BigDecimal cantidad, BigDecimal cmp) {
    }
}
