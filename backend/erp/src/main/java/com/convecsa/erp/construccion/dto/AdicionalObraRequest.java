package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.TipoAdicionalObra;
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
public class AdicionalObraRequest {

    @NotNull
    private TipoAdicionalObra tipo;

    private Long partidaId;

    @NotBlank
    @Size(max = 40)
    private String codigo;

    @NotBlank
    @Size(max = 240)
    private String descripcion;

    @Size(max = 20)
    private String unidad;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 10, fraction = 4)
    private BigDecimal metrado;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal precioUnitario;
}
