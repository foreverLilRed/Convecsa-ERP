package com.convecsa.erp.contrato.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SueldoEnLetrasTest {

    @Test
    void convierteSueldoEntero() {
        assertEquals("Seis mil quinientos soles con 00/100", SueldoEnLetras.de(new BigDecimal("6500")));
    }

    @Test
    void convierteUnSol() {
        assertEquals("Un sol con 50/100", SueldoEnLetras.de(new BigDecimal("1.50")));
    }
}
