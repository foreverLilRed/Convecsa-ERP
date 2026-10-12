package com.convecsa.erp.offboarding.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.convecsa.erp.offboarding.domain.LiquidacionSnapshot;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CalculoLiquidacionServiceTest {

    private final CalculoLiquidacionService calculo = new CalculoLiquidacionService();

    @Test
    void liquidacionMypeEneroAOctubre() {
        LiquidacionSnapshot snapshot = calculo.calcular(
                LocalDate.of(2026, 1, 15),
                LocalDate.of(2026, 10, 11),
                new BigDecimal("1500.00"));

        assertEquals(new BigDecimal("335.42"), snapshot.getCts());
        assertEquals(new BigDecimal("375.00"), snapshot.getGratificacion());
        assertEquals(new BigDecimal("556.25"), snapshot.getVacaciones());
        assertEquals(new BigDecimal("1266.67"), snapshot.getTotal());
        assertEquals("8 meses y 27 días", snapshot.getTiempoServicio());
    }

    @Test
    void ctsSemestreNoviembreAbril() {
        LiquidacionSnapshot snapshot = calculo.calcular(
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2026, 3, 15),
                new BigDecimal("2400.00"));

        assertEquals(new BigDecimal("450.00"), snapshot.getCts());
    }

    @Test
    void fechaCeseAnteriorFalla() {
        assertThrows(IllegalArgumentException.class, () -> calculo.calcular(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 1, 1),
                new BigDecimal("1500.00")));
    }
}
