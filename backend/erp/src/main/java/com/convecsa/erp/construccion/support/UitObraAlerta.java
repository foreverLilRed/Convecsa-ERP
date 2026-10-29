package com.convecsa.erp.construccion.support;

import java.math.BigDecimal;

public final class UitObraAlerta {

    public static final int MULTIPLO = 50;

    private UitObraAlerta() {}

    public static BigDecimal umbral(BigDecimal uit) {
        if (uit == null) {
            return null;
        }
        return uit.multiply(BigDecimal.valueOf(MULTIPLO));
    }

    public static boolean supera(BigDecimal valorObra, BigDecimal uit) {
        if (valorObra == null || uit == null) {
            return false;
        }
        BigDecimal limite = umbral(uit);
        return limite != null && valorObra.compareTo(limite) > 0;
    }
}
