package com.convecsa.erp.obra.dto;

import com.convecsa.erp.obra.domain.EstadoObra;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ObraDTO(
        Long id,
        String codigo,
        String nombre,
        String clienteNombre,
        String contratistaPrincipal,
        String ubicacion,
        LocalDate fechaInicioPlanificada,
        LocalDate fechaFinPlanificada,
        LocalDate fechaInicioReal,
        LocalDate fechaFinReal,
        BigDecimal montoContratado,
        String moneda,
        String responsableNombre,
        EstadoObra estado,
        Instant createdAt,
        Instant updatedAt) {
}
