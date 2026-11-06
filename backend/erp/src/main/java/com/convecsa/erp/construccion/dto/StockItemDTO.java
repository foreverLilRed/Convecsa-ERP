package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockItemDTO {

    private final Long materialId;
    private final String codigo;
    private final String descripcion;
    private final String unidad;
    private final String familia;
    private final BigDecimal stockMinimo;
    private final boolean critico;
    private final boolean activo;
    private final BigDecimal cantidad;
    private final BigDecimal costoPromedio;
    private final BigDecimal valor;
    private final boolean quiebre;
    private final boolean criticoSinStock;
}
