package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FlujoCajaDTO {

    private final BigDecimal porCobrar;
    private final BigDecimal retenido;
    private final BigDecimal cedido;
    private final BigDecimal ingresos;
    private final BigDecimal egresos;
    private final BigDecimal costoFactoring;
    private final BigDecimal neto;
    private final List<FlujoCajaPuntoDTO> puntos;
}
