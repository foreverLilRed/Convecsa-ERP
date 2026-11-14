package com.convecsa.erp.soma.service;

import com.convecsa.erp.soma.dto.SomaPreUsoBloqueoDTO;
import com.convecsa.erp.soma.dto.SomaPreUsoDTO;
import com.convecsa.erp.soma.dto.SomaPreUsoRequest;
import java.time.LocalDate;
import java.util.List;

public interface InterfazSomaPreUsoService {

    List<SomaPreUsoDTO> listar(Long proyectoId);

    SomaPreUsoDTO crear(Long proyectoId, SomaPreUsoRequest request);

    List<SomaPreUsoBloqueoDTO> listarBloqueos(Long proyectoId, LocalDate fecha);

    boolean equipoBloqueadoEn(Long asignacionId, LocalDate fecha);
}
