package com.convecsa.erp.dashboard.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardAsistenciaDTO {

    private final int anio;
    private final int mes;
    private final long registros;
    private final long personasConRegistro;
    private final long inasistencias;
    private final long descansosMedicos;
    private final int minutosTardanza;
    private final int minutosExtras25;
    private final int minutosExtras35;
    private final BigDecimal horasTrabajadas;
}
