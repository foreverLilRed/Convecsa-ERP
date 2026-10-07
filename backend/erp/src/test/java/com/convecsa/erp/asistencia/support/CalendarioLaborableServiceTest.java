package com.convecsa.erp.asistencia.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CalendarioLaborableServiceTest {

    private final CalendarioLaborableService calendario = new CalendarioLaborableService();

    @Test
    void feriadoNoLaborableSeExcluye() {
        LocalDate feriado = LocalDate.of(2026, 10, 8);
        List<LocalDate> dias = calendario.diasLaborables(
                2026, 10, List.of(1, 2, 3, 4, 5), Set.of(feriado));

        assertFalse(dias.contains(feriado));
        assertTrue(dias.contains(LocalDate.of(2026, 10, 7)));
        assertTrue(dias.contains(LocalDate.of(2026, 10, 9)));
        assertFalse(dias.contains(LocalDate.of(2026, 10, 10)));
        assertFalse(dias.contains(LocalDate.of(2026, 10, 11)));
    }

    @Test
    void faltantesNoIncluyeFechasYaRegistradas() {
        List<LocalDate> laborables = List.of(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 2),
                LocalDate.of(2026, 10, 5));
        Set<LocalDate> existentes = Set.of(LocalDate.of(2026, 10, 2));

        List<LocalDate> faltan = CalendarioLaborableService.faltantes(laborables, existentes);

        assertEquals(List.of(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5)), faltan);
    }
}
