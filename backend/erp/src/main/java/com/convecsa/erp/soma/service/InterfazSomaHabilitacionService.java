package com.convecsa.erp.soma.service;

import com.convecsa.erp.soma.dto.SomaHabilitacionDTO;
import java.time.LocalDate;

public interface InterfazSomaHabilitacionService {

    SomaHabilitacionDTO evaluarObra(Long proyectoId, LocalDate fecha);
}
