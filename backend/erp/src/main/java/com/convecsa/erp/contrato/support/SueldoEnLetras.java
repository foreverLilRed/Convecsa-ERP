package com.convecsa.erp.contrato.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SueldoEnLetras {

    private static final String[] UNIDADES = {
            "", "un", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"
    };
    private static final String[] DIEZ_A_DIECINUEVE = {
            "diez", "once", "doce", "trece", "catorce", "quince",
            "dieciséis", "diecisiete", "dieciocho", "diecinueve"
    };
    private static final String[] DECENAS = {
            "", "", "veinte", "treinta", "cuarenta", "cincuenta",
            "sesenta", "setenta", "ochenta", "noventa"
    };
    private static final String[] CENTENAS = {
            "", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos",
            "seiscientos", "setecientos", "ochocientos", "novecientos"
    };

    private SueldoEnLetras() {
    }

    public static String de(BigDecimal monto) {
        if (monto == null) {
            return "";
        }
        BigDecimal normalizado = monto.abs().setScale(2, RoundingMode.HALF_UP);
        long enteros = normalizado.longValue();
        int centavos = normalizado.remainder(BigDecimal.ONE).movePointRight(2).intValue();
        String letras = enteros == 1 ? "un sol" : grupo(enteros) + " soles";
        String decimales = String.format("%02d", centavos) + "/100";
        String resultado = letras + " con " + decimales;
        return Character.toUpperCase(resultado.charAt(0)) + resultado.substring(1);
    }

    private static String grupo(long valor) {
        if (valor == 0) {
            return "cero";
        }
        if (valor == 100) {
            return "cien";
        }
        if (valor < 10) {
            return UNIDADES[(int) valor];
        }
        if (valor < 20) {
            return DIEZ_A_DIECINUEVE[(int) (valor - 10)];
        }
        if (valor < 30) {
            return valor == 20 ? "veinte" : "veinti" + UNIDADES[(int) (valor - 20)];
        }
        if (valor < 100) {
            long unidad = valor % 10;
            return DECENAS[(int) (valor / 10)] + (unidad == 0 ? "" : " y " + UNIDADES[(int) unidad]);
        }
        if (valor < 1000) {
            long resto = valor % 100;
            return CENTENAS[(int) (valor / 100)] + (resto == 0 ? "" : " " + grupo(resto));
        }
        if (valor < 1_000_000) {
            long miles = valor / 1000;
            long resto = valor % 1000;
            String prefijo = miles == 1 ? "mil" : grupo(miles) + " mil";
            return prefijo + (resto == 0 ? "" : " " + grupo(resto));
        }
        long millones = valor / 1_000_000;
        long resto = valor % 1_000_000;
        String prefijo = millones == 1 ? "un millón" : grupo(millones) + " millones";
        return prefijo + (resto == 0 ? "" : " " + grupo(resto));
    }
}
