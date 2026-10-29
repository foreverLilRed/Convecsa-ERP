package com.convecsa.erp.construccion.support;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CcLiquidacionResult {

    private final BigDecimal tarifaDia;
    private final BigDecimal tarifaHora;
    private final BigDecimal totalHaberes;
    private final BigDecimal totalAfecto;
    private final BigDecimal totalDescuentos;
    private final BigDecimal totalAportes;
    private final BigDecimal neto;
    private final List<CcConceptoCalculado> conceptos;
}
