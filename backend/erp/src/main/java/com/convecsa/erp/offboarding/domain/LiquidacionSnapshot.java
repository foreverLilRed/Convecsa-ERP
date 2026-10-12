package com.convecsa.erp.offboarding.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LiquidacionSnapshot {

    private BigDecimal sueldo;
    private BigDecimal cts;
    private BigDecimal gratificacion;
    private BigDecimal vacaciones;
    private BigDecimal total;
    private String tiempoServicio;
    private LocalDate fechaInicio;
    private LocalDate fechaCese;
}
