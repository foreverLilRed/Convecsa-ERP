package com.convecsa.erp.nomina.declaracion.support;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class PlameFormato {

    private static final Locale US = Locale.US;

    private PlameFormato() {}

    public static String monto(BigDecimal valor) {
        DecimalFormat format = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(US));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(n(valor));
    }

    public static String cantidad(BigDecimal valor) {
        DecimalFormat format = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(US));
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(n(valor));
    }

    public static HorasMinutos horasMinutos(BigDecimal horas) {
        BigDecimal valor = n(horas);
        int h = valor.setScale(0, RoundingMode.DOWN).intValue();
        int m = valor.subtract(BigDecimal.valueOf(h))
                .multiply(BigDecimal.valueOf(60))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
        if (m == 60) {
            h++;
            m = 0;
        }
        return new HorasMinutos(h, m);
    }

    public static String limpio(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("|", " ").replace(",", " ").trim();
    }

    public static String rucArchivo(String ruc) {
        if (ruc == null) {
            return "00000000000";
        }
        String digits = ruc.replaceAll("\\D", "");
        return digits.isBlank() ? "00000000000" : digits;
    }

    public static String periodo(int anio, int mes) {
        return String.format("%04d%02d", anio, mes);
    }

    public static BigDecimal n(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    public record HorasMinutos(int horas, int minutos) {}
}
