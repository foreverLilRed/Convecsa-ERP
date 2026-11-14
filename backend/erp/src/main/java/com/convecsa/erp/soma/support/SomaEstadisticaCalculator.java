package com.convecsa.erp.soma.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SomaEstadisticaCalculator {

    public static final int ESCALA = 2;
    public static final RoundingMode MODO = RoundingMode.HALF_UP;
    public static final BigDecimal MILLON = new BigDecimal("1000000");
    public static final BigDecimal MIL = new BigDecimal("1000");

    private SomaEstadisticaCalculator() {}

    public static IndicesSst indices(int accidentes, int diasPerdidos, BigDecimal hht) {
        BigDecimal horas = hht == null ? BigDecimal.ZERO : hht;
        if (horas.compareTo(BigDecimal.ZERO) == 0) {
            return new IndicesSst(cero(), cero(), cero());
        }
        BigDecimal frecuencia = MILLON.multiply(BigDecimal.valueOf(accidentes)).divide(horas, ESCALA, MODO);
        BigDecimal severidad = MILLON.multiply(BigDecimal.valueOf(diasPerdidos)).divide(horas, ESCALA, MODO);
        BigDecimal accidentabilidad = frecuencia.multiply(severidad).divide(MIL, ESCALA, MODO);
        return new IndicesSst(frecuencia, severidad, accidentabilidad);
    }

    public static BigDecimal horasDia(BigDecimal horasOrd, BigDecimal he60, BigDecimal he100) {
        return nuloACero(horasOrd).add(nuloACero(he60)).add(nuloACero(he100));
    }

    public static BigDecimal cero() {
        return BigDecimal.ZERO.setScale(ESCALA, MODO);
    }

    private static BigDecimal nuloACero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    public record IndicesSst(
            BigDecimal indiceFrecuencia, BigDecimal indiceSeveridad, BigDecimal indiceAccidentabilidad) {}
}
