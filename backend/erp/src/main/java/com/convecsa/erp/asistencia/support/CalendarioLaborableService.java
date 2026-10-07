package com.convecsa.erp.asistencia.support;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CalendarioLaborableService {

    public List<Integer> parseDiasSemana(String crudo) {
        LinkedHashSet<Integer> dias = new LinkedHashSet<>();
        if (crudo == null || crudo.isBlank()) {
            return List.of(1, 2, 3, 4, 5);
        }
        for (String parte : crudo.split(",")) {
            try {
                int dia = Integer.parseInt(parte.trim());
                if (dia >= 1 && dia <= 7) {
                    dias.add(dia);
                }
            } catch (NumberFormatException ignored) {
                // se ignora el token inválido
            }
        }
        if (dias.isEmpty()) {
            return List.of(1, 2, 3, 4, 5);
        }
        return new ArrayList<>(dias);
    }

    public String serializarDiasSemana(List<Integer> dias) {
        List<Integer> validos = normalizarDias(dias);
        StringBuilder builder = new StringBuilder();
        for (Integer dia : validos) {
            if (!builder.isEmpty()) {
                builder.append(',');
            }
            builder.append(dia);
        }
        return builder.toString();
    }

    public List<Integer> normalizarDias(List<Integer> dias) {
        LinkedHashSet<Integer> validos = new LinkedHashSet<>();
        if (dias != null) {
            for (Integer dia : dias) {
                if (dia != null && dia >= 1 && dia <= 7) {
                    validos.add(dia);
                }
            }
        }
        if (validos.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un día laborable");
        }
        return new ArrayList<>(validos);
    }

    public List<LocalDate> diasLaborables(
            int anio,
            int mes,
            List<Integer> diasSemana,
            Set<LocalDate> feriadosNoLaborables) {
        YearMonth yearMonth = YearMonth.of(anio, mes);
        Set<Integer> semana = new LinkedHashSet<>(normalizarDias(diasSemana));
        Set<LocalDate> feriados = feriadosNoLaborables == null ? Set.of() : feriadosNoLaborables;
        List<LocalDate> resultado = new ArrayList<>();
        for (int dia = 1; dia <= yearMonth.lengthOfMonth(); dia++) {
            LocalDate fecha = yearMonth.atDay(dia);
            if (!semana.contains(fecha.getDayOfWeek().getValue())) {
                continue;
            }
            if (feriados.contains(fecha)) {
                continue;
            }
            resultado.add(fecha);
        }
        return resultado;
    }

    public static List<LocalDate> faltantes(List<LocalDate> laborables, Set<LocalDate> existentes) {
        List<LocalDate> faltan = new ArrayList<>();
        Set<LocalDate> ya = existentes == null ? Set.of() : existentes;
        for (LocalDate fecha : laborables) {
            if (!ya.contains(fecha)) {
                faltan.add(fecha);
            }
        }
        return faltan;
    }
}
