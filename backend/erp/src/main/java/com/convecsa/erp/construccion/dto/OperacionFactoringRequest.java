package com.convecsa.erp.construccion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperacionFactoringRequest {

    @NotNull
    private Long facturaId;

    @NotBlank
    @Size(max = 160)
    private String entidadRazon;

    @Size(max = 11)
    private String entidadRuc;

    @NotNull
    private LocalDate fechaCesion;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal tasaDescuento;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal comision;

    private LocalDate fechaPagoEsperada;
}
