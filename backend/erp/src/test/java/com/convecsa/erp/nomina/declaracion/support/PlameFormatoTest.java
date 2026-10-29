package com.convecsa.erp.nomina.declaracion.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PlameFormatoTest {

    @Test
    void rucVacioUsaFallbackBoleta() {
        assertEquals("20601414067", PlameFormato.rucArchivo(null));
        assertEquals("20601414067", PlameFormato.rucArchivo(""));
        assertEquals("20601414067", PlameFormato.rucArchivo("RUC-"));
    }

    @Test
    void rucNumericoSeConserva() {
        assertEquals("20601414067", PlameFormato.rucArchivo("20601414067"));
    }
}
