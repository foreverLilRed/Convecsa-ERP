package com.convecsa.erp.onboarding.service;

import com.convecsa.erp.onboarding.dto.PlantillaOnboardingDTO;
import java.util.List;

public interface InterfazOnboardingService {

    List<PlantillaOnboardingDTO> listarPlantillas();

    String documento(Long personaId, String codigos);
}
