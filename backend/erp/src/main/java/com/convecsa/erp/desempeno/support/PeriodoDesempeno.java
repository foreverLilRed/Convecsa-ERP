package com.convecsa.erp.desempeno.support;

import java.time.LocalDate;
import java.util.regex.Pattern;

public final class PeriodoDesempeno {

    private static final Pattern FORMATO = Pattern.compile("^\\d{4}-Q[1-4]$");

    private PeriodoDesempeno() {}

    public static String validar(String periodo) {
        if (periodo == null || periodo.isBlank() || !FORMATO.matcher(periodo.trim()).matches()) {
            throw new IllegalArgumentException("Periodo inválido. Use el formato AAAA-Qn.");
        }
        return periodo.trim();
    }

    public static String actual() {
        LocalDate hoy = LocalDate.now();
        int trimestre = ((hoy.getMonthValue() - 1) / 3) + 1;
        return hoy.getYear() + "-Q" + trimestre;
    }
}
