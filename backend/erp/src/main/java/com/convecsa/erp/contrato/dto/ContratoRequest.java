package com.convecsa.erp.contrato.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContratoRequest {

    @NotBlank
    @Size(max = 200)
    private String titulo;

    @NotNull
    private Long categoriaContratoId;

    @NotNull
    private Long personaId;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal sueldo;

    private Boolean activo;

    private LocalDate fechaCorte;

    @NotNull
    private Long tipoPlantillaId;

    @Size(max = 10000)
    private String observaciones;

    private Map<String, String> metadata = new LinkedHashMap<>();

    @AssertTrue(message = "La fecha de fin no puede ser menor que la de inicio")
    public boolean isRangoFechasValido() {
        return fechaInicio == null || fechaFin == null || !fechaFin.isBefore(fechaInicio);
    }
}
