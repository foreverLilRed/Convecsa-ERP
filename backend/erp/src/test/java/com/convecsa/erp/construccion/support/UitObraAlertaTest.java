package com.convecsa.erp.construccion.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class UitObraAlertaTest {

    @Test
    void umbralCincuentaUit2026() {
        assertEquals(new BigDecimal("275000"), UitObraAlerta.umbral(new BigDecimal("5500")));
    }

    @Test
    void sinValorNoAlerta() {
        assertFalse(UitObraAlerta.supera(null, new BigDecimal("5500")));
    }

    @Test
    void superaCincuentaUit() {
        assertTrue(UitObraAlerta.supera(new BigDecimal("275000.01"), new BigDecimal("5500")));
        assertFalse(UitObraAlerta.supera(new BigDecimal("275000"), new BigDecimal("5500")));
    }
}
