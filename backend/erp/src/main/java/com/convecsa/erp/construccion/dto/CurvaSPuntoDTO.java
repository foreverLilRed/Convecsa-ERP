package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurvaSPuntoDTO {

    private final int anio;
    private final int mes;
    private final BigDecimal programadoFisico;
    private final BigDecimal ejecutadoFisico;
    private final BigDecimal programadoEconomico;
    private final BigDecimal ejecutadoEconomico;
    private final BigDecimal desvioFisico;
    private final BigDecimal desvioEconomico;
}
