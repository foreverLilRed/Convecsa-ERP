package com.convecsa.erp.nomina.declaracion.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.nomina.declaracion.entity.ReciboHonorario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class RxhParserTest {

    private final RxhParser parser = new RxhParser();

    @Test
    void parseaLayoutSunatDe16ColumnasYExtraeDniDeRuc10() {
        String txt = "06|10102103040|LOPEZ|DIAZ|MARIA|S||R|E001|15|1500.00|2026-04-10|10/04/2026|S|N|0.00\n";
        List<ReciboHonorario> filas = parser.parsear(txt);
        assertEquals(1, filas.size());
        ReciboHonorario recibo = filas.get(0);
        assertEquals("06", recibo.getTipoDocumento());
        assertEquals("10102103040", recibo.getNumeroDocumento());
        assertEquals("E001", recibo.getSerie());
        assertEquals("15", recibo.getNumero());
        assertEquals(new BigDecimal("1500.00"), recibo.getMonto());
        assertEquals(LocalDate.of(2026, 4, 10), recibo.getFechaEmision());
        assertEquals(LocalDate.of(2026, 4, 10), recibo.getFechaPago());
        assertTrue(recibo.isRetencionCuarta());
        assertEquals("10210304", RxhParser.dniDesdeDocumento(recibo.getTipoDocumento(), recibo.getNumeroDocumento()));
    }
}
