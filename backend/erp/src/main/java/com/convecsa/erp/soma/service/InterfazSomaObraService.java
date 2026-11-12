package com.convecsa.erp.soma.service;

import com.convecsa.erp.soma.dto.SomaResumenObraDTO;

public interface InterfazSomaObraService {

    SomaResumenObraDTO obtenerResumen(Long proyectoId);
}
