package com.convecsa.erp.soma.service;

import com.convecsa.erp.soma.dto.SomaMonitoreoDTO;
import com.convecsa.erp.soma.dto.SomaMonitoreoRequest;
import com.convecsa.erp.soma.dto.SomaResiduoDTO;
import com.convecsa.erp.soma.dto.SomaResiduoRequest;
import java.util.List;

public interface InterfazSomaAmbientalService {

    List<SomaResiduoDTO> listarResiduos(Long proyectoId);

    SomaResiduoDTO crearResiduo(Long proyectoId, SomaResiduoRequest request);

    List<SomaMonitoreoDTO> listarMonitoreos(Long proyectoId);

    SomaMonitoreoDTO crearMonitoreo(Long proyectoId, SomaMonitoreoRequest request);
}
