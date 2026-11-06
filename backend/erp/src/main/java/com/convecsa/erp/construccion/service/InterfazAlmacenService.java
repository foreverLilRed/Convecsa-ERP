package com.convecsa.erp.construccion.service;

import com.convecsa.erp.construccion.dto.AjusteAlmacenRequest;
import com.convecsa.erp.construccion.dto.AlmacenDTO;
import com.convecsa.erp.construccion.dto.CierreInventarioRequest;
import com.convecsa.erp.construccion.dto.DevolucionRequest;
import com.convecsa.erp.construccion.dto.InventarioCierreDTO;
import com.convecsa.erp.construccion.dto.MaterialDTO;
import com.convecsa.erp.construccion.dto.MaterialRequest;
import com.convecsa.erp.construccion.dto.MovimientoAlmacenDTO;
import com.convecsa.erp.construccion.dto.OrdenCompraDTO;
import com.convecsa.erp.construccion.dto.OrdenCompraRequest;
import com.convecsa.erp.construccion.dto.RecepcionRequest;
import com.convecsa.erp.construccion.dto.RequerimientoDTO;
import com.convecsa.erp.construccion.dto.RequerimientoRequest;
import com.convecsa.erp.construccion.dto.SugeridoApuDTO;
import com.convecsa.erp.construccion.dto.ValeSalidaDTO;
import com.convecsa.erp.construccion.dto.ValeSalidaRequest;
import java.util.List;

public interface InterfazAlmacenService {

    List<MaterialDTO> listarMateriales();

    MaterialDTO crearMaterial(MaterialRequest request);

    MaterialDTO actualizarMaterial(Long materialId, MaterialRequest request);

    AlmacenDTO obtenerAlmacen(Long proyectoId);

    List<MovimientoAlmacenDTO> listarKardex(Long proyectoId, Long materialId);

    AlmacenDTO registrarAjuste(Long proyectoId, AjusteAlmacenRequest request);

    InventarioCierreDTO cerrarInventario(Long proyectoId, CierreInventarioRequest request);

    List<InventarioCierreDTO> listarCierres(Long proyectoId);

    List<RequerimientoDTO> listarRequerimientos(Long proyectoId);

    RequerimientoDTO obtenerRequerimiento(Long proyectoId, Long requerimientoId);

    RequerimientoDTO crearRequerimiento(Long proyectoId, RequerimientoRequest request);

    RequerimientoDTO guardarRequerimiento(Long proyectoId, Long requerimientoId, RequerimientoRequest request);

    RequerimientoDTO aprobarRequerimiento(Long proyectoId, Long requerimientoId);

    RequerimientoDTO anularRequerimiento(Long proyectoId, Long requerimientoId);

    List<SugeridoApuDTO> sugeridosApu(Long proyectoId);

    List<OrdenCompraDTO> listarOrdenesCompra(Long proyectoId);

    OrdenCompraDTO obtenerOrdenCompra(Long proyectoId, Long ordenId);

    OrdenCompraDTO crearOrdenCompra(Long proyectoId, OrdenCompraRequest request);

    OrdenCompraDTO guardarOrdenCompra(Long proyectoId, Long ordenId, OrdenCompraRequest request);

    OrdenCompraDTO emitirOrdenCompra(Long proyectoId, Long ordenId);

    OrdenCompraDTO cerrarOrdenCompra(Long proyectoId, Long ordenId);

    OrdenCompraDTO recepcionar(Long proyectoId, Long ordenId, RecepcionRequest request);

    OrdenCompraDTO devolverProveedor(Long proyectoId, Long ordenId, DevolucionRequest request);

    List<ValeSalidaDTO> listarVales(Long proyectoId);

    ValeSalidaDTO crearVale(Long proyectoId, ValeSalidaRequest request);

    ValeSalidaDTO devolverVale(Long proyectoId, Long valeId, DevolucionRequest request);
}
