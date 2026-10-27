package com.convecsa.erp.obra.dto;

public record ObraResumenDTO(
        long total,
        long borrador,
        long planificada,
        long enEjecucion,
        long suspendida,
        long cerrada,
        long cancelada) {
}
