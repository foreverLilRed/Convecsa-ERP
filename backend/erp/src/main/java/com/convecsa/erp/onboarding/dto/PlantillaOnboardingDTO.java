package com.convecsa.erp.onboarding.dto;

import com.convecsa.erp.onboarding.domain.AmbitoPlantilla;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlantillaOnboardingDTO {

    private final Long id;
    private final String codigo;
    private final String categoria;
    private final String titulo;
    private final String descripcion;
    private final String baseLegal;
    private final AmbitoPlantilla ambito;
}
