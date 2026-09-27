package com.convecsa.erp.persona.controller;

import com.convecsa.erp.persona.dto.PersonaDTO;
import com.convecsa.erp.persona.dto.PersonaRequest;
import com.convecsa.erp.persona.service.InterfazPersonaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestion-humana/personas")
@RequiredArgsConstructor
public class PersonaController {

    private final InterfazPersonaService personaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersonaDTO crear(@Valid @RequestBody PersonaRequest request) {
        return personaService.crear(request);
    }
}
