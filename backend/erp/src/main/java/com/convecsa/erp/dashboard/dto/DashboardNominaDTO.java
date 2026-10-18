package com.convecsa.erp.dashboard.dto;

import com.convecsa.erp.nomina.domain.EstadoPeriodoNomina;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardNominaDTO {

    private final int anio;
    private final int mes;
    private final Long periodoId;
    private final EstadoPeriodoNomina estado;
    private final boolean periodoGenerado;
    private final boolean montoEstimado;
    private final long cantidadBoletas;
    private final BigDecimal totalHaberes;
    private final BigDecimal totalDescuentos;
    private final BigDecimal aportesEmpleador;
    private final BigDecimal montoPagar;
}
