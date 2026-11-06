package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrdenCompraItemDTO {

    private final Long id;
    private final Long materialId;
    private final String codigoMaterial;
    private final String descripcionMaterial;
    private final String unidad;
    private final Long requerimientoItemId;
    private final BigDecimal cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal cantidadRecibida;
    private final BigDecimal cantidadPendiente;
}
