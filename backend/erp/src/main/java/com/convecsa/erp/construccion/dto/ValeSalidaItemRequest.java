package com.convecsa.erp.construccion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValeSalidaItemRequest {

    @NotNull
    private Long materialId;

    @NotNull
    @DecimalMin(value = "0.0001", inclusive = true)
    @Digits(integer = 10, fraction = 4)
    private BigDecimal cantidad;
}
