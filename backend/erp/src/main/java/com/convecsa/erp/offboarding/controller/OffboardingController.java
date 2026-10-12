package com.convecsa.erp.offboarding.controller;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.offboarding.dto.CeseDTO;
import com.convecsa.erp.offboarding.dto.CeseRequest;
import com.convecsa.erp.offboarding.dto.GeneracionOffboardingRequest;
import com.convecsa.erp.offboarding.dto.LiquidacionPreviewDTO;
import com.convecsa.erp.offboarding.service.InterfazOffboardingService;
import com.convecsa.erp.onboarding.dto.GeneracionOnboardingDTO;
import com.convecsa.erp.onboarding.dto.PlantillaOnboardingDTO;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestion-humana/offboarding")
@RequiredArgsConstructor
public class OffboardingController {

    private final InterfazOffboardingService offboardingService;

    @GetMapping("/liquidacion")
    public LiquidacionPreviewDTO preview(
            @RequestParam Long personaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCese) {
        return offboardingService.previewLiquidacion(personaId, fechaCese);
    }

    @PostMapping("/ceses")
    @ResponseStatus(HttpStatus.CREATED)
    public CeseDTO registrar(@Valid @RequestBody CeseRequest request) {
        return offboardingService.registrarCese(request);
    }

    @GetMapping("/ceses")
    public PagedResponse<CeseDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return offboardingService.listarCeses(page, size);
    }

    @GetMapping("/ceses/{id}")
    public CeseDTO obtener(@PathVariable Long id) {
        return offboardingService.obtenerCese(id);
    }

    @GetMapping("/plantillas")
    public List<PlantillaOnboardingDTO> plantillas() {
        return offboardingService.listarPlantillas();
    }

    @PostMapping("/ceses/{id}/generaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public GeneracionOnboardingDTO crearGeneracion(
            @PathVariable Long id, @Valid @RequestBody GeneracionOffboardingRequest request) {
        return offboardingService.crearGeneracion(id, request);
    }
}
