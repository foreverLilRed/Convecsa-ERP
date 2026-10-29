package com.convecsa.erp.construccion.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PeriodoCcRequest {

    @NotNull
    private LocalDate fechaDesde;

    @NotNull
    private LocalDate fechaHasta;

    private Integer nroSemana;

    @AssertTrue(message = "La fecha hasta no puede ser menor que la fecha desde")
    public boolean isRangoValido() {
        return fechaDesde == null || fechaHasta == null || !fechaHasta.isBefore(fechaDesde);
    }
}
