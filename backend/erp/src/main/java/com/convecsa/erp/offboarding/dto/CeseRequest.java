package com.convecsa.erp.offboarding.dto;

import com.convecsa.erp.offboarding.domain.TipoCese;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CeseRequest {

    @NotNull
    private Long personaId;

    @NotNull
    private LocalDate fechaCese;

    @NotNull
    private TipoCese tipoCese;

    private String motivo;

    private BigDecimal cts;

    private BigDecimal gratificacion;

    private BigDecimal vacaciones;
}
