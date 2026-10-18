package com.convecsa.erp.dashboard.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardContratosDTO {

    private final long total;
    private final long activos;
    private final long inactivos;
    private final long vigentesHoy;
    private final long porVencer30Dias;
    private final long sinFechaFin;
    private final BigDecimal sueldosContratadosMes;
}
