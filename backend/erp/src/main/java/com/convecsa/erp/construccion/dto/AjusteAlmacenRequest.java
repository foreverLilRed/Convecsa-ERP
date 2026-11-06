package com.convecsa.erp.construccion.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AjusteAlmacenRequest {

    @NotNull
    private Long materialId;

    @NotNull
    private BigDecimal cantidad;

    private BigDecimal costoUnitario;

    private LocalDate fecha;

    private String observaciones;
}
