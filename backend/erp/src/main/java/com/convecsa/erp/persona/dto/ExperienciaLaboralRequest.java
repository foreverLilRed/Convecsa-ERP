package com.convecsa.erp.persona.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExperienciaLaboralRequest {

    @NotBlank
    @Size(max = 160)
    private String organizacion;

    @NotBlank
    @Size(max = 160)
    private String cargo;

    @NotNull
    private LocalDate fechaInicioLaboral;

    private LocalDate fechaFin;

    @AssertTrue(message = "La fecha de fin no puede ser menor que la de inicio")
    public boolean isRangoFechasValido() {
        if (fechaInicioLaboral == null || fechaFin == null) {
            return true;
        }
        return !fechaFin.isBefore(fechaInicioLaboral);
    }
}
