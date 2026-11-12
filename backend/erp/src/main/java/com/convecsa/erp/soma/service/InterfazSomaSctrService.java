package com.convecsa.erp.soma.service;

import com.convecsa.erp.soma.dto.SomaSctrNominaGuardarRequest;
import com.convecsa.erp.soma.dto.SomaSctrPolizaDTO;
import com.convecsa.erp.soma.dto.SomaSctrPolizaRequest;
import com.convecsa.erp.soma.dto.SomaTrabajadorObraDTO;
import java.util.List;

public interface InterfazSomaSctrService {

    List<SomaSctrPolizaDTO> listar(Long proyectoId);

    SomaSctrPolizaDTO crear(Long proyectoId, SomaSctrPolizaRequest request);

    SomaSctrPolizaDTO guardarNomina(Long proyectoId, Long polizaId, SomaSctrNominaGuardarRequest request);

    SomaSctrPolizaDTO anular(Long proyectoId, Long polizaId);

    List<SomaTrabajadorObraDTO> sugerirNomina(Long proyectoId, Integer anio, Integer mes);
}
