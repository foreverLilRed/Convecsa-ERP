package com.convecsa.erp.soma.service;

import com.convecsa.erp.soma.dto.SomaComiteActaDTO;
import com.convecsa.erp.soma.dto.SomaComiteActaRequest;
import com.convecsa.erp.soma.dto.SomaComiteMiembroDTO;
import com.convecsa.erp.soma.dto.SomaComiteMiembroRequest;
import com.convecsa.erp.soma.dto.SomaComiteRequerimientoDTO;
import java.util.List;

public interface InterfazSomaComiteService {

    SomaComiteRequerimientoDTO requerimiento(Long proyectoId);

    List<SomaComiteMiembroDTO> listarMiembros(Long proyectoId);

    SomaComiteMiembroDTO crearMiembro(Long proyectoId, SomaComiteMiembroRequest request);

    SomaComiteMiembroDTO darBaja(Long proyectoId, Long miembroId);

    List<SomaComiteActaDTO> listarActas(Long proyectoId);

    SomaComiteActaDTO crearActa(Long proyectoId, SomaComiteActaRequest request);
}
