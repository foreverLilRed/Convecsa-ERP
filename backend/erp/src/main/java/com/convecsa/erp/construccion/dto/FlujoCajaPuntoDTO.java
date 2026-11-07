package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FlujoCajaPuntoDTO {

    private final int anio;
    private final int mes;
    private final BigDecimal ingresos;
    private final BigDecimal egresos;
    private final BigDecimal costoFactoring;
    private final BigDecimal neto;
}
