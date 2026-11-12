package com.convecsa.erp.soma.service;

import com.convecsa.erp.soma.dto.SomaEmoDTO;
import com.convecsa.erp.soma.dto.SomaEmoRequest;
import java.time.LocalDate;
import java.util.List;

public interface InterfazSomaEmoService {

    SomaEmoDTO registrar(Long proyectoId, SomaEmoRequest request);

    List<SomaEmoDTO> listarCorporativo(Long personaId, LocalDate vencenAntesDe);

    List<SomaEmoDTO> listarPorObra(Long proyectoId);
}
