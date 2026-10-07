package com.convecsa.erp.asistencia.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.convecsa.erp.contrato.catalogo.Jornada;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class HorasJornadaServiceTest {

    private final HorasJornadaService servicio = new HorasJornadaService();

    @Test
    void cuarentaYOchoHorasConCincoDias() {
        Jornada jornada = semanal("48.00");
        assertEquals(new BigDecimal("9.60"), servicio.horasEsperadasDia(jornada, 5));
    }

    @Test
    void cuarentaYOchoHorasConSeisDias() {
        Jornada jornada = semanal("48.00");
        assertEquals(new BigDecimal("8.00"), servicio.horasEsperadasDia(jornada, 6));
    }

    @Test
    void ochoHorasDiariasIgnoraDiasDeLaSemana() {
        Jornada jornada = new Jornada();
        jornada.setHorasDiarias(new BigDecimal("8.00"));
        jornada.setHorasSemanales(new BigDecimal("48.00"));
        assertEquals(new BigDecimal("8.00"), servicio.horasEsperadasDia(jornada, 5));
    }

    @Test
    void sinJornadaUsaOcho() {
        assertEquals(new BigDecimal("8.00"), servicio.horasEsperadasDia(null, 5));
    }

    private static Jornada semanal(String horas) {
        Jornada jornada = new Jornada();
        jornada.setHorasSemanales(new BigDecimal(horas));
        return jornada;
    }
}
