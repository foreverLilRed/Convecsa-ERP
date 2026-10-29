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
public class ProyectoRequest {

    @NotBlank
    @Size(max = 20)
    private String codigo;

    @NotBlank
    @Size(max = 160)
    private String nombre;

    @Size(max = 120)
    private String ubicacion;

    @Size(max = 40)
    private String centroCosto;

    @DecimalMin(value = "0.00")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal valorObra;

    private Boolean activo;
}
