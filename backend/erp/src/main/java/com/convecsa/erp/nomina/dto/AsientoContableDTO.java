package com.convecsa.erp.nomina.dto;

import com.convecsa.erp.nomina.domain.EstadoPeriodoNomina;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AsientoContableDTO {
    private final Long periodoId;
    private final int anio;
    private final int mes;
    private final EstadoPeriodoNomina estadoPeriodo;
    private final boolean preliminar;
    private final Instant generadoAt;
    private final int cantidadTrabajadores;
    private final BigDecimal totalHaberes;
    private final BigDecimal totalDescuentos;
    private final BigDecimal totalAportesEmpleador;
    private final BigDecimal totalNeto;
    private final List<AsientoContableBloqueDTO> asientos;
    private final BigDecimal totalDebe;
    private final BigDecimal totalHaber;
    private final BigDecimal diferencia;
}
