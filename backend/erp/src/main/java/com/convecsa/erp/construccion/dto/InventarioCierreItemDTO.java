package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventarioCierreItemDTO {

    private final Long materialId;
    private final String codigo;
    private final String descripcion;
    private final String unidad;
    private final BigDecimal cantidad;
    private final BigDecimal costoPromedio;
    private final BigDecimal valor;
}
