package com.convecsa.erp.onboarding.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneracionOnboardingDTO {

    private final Long id;
    private final Long personaId;
    private final String personaNombres;
    private final String personaApellidos;
    private final String numeroDocumento;
    private final List<String> codigos;
    private final Instant createdAt;
}
