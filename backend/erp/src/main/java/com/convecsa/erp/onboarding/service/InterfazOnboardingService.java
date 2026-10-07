package com.convecsa.erp.onboarding.service;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.onboarding.dto.GeneracionOnboardingDTO;
import com.convecsa.erp.onboarding.dto.GeneracionOnboardingRequest;
import com.convecsa.erp.onboarding.dto.OnboardingDocumentoHtml;
import com.convecsa.erp.onboarding.dto.PlantillaOnboardingDTO;
import java.util.List;

public interface InterfazOnboardingService {

    List<PlantillaOnboardingDTO> listarPlantillas();

    String documento(Long personaId, String codigos);

    GeneracionOnboardingDTO crearGeneracion(GeneracionOnboardingRequest request);

    PagedResponse<GeneracionOnboardingDTO> listarGeneraciones(int page, int size);

    OnboardingDocumentoHtml documentoGeneracion(Long id, boolean descarga, boolean imprimir);
}
