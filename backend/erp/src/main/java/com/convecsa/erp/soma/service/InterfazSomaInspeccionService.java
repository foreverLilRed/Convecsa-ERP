package com.convecsa.erp.soma.service;

import com.convecsa.erp.soma.domain.EstadoHallazgoSoma;
import com.convecsa.erp.soma.dto.SomaHallazgoDTO;
import com.convecsa.erp.soma.dto.SomaHallazgoLevantarRequest;
import com.convecsa.erp.soma.dto.SomaHallazgoRequest;
import com.convecsa.erp.soma.dto.SomaInspeccionDTO;
import com.convecsa.erp.soma.dto.SomaInspeccionRequest;
import java.util.List;

public interface InterfazSomaInspeccionService {

    List<SomaInspeccionDTO> listarInspecciones(Long proyectoId);

    SomaInspeccionDTO crearInspeccion(Long proyectoId, SomaInspeccionRequest request);

    List<SomaHallazgoDTO> listarHallazgos(Long proyectoId, EstadoHallazgoSoma estado, Boolean vencidos);

    SomaHallazgoDTO crearHallazgo(Long proyectoId, SomaHallazgoRequest request);

    SomaHallazgoDTO marcarEnProceso(Long proyectoId, Long hallazgoId);

    SomaHallazgoDTO levantar(Long proyectoId, Long hallazgoId, SomaHallazgoLevantarRequest request);
}
