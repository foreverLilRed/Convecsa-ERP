package com.convecsa.erp.nomina.dto;

import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoletaConceptoExtraRequest {

    @NotNull
    private CategoriaConcepto categoria;

    @NotBlank
    private String label;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal monto;
}
