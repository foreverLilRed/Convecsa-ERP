package com.convecsa.erp.contrato.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContratoRequest {

    @NotNull
    private Long personaId;

    @NotNull
    private Long tipoPlantillaId;

    @NotNull
    private Long causaObjetivaId;

    @NotBlank
    @Size(max = 160)
    private String cargo;

    @NotBlank
    @Size(max = 160)
    private String area;

    @NotBlank
    @Size(max = 200)
    private String proyecto;

    @NotBlank
    @Size(max = 200)
    private String ubicacionObra;

    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaFin;

    private LocalDate fechaCorte;

    @NotNull
    private Long jornadaId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal sueldo;

    @NotNull
    private Long metodoPagoId;

    @NotBlank
    @Size(max = 80)
    private String ciudadFirma;

    @Size(max = 40)
    private String numero;

    @Size(max = 2000)
    private String observaciones;

    @AssertTrue(message = "La fecha de fin no puede ser menor que la de inicio")
    public boolean isRangoFechasValido() {
        if (fechaInicio == null || fechaFin == null) {
            return true;
        }
        return !fechaFin.isBefore(fechaInicio);
    }
}
