package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ComparativoPresupuestoDTO {

    private final BigDecimal metaMo;
    private final BigDecimal realMo;
    private final BigDecimal desvio;
    private final BigDecimal desvioPct;
    private final BigDecimal metaMaterial;
    private final BigDecimal realMaterial;
    private final BigDecimal desvioMaterial;
    private final BigDecimal desvioPctMaterial;
    private final List<ComparativoCentroDTO> centros;
}
