package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ComparativoCentroDTO {

    private final String centroCosto;
    private final BigDecimal metaMo;
    private final BigDecimal realMo;
    private final BigDecimal desvio;
    private final BigDecimal metaMaterial;
    private final BigDecimal realMaterial;
    private final BigDecimal desvioMaterial;
    private final BigDecimal metaEquipo;
    private final BigDecimal realEquipo;
    private final BigDecimal desvioEquipo;
    private final BigDecimal metaSubcontrato;
    private final BigDecimal realSubcontrato;
    private final BigDecimal desvioSubcontrato;
}
