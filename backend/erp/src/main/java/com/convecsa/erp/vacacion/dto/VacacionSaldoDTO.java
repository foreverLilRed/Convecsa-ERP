package com.convecsa.erp.vacacion.dto;

import java.time.LocalDate;

public record VacacionSaldoDTO(
        Long personaId,
        LocalDate periodoInicio,
        LocalDate periodoFin,
        int diasDerecho,
        int diasAprobados,
        int diasPendientes,
        int diasDisponibles) {
}
