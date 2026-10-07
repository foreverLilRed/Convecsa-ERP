package com.convecsa.erp.nomina.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AfpDTO {

    private final Long id;
    private final String codigo;
    private final String label;
    private final BigDecimal aporteObligatorio;
    private final BigDecimal comisionFlujo;
    private final BigDecimal primaSeguro;
    private final boolean esOnp;
}
