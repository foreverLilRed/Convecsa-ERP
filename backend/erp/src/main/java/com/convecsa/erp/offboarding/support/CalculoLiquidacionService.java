package com.convecsa.erp.offboarding.support;

import com.convecsa.erp.offboarding.domain.LiquidacionSnapshot;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;

@Service
public class CalculoLiquidacionService {

    private static final BigDecimal VEINTICUATRO = new BigDecimal("24");
    private static final BigDecimal SETECIENTOS_VEINTE = new BigDecimal("720");
    private static final BigDecimal DOCE = new BigDecimal("12");

    public LiquidacionSnapshot calcular(LocalDate fechaInicio, LocalDate fechaCese, BigDecimal sueldo) {
        if (fechaInicio == null || fechaCese == null) {
            throw new IllegalArgumentException("Debe indicar fecha de inicio y fecha de cese");
        }
        if (fechaCese.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de cese no puede ser anterior al inicio del contrato");
        }
        if (sueldo == null || sueldo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El contrato no tiene un sueldo válido para liquidar");
        }

        BigDecimal sueldoMes = sueldo.setScale(2, RoundingMode.HALF_UP);
        Periodo ctsPeriodo = contar(inicioCts(fechaInicio, fechaCese), fechaCese);
        Periodo vacPeriodo = contar(inicioVacaciones(fechaInicio, fechaCese), fechaCese);
        Periodo gratiPeriodo = contar(inicioGratificacion(fechaInicio, fechaCese), fechaCese);

        BigDecimal cts = montoTrunco(sueldoMes, ctsPeriodo.meses, ctsPeriodo.dias);
        BigDecimal vacaciones = montoTrunco(sueldoMes, vacPeriodo.meses, vacPeriodo.dias);
        BigDecimal gratificacion = sueldoMes
                .divide(DOCE, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(gratiPeriodo.meses))
                .setScale(2, RoundingMode.HALF_UP);

        LiquidacionSnapshot snapshot = new LiquidacionSnapshot();
        snapshot.setSueldo(sueldoMes);
        snapshot.setCts(cts);
        snapshot.setVacaciones(vacaciones);
        snapshot.setGratificacion(gratificacion);
        snapshot.setTotal(cts.add(vacaciones).add(gratificacion).setScale(2, RoundingMode.HALF_UP));
        snapshot.setTiempoServicio(formatoTiempo(fechaInicio, fechaCese));
        snapshot.setFechaInicio(fechaInicio);
        snapshot.setFechaCese(fechaCese);
        return snapshot;
    }

    static LocalDate inicioCts(LocalDate fechaInicio, LocalDate fechaCese) {
        int mes = fechaCese.getMonthValue();
        LocalDate periodo = mes >= 5 && mes <= 10
                ? LocalDate.of(fechaCese.getYear(), 5, 1)
                : mes >= 11
                        ? LocalDate.of(fechaCese.getYear(), 11, 1)
                        : LocalDate.of(fechaCese.getYear() - 1, 11, 1);
        return fechaInicio.isAfter(periodo) ? fechaInicio : periodo;
    }

    static LocalDate inicioGratificacion(LocalDate fechaInicio, LocalDate fechaCese) {
        int mes = fechaCese.getMonthValue();
        LocalDate periodo = mes <= 6
                ? LocalDate.of(fechaCese.getYear(), 1, 1)
                : LocalDate.of(fechaCese.getYear(), 7, 1);
        return fechaInicio.isAfter(periodo) ? fechaInicio : periodo;
    }

    static LocalDate inicioVacaciones(LocalDate fechaInicio, LocalDate fechaCese) {
        long anios = ChronoUnit.YEARS.between(fechaInicio, fechaCese);
        LocalDate aniversario = fechaInicio.plusYears(anios);
        if (aniversario.isAfter(fechaCese)) {
            aniversario = fechaInicio.plusYears(Math.max(0, anios - 1));
        }
        return aniversario.isBefore(fechaInicio) ? fechaInicio : aniversario;
    }

    static Periodo contar(LocalDate inicio, LocalDate fin) {
        LocalDate finExclusivo = fin.plusDays(1);
        long meses = ChronoUnit.MONTHS.between(inicio, finExclusivo);
        LocalDate despuesMeses = inicio.plusMonths(meses);
        long dias = ChronoUnit.DAYS.between(despuesMeses, finExclusivo);
        return new Periodo((int) meses, (int) dias);
    }

    static BigDecimal montoTrunco(BigDecimal sueldo, int meses, int dias) {
        BigDecimal porMes = sueldo.divide(VEINTICUATRO, 6, RoundingMode.HALF_UP);
        BigDecimal porDia = sueldo.divide(SETECIENTOS_VEINTE, 6, RoundingMode.HALF_UP);
        return porMes.multiply(BigDecimal.valueOf(meses))
                .add(porDia.multiply(BigDecimal.valueOf(dias)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    static String formatoTiempo(LocalDate inicio, LocalDate fin) {
        Periodo periodo = contar(inicio, fin);
        int anios = periodo.meses / 12;
        int meses = periodo.meses % 12;
        int dias = periodo.dias;
        StringBuilder texto = new StringBuilder();
        if (anios > 0) {
            texto.append(anios).append(anios == 1 ? " año" : " años");
        }
        if (meses > 0) {
            if (texto.length() > 0) {
                texto.append(", ");
            }
            texto.append(meses).append(meses == 1 ? " mes" : " meses");
        }
        if (dias > 0 || texto.length() == 0) {
            if (texto.length() > 0) {
                texto.append(" y ");
            }
            texto.append(dias).append(dias == 1 ? " día" : " días");
        }
        return texto.toString();
    }

    record Periodo(int meses, int dias) {
    }
}
