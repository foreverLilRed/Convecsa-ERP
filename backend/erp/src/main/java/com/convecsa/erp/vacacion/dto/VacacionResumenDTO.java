package com.convecsa.erp.vacacion.dto;

public record VacacionResumenDTO(
        long totalSolicitudes,
        long pendientes,
        long aprobadas,
        long gozadas,
        long conveniosAdelanto,
        long diasAprobados) {
}
