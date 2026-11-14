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

    private static SomaSunafilDocumentoDTO documento(int formato, List<List<String>> filas) {
        return SomaSunafilDocumentoDTO.builder()
                .formato(formato)
                .titulo(formato == 1 ? "Registro de eventos SST" : "Registro de EMO")
                .razonSocial("Convecsa SAC")
                .ruc("20100000000")
                .domicilio("Lima")
                .alcance("0000 — Obra piloto")
                .periodo("Histórico")
                .columnas(formato == 1
                        ? List.of("N°", "Tipo", "Obra")
                        : List.of("Trabajador", "Tipo", "Obra"))
                .filas(filas)
                .build();
    }
}
