package com.convecsa.erp.persona.controller;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.persona.dto.PersonaDTO;
import com.convecsa.erp.persona.dto.PersonaListQuery;
import com.convecsa.erp.persona.dto.PersonaRequest;
import com.convecsa.erp.persona.dto.PersonaResumenDTO;
import com.convecsa.erp.persona.service.InterfazPersonaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestion-humana/personas")
@RequiredArgsConstructor
@Validated
public class PersonaController {

    private final InterfazPersonaService personaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersonaDTO crear(@Valid @RequestBody PersonaRequest request) {
        return personaService.crear(request);
    }

    @GetMapping
    public PagedResponse<PersonaResumenDTO> listar(@Valid @ModelAttribute PersonaListQuery query) {
        return personaService.listar(query);
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(@Valid @ModelAttribute PersonaListQuery query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("personas.xlsx").build());
        return new ResponseEntity<>(personaService.exportar(query), headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public PersonaDTO obtener(@PathVariable Long id) {
        return personaService.obtener(id);
    }

    @PutMapping("/{id}")
    public PersonaDTO actualizar(@PathVariable Long id, @Valid @RequestBody PersonaRequest request) {
        return personaService.actualizar(id, request);
    }

    @PatchMapping("/{id}/enable")
    public PersonaDTO habilitar(@PathVariable Long id) {
        return personaService.habilitar(id);
    }

    @PatchMapping("/{id}/disable")
    public PersonaDTO deshabilitar(@PathVariable Long id) {
        return personaService.deshabilitar(id);
    }
}
