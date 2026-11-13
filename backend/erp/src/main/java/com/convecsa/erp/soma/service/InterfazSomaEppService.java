package com.convecsa.erp.soma.service;

import com.convecsa.erp.soma.dto.SomaEntregaEppDTO;
import com.convecsa.erp.soma.dto.SomaEntregaEppRequest;
import com.convecsa.erp.soma.dto.SomaEppItemEstadoRequest;
import com.convecsa.erp.soma.dto.SomaEppMaterialDTO;
import java.util.List;

public interface InterfazSomaEppService {

    List<SomaEntregaEppDTO> listar(Long proyectoId);

    SomaEntregaEppDTO crear(Long proyectoId, SomaEntregaEppRequest request);

    SomaEntregaEppDTO cambiarEstadoItem(Long proyectoId, Long itemId, SomaEppItemEstadoRequest request);

    List<SomaEppMaterialDTO> listarMateriales(Long proyectoId);

    byte[] kardexPdf(Long proyectoId, Long personaId);
}
