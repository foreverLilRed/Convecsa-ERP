package com.convecsa.erp.soma.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.convecsa.erp.soma.support.SomaEstadisticaCalculator.IndicesSst;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SomaEstadisticaCalculatorTest {

    @Test
    void casoConocidoDaIfIsIaDeLaSpec() {
        IndicesSst indices = SomaEstadisticaCalculator.indices(1, 15, new BigDecimal("200000"));
        assertEquals(new BigDecimal("5.00"), indices.indiceFrecuencia());
        assertEquals(new BigDecimal("75.00"), indices.indiceSeveridad());
        assertEquals(new BigDecimal("0.38"), indices.indiceAccidentabilidad());
    }

    @Test
    void hhtCeroNoDivideYDevuelveCeros() {
        IndicesSst indices = SomaEstadisticaCalculator.indices(4, 20, BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00"), indices.indiceFrecuencia());
        assertEquals(new BigDecimal("0.00"), indices.indiceSeveridad());
        assertEquals(new BigDecimal("0.00"), indices.indiceAccidentabilidad());
    }

    @Test
    void acumuladoNoEsLaSumaDeIndicesMensuales() {
        IndicesSst mes1 = SomaEstadisticaCalculator.indices(1, 15, new BigDecimal("200000"));
        IndicesSst mes2 = SomaEstadisticaCalculator.indices(0, 0, new BigDecimal("200000"));
        IndicesSst acumulado = SomaEstadisticaCalculator.indices(1, 15, new BigDecimal("400000"));
        assertEquals(new BigDecimal("5.00"), mes1.indiceFrecuencia());
        assertEquals(new BigDecimal("0.00"), mes2.indiceFrecuencia());
        assertEquals(new BigDecimal("2.50"), acumulado.indiceFrecuencia());
        assertNotEquals(
                mes1.indiceFrecuencia().add(mes2.indiceFrecuencia()), acumulado.indiceFrecuencia());
    }
}
