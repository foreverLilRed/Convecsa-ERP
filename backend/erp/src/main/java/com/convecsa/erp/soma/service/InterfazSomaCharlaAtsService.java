package com.convecsa.erp.soma.service;

import com.convecsa.erp.soma.dto.SomaCharlaAsistenteDTO;
import com.convecsa.erp.soma.dto.SomaCharlaAtsDTO;
import com.convecsa.erp.soma.dto.SomaCharlaAtsRequest;
import java.time.LocalDate;
import java.util.List;

public interface InterfazSomaCharlaAtsService {

    List<SomaCharlaAtsDTO> listar(Long proyectoId, LocalDate fecha);

    SomaCharlaAtsDTO crear(Long proyectoId, SomaCharlaAtsRequest request);

    List<SomaCharlaAsistenteDTO> precargarCuadrilla(Long proyectoId, Long cuadrillaId, LocalDate fecha);
}
