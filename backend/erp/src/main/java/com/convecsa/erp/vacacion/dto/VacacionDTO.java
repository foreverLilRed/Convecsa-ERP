package com.convecsa.erp.vacacion.dto;

import com.convecsa.erp.vacacion.domain.EstadoVacacion;
import com.convecsa.erp.vacacion.domain.TipoVacacion;
import java.time.Instant;
import java.time.LocalDate;

public record VacacionDTO(
        Long id,
        Long personaId,
        String personaNombreCompleto,
        String personaNumeroDocumento,
        String cargo,
        Long contratoId,
        LocalDate fechaIngreso,
        TipoVacacion tipo,
        EstadoVacacion estado,
        boolean gozada,
        LocalDate periodoInicio,
        LocalDate periodoFin,
        int diasDerechoPeriodo,
        int diasSolicitados,
        int diasUsadosPeriodo,
        int diasDisponiblesPeriodo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        LocalDate fechaRetorno,
        String observacion,
        Instant aprobadaEn,
        Instant createdAt) {
}
