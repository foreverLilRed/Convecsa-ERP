package com.convecsa.erp.construccion.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.convecsa.erp.construccion.domain.CodigoTareoCc;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DominicalCcTest {

    @Test
    void unaFaltaRecortaSoloElDominicalDeEsaSemanaIso() {
        List<TareoCcDia> dias = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            dias.add(dia(LocalDate.of(2026, 1, 5).plusDays(i), "8.00", CodigoTareoCc.LAB));
        }
        dias.add(dia(LocalDate.of(2026, 1, 12), "8.00", CodigoTareoCc.F));
        for (int i = 1; i < 6; i++) {
            dias.add(dia(LocalDate.of(2026, 1, 12).plusDays(i), "8.00", CodigoTareoCc.LAB));
        }

        assertEquals(new BigDecimal("14.6667"), DominicalCc.horasDominical(dias));
    }

    @Test
    void redondeaPorSemanaIsoNoPorElTotalDelRango() {
        List<TareoCcDia> dias = List.of(
                dia(LocalDate.of(2026, 1, 5), "1.00", CodigoTareoCc.LAB),
                dia(LocalDate.of(2026, 1, 12), "1.00", CodigoTareoCc.LAB));

        assertEquals(new BigDecimal("0.3334"), DominicalCc.horasDominical(dias));
    }

    private static TareoCcDia dia(LocalDate fecha, String horas, CodigoTareoCc codigo) {
        TareoCcDia dia = new TareoCcDia();
        dia.setFecha(fecha);
        dia.setHorasOrd(new BigDecimal(horas));
        dia.setCodigo(codigo);
        return dia;
    }
}
