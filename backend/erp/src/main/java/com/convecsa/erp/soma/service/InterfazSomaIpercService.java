package com.convecsa.erp.soma.service;

import com.convecsa.erp.soma.dto.SomaIpercFilaDTO;
import com.convecsa.erp.soma.dto.SomaIpercFilaRequest;
import java.util.List;

public interface InterfazSomaIpercService {

    List<SomaIpercFilaDTO> listar(Long proyectoId, Long partidaId, Boolean incluirInactivos);

    SomaIpercFilaDTO crear(Long proyectoId, SomaIpercFilaRequest request);

    SomaIpercFilaDTO actualizar(Long proyectoId, Long filaId, SomaIpercFilaRequest request);

    SomaIpercFilaDTO desactivar(Long proyectoId, Long filaId);
}
