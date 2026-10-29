package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.EstadoPeriodoCc;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PeriodoCcDTO {

    private final Long id;
    private final Long proyectoId;
    private final String proyectoCodigo;
    private final String proyectoNombre;
    private final LocalDate fechaDesde;
    private final LocalDate fechaHasta;
    private final int nroSemana;
    private final EstadoPeriodoCc estado;
    private final Instant createdAt;
    private final Instant closedAt;
    private final int cantidadBoletas;
}
