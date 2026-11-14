package com.convecsa.erp.soma.support;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.nomina.support.BoletaPdfService;
import com.convecsa.erp.soma.dto.SomaSunafilDocumentoDTO;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class SomaSunafilPdfServiceTest {

    private final SomaSunafilPdfService servicio = new SomaSunafilPdfService(new BoletaPdfService());

    @Test
    void formato1NoVacioYEmpiezaConPdf() {
        byte[] pdf = servicio.generar(documento(1, List.of(List.of("1", "INCIDENTE", "0000"))));
        assertTrue(pdf.length > 100);
        assertTrue(new String(pdf, 0, 4, StandardCharsets.ISO_8859_1).startsWith("%PDF"));
    }

    @Test
    void formato2SinFilasSigueSiendoPdf() {
        byte[] pdf = servicio.generar(documento(2, List.of()));
        assertTrue(pdf.length > 100);
        assertTrue(new String(pdf, 0, 4, StandardCharsets.ISO_8859_1).startsWith("%PDF"));
    }

    @Test
    void formato3NoVacioYEmpiezaConPdf() {
        byte[] pdf = servicio.generar(documento(3, List.of(List.of("13/11/2026", "FISICO_RUIDO", "0000"))));
        assertTrue(pdf.length > 100);
        assertTrue(new String(pdf, 0, 4, StandardCharsets.ISO_8859_1).startsWith("%PDF"));
    }

    @Test
    void formato4SinFilasSigueSiendoPdf() {
        byte[] pdf = servicio.generar(documento(4, List.of()));
        assertTrue(pdf.length > 100);
        assertTrue(new String(pdf, 0, 4, StandardCharsets.ISO_8859_1).startsWith("%PDF"));
    }

    @Test
    void formato5NoVacioYEmpiezaConPdf() {
        byte[] pdf = servicio.generar(documento(5, List.of(List.of("11", "100", "0"))));
        assertTrue(pdf.length > 100);
        assertTrue(new String(pdf, 0, 4, StandardCharsets.ISO_8859_1).startsWith("%PDF"));
    }

    @Test
    void formato6SinFilasSigueSiendoPdf() {
        byte[] pdf = servicio.generar(documento(6, List.of()));
        assertTrue(pdf.length > 100);
        assertTrue(new String(pdf, 0, 4, StandardCharsets.ISO_8859_1).startsWith("%PDF"));
    }

    private static SomaSunafilDocumentoDTO documento(int formato, List<List<String>> filas) {
        return SomaSunafilDocumentoDTO.builder()
                .formato(formato)
                .titulo("Registro SUNAFIL " + formato)
                .razonSocial("Convecsa SAC")
                .ruc("20100000000")
                .domicilio("Lima")
                .alcance("0000 — Obra piloto")
                .periodo("Histórico")
                .columnas(List.of("Col1", "Col2", "Obra"))
                .filas(filas)
                .build();
    }
}
