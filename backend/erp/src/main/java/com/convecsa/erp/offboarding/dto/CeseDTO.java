package com.convecsa.erp.offboarding.dto;

import com.convecsa.erp.offboarding.domain.TipoCese;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CeseDTO {

    private final Long id;
    private final Long personaId;
    private final String personaNombres;
    private final String personaApellidos;
    private final String numeroDocumento;
    private final Long contratoId;
    private final LocalDate fechaCese;
    private final TipoCese tipoCese;
    private final String motivo;
    private final LiquidacionPreviewDTO liquidacion;
    private final Instant createdAt;
}
