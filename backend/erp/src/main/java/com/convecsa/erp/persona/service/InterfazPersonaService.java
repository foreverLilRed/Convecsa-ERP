package com.convecsa.erp.persona.service;

import com.convecsa.erp.persona.dto.PersonaDTO;
import com.convecsa.erp.persona.dto.PersonaRequest;

public interface InterfazPersonaService {

    PersonaDTO crear(PersonaRequest request);
}
