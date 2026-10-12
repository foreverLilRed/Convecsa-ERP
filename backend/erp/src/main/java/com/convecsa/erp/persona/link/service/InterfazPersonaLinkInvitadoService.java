package com.convecsa.erp.persona.link.service;

import com.convecsa.erp.persona.dto.PersonaDTO;
import com.convecsa.erp.persona.dto.PersonaRequest;
import com.convecsa.erp.persona.link.dto.PersonaLinkAltaRequest;
import com.convecsa.erp.persona.link.dto.PersonaLinkInvitadoDTO;

public interface InterfazPersonaLinkInvitadoService {

    PersonaLinkInvitadoDTO crear(Long personaId);

    PersonaLinkInvitadoDTO crearAlta(PersonaLinkAltaRequest request);

    PersonaDTO obtenerPorToken(String token);

    PersonaDTO actualizarPorToken(String token, PersonaRequest request);
}
