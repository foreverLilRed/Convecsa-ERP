package com.convecsa.erp.construccion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrdenCompraItemRequest {

    @NotNull
    private Long materialId;

    private Long requerimientoItemId;

    @NotNull
    @DecimalMin(value = "0.0001", inclusive = true)
    @Digits(integer = 10, fraction = 4)
    private BigDecimal cantidad;

    @DecimalMin("0")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal precioUnitario;
}
