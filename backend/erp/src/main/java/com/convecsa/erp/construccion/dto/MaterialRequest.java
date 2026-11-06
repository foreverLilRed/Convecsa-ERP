package com.convecsa.erp.construccion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialRequest {

    @NotBlank
    @Size(max = 40)
    private String codigo;

    @NotBlank
    @Size(max = 240)
    private String descripcion;

    @Size(max = 20)
    private String unidad;

    @Size(max = 40)
    private String familia;

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 4)
    private BigDecimal stockMinimo;

    private Boolean critico;

    private Boolean activo;
}
