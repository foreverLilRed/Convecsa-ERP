package com.convecsa.erp.mof.service;

import com.convecsa.erp.mof.dto.MofPuestoDTO;
import com.convecsa.erp.mof.dto.MofPuestoRequest;
import com.convecsa.erp.mof.dto.MofPuestoResumenDTO;
import java.util.List;

public interface InterfazMofService {

    List<MofPuestoResumenDTO> listar();

    MofPuestoDTO obtener(Long id);

    MofPuestoDTO guardar(Long id, MofPuestoRequest request);

    String documento(Long id);

    String documentoCompleto();
}
