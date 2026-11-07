package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.TipoValorizacion;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValorizacionCrearRequest {

    @NotNull
    private TipoValorizacion tipo;

    @NotNull
    private Integer anio;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer mes;
}
