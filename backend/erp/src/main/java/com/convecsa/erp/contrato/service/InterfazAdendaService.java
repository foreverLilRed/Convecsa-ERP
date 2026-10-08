package com.convecsa.erp.contrato.service;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.contrato.dto.AdendaDTO;
import com.convecsa.erp.contrato.dto.AdendaRequest;

public interface InterfazAdendaService {

    PagedResponse<AdendaDTO> listar(int page, int size);

    AdendaDTO crear(AdendaRequest request);

    String documento(Long id);
}
