package com.convecsa.erp.construccion.service;

import com.convecsa.erp.construccion.dto.CobranzaObraDTO;
import com.convecsa.erp.construccion.dto.CobranzaObraRequest;
import com.convecsa.erp.construccion.dto.FacturaObraDTO;
import com.convecsa.erp.construccion.dto.FacturaVencimientoRequest;
import com.convecsa.erp.construccion.dto.FlujoCajaDTO;
import com.convecsa.erp.construccion.dto.LiberacionRetencionRequest;
import com.convecsa.erp.construccion.dto.OperacionFactoringDTO;
import com.convecsa.erp.construccion.dto.OperacionFactoringRequest;
import com.convecsa.erp.construccion.dto.RetencionesDTO;
import java.time.LocalDate;
import java.util.List;

public interface InterfazFinanciamientoService {

    List<FacturaObraDTO> listarFacturas(Long proyectoId);

    FacturaObraDTO obtenerFactura(Long proyectoId, Long facturaId);

    FacturaObraDTO actualizarVencimiento(Long proyectoId, Long facturaId, FacturaVencimientoRequest request);

    List<CobranzaObraDTO> listarCobranzas(Long proyectoId);

    FacturaObraDTO registrarCobranza(Long proyectoId, CobranzaObraRequest request);

    RetencionesDTO obtenerRetenciones(Long proyectoId);

    RetencionesDTO liberarRetencion(Long proyectoId, LiberacionRetencionRequest request);

    List<OperacionFactoringDTO> listarFactoring(Long proyectoId);

    OperacionFactoringDTO cederFactura(Long proyectoId, OperacionFactoringRequest request);

    OperacionFactoringDTO adelantarFactoring(Long proyectoId, Long operacionId);

    OperacionFactoringDTO cobrarFactoring(Long proyectoId, Long operacionId);

    OperacionFactoringDTO impagarFactoring(Long proyectoId, Long operacionId);

    OperacionFactoringDTO revertirFactoring(Long proyectoId, Long operacionId);

    FlujoCajaDTO flujoCaja(Long proyectoId, LocalDate desde, LocalDate hasta);
}
