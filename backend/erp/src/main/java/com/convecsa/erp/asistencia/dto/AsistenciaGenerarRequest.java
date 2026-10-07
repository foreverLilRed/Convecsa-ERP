package com.convecsa.erp.asistencia.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsistenciaGenerarRequest {

    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer anio;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer mes;
}
