package com.convecsa.erp.nomina.service;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.nomina.dto.AfpDTO;
import com.convecsa.erp.nomina.dto.BoletaDocumentoHtml;
import com.convecsa.erp.nomina.dto.BoletaPagoDTO;
import com.convecsa.erp.nomina.dto.BoletaPagoResumenDTO;
import com.convecsa.erp.nomina.dto.BoletaUpdateRequest;
import com.convecsa.erp.nomina.dto.PeriodoNominaDTO;
import com.convecsa.erp.nomina.dto.PeriodoNominaRequest;
import java.util.List;

public interface InterfazNominaService {

    List<AfpDTO> listarAfp();

    PeriodoNominaDTO crearPeriodo(PeriodoNominaRequest request);

    PagedResponse<PeriodoNominaDTO> listarPeriodos(int page, int size);

    PeriodoNominaDTO obtenerPeriodo(Long id);

    PeriodoNominaDTO generarBoletas(Long periodoId);

    PeriodoNominaDTO recalcularPeriodo(Long periodoId);

    PeriodoNominaDTO cerrarPeriodo(Long periodoId);

    PagedResponse<BoletaPagoResumenDTO> listarBoletas(Long periodoId, int page, int size);

    BoletaPagoDTO obtenerBoleta(Long id);

    BoletaPagoDTO actualizarBoleta(Long id, BoletaUpdateRequest request);

    BoletaPagoDTO recalcularBoleta(Long id);

    BoletaDocumentoHtml documentoBoleta(Long id, boolean descarga, boolean imprimir);
}
