package com.convecsa.erp.offboarding.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LiquidacionPreviewDTO {

    private final BigDecimal sueldo;
    private final BigDecimal cts;
    private final BigDecimal gratificacion;
    private final BigDecimal vacaciones;
    private final BigDecimal total;
    private final String tiempoServicio;
    private final LocalDate fechaInicio;
    private final LocalDate fechaCese;
}
