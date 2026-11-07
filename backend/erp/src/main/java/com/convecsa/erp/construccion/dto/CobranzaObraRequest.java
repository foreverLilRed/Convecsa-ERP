package com.convecsa.erp.construccion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CobranzaObraRequest {

    @NotNull
    private Long facturaId;

    @NotNull
    private LocalDate fecha;

    @Size(max = 80)
    private String banco;

    @Size(max = 40)
    private String numeroOperacion;

    @DecimalMin("0")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal importe;

    @DecimalMin("0")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal detraccion;

    @Size(max = 400)
    private String observaciones;
}
