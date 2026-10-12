package com.convecsa.erp.offboarding.service;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.offboarding.dto.CeseDTO;
import com.convecsa.erp.offboarding.dto.CeseRequest;
import com.convecsa.erp.offboarding.dto.GeneracionOffboardingRequest;
import com.convecsa.erp.offboarding.dto.LiquidacionPreviewDTO;
import com.convecsa.erp.onboarding.dto.GeneracionOnboardingDTO;
import com.convecsa.erp.onboarding.dto.PlantillaOnboardingDTO;
import java.time.LocalDate;
import java.util.List;

public interface InterfazOffboardingService {

    LiquidacionPreviewDTO previewLiquidacion(Long personaId, LocalDate fechaCese);

    CeseDTO registrarCese(CeseRequest request);

    PagedResponse<CeseDTO> listarCeses(int page, int size);

    CeseDTO obtenerCese(Long id);

    List<PlantillaOnboardingDTO> listarPlantillas();

    GeneracionOnboardingDTO crearGeneracion(Long ceseId, GeneracionOffboardingRequest request);
}
