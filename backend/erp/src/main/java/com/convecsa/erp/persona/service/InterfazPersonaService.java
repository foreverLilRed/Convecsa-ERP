package com.convecsa.erp.persona.service;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.persona.dto.PersonaDTO;
import com.convecsa.erp.persona.dto.PersonaListQuery;
import com.convecsa.erp.persona.dto.PersonaRequest;
import com.convecsa.erp.persona.dto.PersonaResumenDTO;

public interface InterfazPersonaService {

    PersonaDTO crear(PersonaRequest request);

    PersonaDTO obtener(Long id);

    PersonaDTO actualizar(Long id, PersonaRequest request);

    PagedResponse<PersonaResumenDTO> listar(PersonaListQuery query);
}
