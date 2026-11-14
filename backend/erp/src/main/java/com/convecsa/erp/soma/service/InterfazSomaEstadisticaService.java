package com.convecsa.erp.soma.service;

import com.convecsa.erp.soma.dto.SomaEstadisticaAnioDTO;

public interface InterfazSomaEstadisticaService {

    SomaEstadisticaAnioDTO porObra(Long proyectoId, Integer anio);

    SomaEstadisticaAnioDTO corporativo(Integer anio);
}
