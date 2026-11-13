package com.convecsa.erp.soma.service;

import com.convecsa.erp.soma.dto.SomaCapacitacionAsistenteDTO;
import com.convecsa.erp.soma.dto.SomaCapacitacionDTO;
import com.convecsa.erp.soma.dto.SomaCapacitacionRequest;
import java.util.List;

public interface InterfazSomaCapacitacionService {

    List<SomaCapacitacionDTO> listar(Long proyectoId);

    List<SomaCapacitacionDTO> listarCorporativo(Integer anio);

    SomaCapacitacionDTO crear(Long proyectoId, SomaCapacitacionRequest request);

    List<SomaCapacitacionAsistenteDTO> precargarCuadrilla(Long proyectoId, Long cuadrillaId);
}
