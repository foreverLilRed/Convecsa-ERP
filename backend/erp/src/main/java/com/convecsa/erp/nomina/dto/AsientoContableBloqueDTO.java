package com.convecsa.erp.nomina.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AsientoContableBloqueDTO {
    private final String codigo;
    private final String titulo;
    private final List<AsientoContableLineaDTO> lineas;
    private final BigDecimal totalDebe;
    private final BigDecimal totalHaber;
}
