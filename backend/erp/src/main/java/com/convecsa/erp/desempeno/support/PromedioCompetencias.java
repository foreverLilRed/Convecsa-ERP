package com.convecsa.erp.desempeno.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PromedioCompetencias {

    private PromedioCompetencias() {}

    public static BigDecimal calcular(
            int seguridadSst, int productividad, int calidadTecnica, int puntualidad, int trabajoEquipo) {
        int suma = seguridadSst + productividad + calidadTecnica + puntualidad + trabajoEquipo;
        return BigDecimal.valueOf(suma).divide(BigDecimal.valueOf(5), 1, RoundingMode.HALF_UP);
    }
}
