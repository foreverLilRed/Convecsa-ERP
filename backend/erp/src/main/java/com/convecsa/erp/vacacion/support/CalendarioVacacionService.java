package com.convecsa.erp.vacacion.support;

import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class CalendarioVacacionService {

    public LocalDate fechaFin(LocalDate fechaInicio, int diasCalendario) {
        validar(fechaInicio, diasCalendario);
        return fechaInicio.plusDays(diasCalendario - 1L);
    }

    public LocalDate fechaRetorno(LocalDate fechaInicio, int diasCalendario) {
        return fechaFin(fechaInicio, diasCalendario).plusDays(1);
    }

    private static void validar(LocalDate fechaInicio, int diasCalendario) {
        if (fechaInicio == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }
        if (diasCalendario < 1 || diasCalendario > 30) {
            throw new IllegalArgumentException("Los días solicitados deben estar entre 1 y 30");
        }
    }
}
