package com.convecsa.erp.construccion.support;

import com.convecsa.erp.construccion.domain.CodigoTareoCc;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class DominicalCc {

    private static final BigDecimal SEIS = new BigDecimal("6");

    private DominicalCc() {}

    public static BigDecimal horasDominical(List<TareoCcDia> dias) {
        Map<Integer, BigDecimal> porSemana = new TreeMap<>();
        for (TareoCcDia dia : dias) {
            if (dia.getFecha() == null || dia.getFecha().getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }
            if (dia.getCodigo() != null && dia.getCodigo() != CodigoTareoCc.LAB) {
                continue;
            }
            BigDecimal horas = dia.getHorasOrd() == null ? BigDecimal.ZERO : dia.getHorasOrd();
            if (horas.signum() <= 0) {
                continue;
            }
            int semana = dia.getFecha().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            porSemana.merge(semana, horas, BigDecimal::add);
        }
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal horas : porSemana.values()) {
            total = total.add(horas.divide(SEIS, 4, RoundingMode.HALF_UP));
        }
        return total;
    }
}
