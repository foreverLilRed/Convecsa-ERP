package com.convecsa.erp.soma.service;

import com.convecsa.erp.construccion.dto.AlertaObraDTO;
import java.util.List;

public interface InterfazSomaAlertaService {

    List<AlertaObraDTO> alertas(Long proyectoId);
}
