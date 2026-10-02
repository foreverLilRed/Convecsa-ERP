package com.convecsa.erp.contrato.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContratoRequest {

    @NotNull
    private Long personaId;

    @NotNull
    private Long plantillaContratoId;

    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaFin;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal sueldo;

    private Map<String, String> metadata = new LinkedHashMap<>();

    @AssertTrue(message = "La fecha de fin no puede ser menor que la de inicio")
    public boolean isRangoFechasValido() {
        if (fechaInicio == null || fechaFin == null) {
            return true;
        }
        return !fechaFin.isBefore(fechaInicio);
    }
}
