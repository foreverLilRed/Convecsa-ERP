package com.convecsa.erp.contrato.controller;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.contrato.domain.EstadoContrato;
import com.convecsa.erp.contrato.dto.AdendaDTO;
import com.convecsa.erp.contrato.dto.AdendaRequest;
import com.convecsa.erp.contrato.dto.ContratoDTO;
import com.convecsa.erp.contrato.dto.ContratoRequest;
import com.convecsa.erp.contrato.dto.ContratosCatalogosDTO;
import com.convecsa.erp.contrato.dto.ContratosResumeDTO;
import com.convecsa.erp.contrato.service.InterfazAdendaService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestion-humana/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final InterfazContratoService contratoService;
    private final InterfazAdendaService adendaService;

    @GetMapping("/resume")
    public ContratosResumeDTO resume() {
        return contratoService.resume();
    }

    @GetMapping("/catalogos")
    public ContratosCatalogosDTO catalogos() {
        return contratoService.catalogos();
    }

    @GetMapping
    public PagedResponse<ContratoDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) EstadoContrato estado) {
        return contratoService.listar(page, size, estado);
    }

    @GetMapping("/adendas")
    public PagedResponse<AdendaDTO> listarAdendas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adendaService.listar(page, size);
    }

    @PostMapping("/adendas")
    @ResponseStatus(HttpStatus.CREATED)
    public AdendaDTO crearAdenda(@Valid @RequestBody AdendaRequest request) {
        return adendaService.crear(request);
    }

    @GetMapping(value = "/adendas/{id}/documento", produces = MediaType.TEXT_HTML_VALUE)
    public String documentoAdenda(@PathVariable Long id) {
        return adendaService.documento(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoDTO crear(@Valid @RequestBody ContratoRequest request) {
        return contratoService.crear(request);
    }

    @GetMapping("/personas/{personaId}/ultimo")
    public ContratoDTO obtenerUltimoContrato(@PathVariable Long personaId) {
        return contratoService.obtenerUltimoContrato(personaId);
    }

    @GetMapping(value = "/{id}/documento", produces = MediaType.TEXT_HTML_VALUE)
    public String documento(@PathVariable Long id) {
        return contratoService.documento(id);
    }
}
