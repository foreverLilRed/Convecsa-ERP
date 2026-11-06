package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ValeSalidaItemDTO {

    private final Long id;
    private final Long materialId;
    private final String codigoMaterial;
    private final String descripcionMaterial;
    private final String unidad;
    private final BigDecimal cantidad;
    private final BigDecimal costoUnitario;
    private final BigDecimal costoTotal;
}
