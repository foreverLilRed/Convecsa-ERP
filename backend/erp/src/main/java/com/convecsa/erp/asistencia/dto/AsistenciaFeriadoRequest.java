package com.convecsa.erp.asistencia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsistenciaFeriadoRequest {

    @NotNull
    private LocalDate fecha;

    @NotBlank
    private String nombre;

    private Boolean laborable;
}
