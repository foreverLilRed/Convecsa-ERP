package com.convecsa.erp.construccion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartidaPresupuestoRequest {

    @NotBlank
    @Size(max = 40)
    private String codigo;

    @Size(max = 80)
    private String capitulo;

    @NotBlank
    @Size(max = 240)
    private String descripcion;

    @Size(max = 20)
    private String unidad;

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 4)
    private BigDecimal metrado;

    @DecimalMin("0")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal precioUnitario;

    @Size(max = 40)
    private String centroCosto;

    @Valid
    private List<ApuPartidaRequest> apu = new ArrayList<>();
}
