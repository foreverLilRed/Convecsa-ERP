package com.convecsa.erp.contrato.controller;

import com.convecsa.erp.contrato.dto.ContratoDTO;
import com.convecsa.erp.contrato.dto.ContratoRequest;
import com.convecsa.erp.contrato.dto.ContratosCatalogosDTO;
import com.convecsa.erp.contrato.dto.ContratosResumeDTO;
import com.convecsa.erp.contrato.service.InterfazContratoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestion-humana/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final InterfazContratoService contratoService;

    @GetMapping("/resume")
    public ContratosResumeDTO resume() {
        return contratoService.resume();
    }

    @GetMapping("/catalogos")
    public ContratosCatalogosDTO catalogos() {
        return contratoService.catalogos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoDTO crear(@Valid @RequestBody ContratoRequest request) {
        return contratoService.crear(request);
    }

    @GetMapping(value = "/{id}/documento", produces = MediaType.TEXT_HTML_VALUE)
    public String documento(@PathVariable Long id) {
        return contratoService.documento(id);
    }
}
