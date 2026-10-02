package com.convecsa.erp.contrato.service;

import com.convecsa.erp.contrato.dto.ContratoDTO;
import com.convecsa.erp.contrato.dto.ContratoRequest;
import com.convecsa.erp.contrato.dto.ContratosCatalogosDTO;
import com.convecsa.erp.contrato.dto.ContratosResumeDTO;

public interface InterfazContratoService {

    ContratosResumeDTO resume();

    ContratosCatalogosDTO catalogos();

    ContratoDTO crear(ContratoRequest request);

    String documento(Long id);
}
