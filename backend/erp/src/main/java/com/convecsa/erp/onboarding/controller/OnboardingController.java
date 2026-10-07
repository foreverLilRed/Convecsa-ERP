package com.convecsa.erp.onboarding.controller;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.onboarding.dto.GeneracionOnboardingDTO;
import com.convecsa.erp.onboarding.dto.GeneracionOnboardingRequest;
import com.convecsa.erp.onboarding.dto.OnboardingDocumentoHtml;
import com.convecsa.erp.onboarding.dto.PlantillaOnboardingDTO;
import com.convecsa.erp.onboarding.service.InterfazOnboardingService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestion-humana/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final InterfazOnboardingService onboardingService;

    @GetMapping("/plantillas")
    public List<PlantillaOnboardingDTO> plantillas() {
        return onboardingService.listarPlantillas();
    }

    @GetMapping(value = "/documento", produces = MediaType.TEXT_HTML_VALUE)
    public String documento(
            @RequestParam Long personaId,
            @RequestParam String codigos) {
        return onboardingService.documento(personaId, codigos);
    }

    @PostMapping("/generaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public GeneracionOnboardingDTO crearGeneracion(@Valid @RequestBody GeneracionOnboardingRequest request) {
        return onboardingService.crearGeneracion(request);
    }

    @GetMapping("/generaciones")
    public PagedResponse<GeneracionOnboardingDTO> listarGeneraciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return onboardingService.listarGeneraciones(page, size);
    }

    @GetMapping(value = "/generaciones/{id}/documento", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> documentoGeneracion(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean descarga,
            @RequestParam(defaultValue = "false") boolean imprimir) {
        OnboardingDocumentoHtml documento = onboardingService.documentoGeneracion(id, descarga, imprimir);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8));
        ContentDisposition disposition = descarga
                ? ContentDisposition.attachment().filename(documento.getFilename(), StandardCharsets.UTF_8).build()
                : ContentDisposition.inline().filename(documento.getFilename(), StandardCharsets.UTF_8).build();
        headers.setContentDisposition(disposition);
        return new ResponseEntity<>(documento.getHtml(), headers, HttpStatus.OK);
    }
}
