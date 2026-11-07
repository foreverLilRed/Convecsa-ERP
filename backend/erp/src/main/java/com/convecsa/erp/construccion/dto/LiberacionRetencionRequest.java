package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.TipoLiberacionRetencion;
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
public class LiberacionRetencionRequest {

    @NotNull
    private TipoLiberacionRetencion tipo;

    @NotNull
    private LocalDate fecha;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal importe;

    @Size(max = 160)
    private String entidad;

    @Size(max = 40)
    private String numeroDocumento;

    @Size(max = 400)
    private String observaciones;
}
