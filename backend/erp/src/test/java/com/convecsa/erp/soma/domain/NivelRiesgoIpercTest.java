package com.convecsa.erp.soma.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NivelRiesgoIpercTest {

    @Test
    void bordesDeCadaRango() {
        assertEquals(NivelRiesgoIperc.TRIVIAL, NivelRiesgoIperc.desdePuntaje(4));
        assertEquals(NivelRiesgoIperc.TOLERABLE, NivelRiesgoIperc.desdePuntaje(5));
        assertEquals(NivelRiesgoIperc.TOLERABLE, NivelRiesgoIperc.desdePuntaje(8));
        assertEquals(NivelRiesgoIperc.MODERADO, NivelRiesgoIperc.desdePuntaje(9));
        assertEquals(NivelRiesgoIperc.MODERADO, NivelRiesgoIperc.desdePuntaje(12));
        assertEquals(NivelRiesgoIperc.IMPORTANTE, NivelRiesgoIperc.desdePuntaje(13));
        assertEquals(NivelRiesgoIperc.IMPORTANTE, NivelRiesgoIperc.desdePuntaje(19));
        assertEquals(NivelRiesgoIperc.INTOLERABLE, NivelRiesgoIperc.desdePuntaje(20));
    }

    @Test
    void puntajeFueraDeRangoFalla() {
        assertThrows(IllegalArgumentException.class, () -> NivelRiesgoIperc.desdePuntaje(0));
        assertThrows(IllegalArgumentException.class, () -> NivelRiesgoIperc.desdePuntaje(26));
    }
}
