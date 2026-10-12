package com.convecsa.erp.persona.link.controller;

import com.convecsa.erp.persona.link.dto.PersonaLinkAltaRequest;
import com.convecsa.erp.persona.link.dto.PersonaLinkInvitadoDTO;
import com.convecsa.erp.persona.link.service.InterfazPersonaLinkInvitadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestion-humana/personas")
@RequiredArgsConstructor
@Validated
public class PersonaLinkInvitadoController {

    private final InterfazPersonaLinkInvitadoService linkService;

    @PostMapping("/{personaId}/links-invitado")
    @ResponseStatus(HttpStatus.CREATED)
    public PersonaLinkInvitadoDTO crear(@PathVariable Long personaId) {
        return linkService.crear(personaId);
    }

    @PostMapping("/links-invitado/alta")
    @ResponseStatus(HttpStatus.CREATED)
    public PersonaLinkInvitadoDTO crearAlta(@Valid @RequestBody PersonaLinkAltaRequest request) {
        return linkService.crearAlta(request);
    }
}
