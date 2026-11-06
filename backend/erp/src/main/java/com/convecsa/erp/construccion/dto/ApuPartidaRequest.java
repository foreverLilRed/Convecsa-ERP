package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.TipoRecursoApu;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApuPartidaRequest {

    @NotNull
    private TipoRecursoApu tipo;

    @Size(max = 40)
    private String codigo;

    @NotBlank
    @Size(max = 240)
    private String descripcion;

    @Size(max = 20)
    private String unidad;

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 4)
    private BigDecimal cantidad;

    @DecimalMin("0")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal precioUnitario;
}
