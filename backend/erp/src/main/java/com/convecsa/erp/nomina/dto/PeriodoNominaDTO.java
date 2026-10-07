package com.convecsa.erp.nomina.dto;

import com.convecsa.erp.nomina.domain.EstadoPeriodoNomina;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PeriodoNominaDTO {

    private final Long id;
    private final int anio;
    private final int mes;
    private final EstadoPeriodoNomina estado;
    private final String observaciones;
    private final Instant createdAt;
    private final Instant closedAt;
    private final long cantidadBoletas;
    private final BigDecimal totalNeto;
}
