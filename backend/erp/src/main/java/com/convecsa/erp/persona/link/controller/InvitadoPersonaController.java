package com.convecsa.erp.persona.link.controller;

import com.convecsa.erp.persona.dto.PersonaDTO;
import com.convecsa.erp.persona.dto.PersonaRequest;
import com.convecsa.erp.persona.link.service.InterfazPersonaLinkInvitadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invitado/personas")
@RequiredArgsConstructor
@Validated
public class InvitadoPersonaController {

    private final InterfazPersonaLinkInvitadoService linkService;

    @GetMapping("/{token}")
    public PersonaDTO obtener(@PathVariable String token) {
        return linkService.obtenerPorToken(token);
    }

    @PutMapping("/{token}")
    public PersonaDTO actualizar(@PathVariable String token, @Valid @RequestBody PersonaRequest request) {
        return linkService.actualizarPorToken(token, request);
    }
}
