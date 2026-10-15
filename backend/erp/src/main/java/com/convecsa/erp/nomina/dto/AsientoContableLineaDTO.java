package com.convecsa.erp.nomina.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AsientoContableLineaDTO {
    private final String cuenta;
    private final String descripcion;
    private final String destino;
    private final BigDecimal debe;
    private final BigDecimal haber;
}
