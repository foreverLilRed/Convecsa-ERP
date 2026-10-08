package com.convecsa.erp.desempeno.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PromedioCompetenciasTest {

    @Test
    void promedioDeLaMaqueta() {
        assertEquals(new BigDecimal("4.4"), PromedioCompetencias.calcular(5, 4, 4, 5, 4));
    }

    @Test
    void todosCinco() {
        assertEquals(new BigDecimal("5.0"), PromedioCompetencias.calcular(5, 5, 5, 5, 5));
    }
}

class PeriodoDesempenoTest {

    @Test
    void aceptaFormatoValido() {
        assertEquals("2026-Q3", PeriodoDesempeno.validar("2026-Q3"));
    }

    @Test
    void rechazaFormatoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> PeriodoDesempeno.validar("2026-T3"));
        assertThrows(IllegalArgumentException.class, () -> PeriodoDesempeno.validar(""));
    }
}
