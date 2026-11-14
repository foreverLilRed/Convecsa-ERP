package com.convecsa.erp.soma.service;

import com.convecsa.erp.soma.dto.SomaEventoDTO;
import com.convecsa.erp.soma.dto.SomaEventoRequest;
import com.convecsa.erp.soma.dto.SomaEventoSyncDmDTO;
import com.convecsa.erp.soma.dto.SomaHallazgoDTO;
import com.convecsa.erp.soma.dto.SomaHallazgoRequest;
import java.util.List;

public interface InterfazSomaEventoService {

    List<SomaEventoDTO> listar(Long proyectoId);

    SomaEventoDTO crear(Long proyectoId, SomaEventoRequest request);

    SomaEventoDTO actualizar(Long proyectoId, Long eventoId, SomaEventoRequest request);

    SomaHallazgoDTO agregarHallazgo(Long proyectoId, Long eventoId, SomaHallazgoRequest request);

    SomaEventoDTO cerrar(Long proyectoId, Long eventoId);

    SomaEventoSyncDmDTO sincronizarDm(Long proyectoId, Long eventoId);
}
