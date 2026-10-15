package com.convecsa.erp.nomina.service;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.nomina.dto.AfpDTO;
import com.convecsa.erp.nomina.dto.AsientoContableDTO;
import com.convecsa.erp.nomina.dto.BoletaDocumentoHtml;
import com.convecsa.erp.nomina.dto.BoletaEnvioResumenDTO;
import com.convecsa.erp.nomina.dto.BoletaLoteRequest;
import com.convecsa.erp.nomina.dto.BoletaPagoDTO;
import com.convecsa.erp.nomina.dto.BoletaPagoResumenDTO;
import com.convecsa.erp.nomina.dto.BoletaUpdateRequest;
import com.convecsa.erp.nomina.dto.PeriodoNominaDTO;
import com.convecsa.erp.nomina.dto.PeriodoNominaRequest;
import com.convecsa.erp.nomina.dto.NominaDocumentoConfiguracionDTO;
import com.convecsa.erp.nomina.dto.NominaDocumentoConfiguracionRequest;
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

    byte[] pdfBoleta(Long id);

    byte[] pdfBoletas(Long periodoId, BoletaLoteRequest request);

    BoletaEnvioResumenDTO enviarBoletas(Long periodoId, BoletaLoteRequest request);

    AsientoContableDTO obtenerAsientoContable(Long periodoId);

    byte[] pdfAsientoContable(Long periodoId);

    NominaDocumentoConfiguracionDTO obtenerConfiguracionDocumento();

    NominaDocumentoConfiguracionDTO guardarConfiguracionDocumento(NominaDocumentoConfiguracionRequest request);
}
