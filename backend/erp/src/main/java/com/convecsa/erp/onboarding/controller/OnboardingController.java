package com.convecsa.erp.onboarding.controller;

import com.convecsa.erp.onboarding.dto.PlantillaOnboardingDTO;
import com.convecsa.erp.onboarding.service.InterfazOnboardingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
}
