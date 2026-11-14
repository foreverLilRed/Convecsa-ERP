package com.convecsa.erp.soma.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.soma.dto.SomaSunafilDocumentoDTO;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class SomaSunafilExcelExporterTest {

    private final SomaSunafilExcelExporter exporter = new SomaSunafilExcelExporter();

    @Test
    void escribeMembreteYFilaDeDatos() throws Exception {
        byte[] xlsx = exporter.exportar(documento(List.of(List.of("1", "Accidente", "0000"))));
        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            Sheet hoja = libro.getSheetAt(0);
            assertEquals("Convecsa SAC", hoja.getRow(0).getCell(0).getStringCellValue());
            assertEquals("RUC 20100000000", hoja.getRow(1).getCell(0).getStringCellValue());
            assertTrue(hoja.getRow(3).getCell(0).getStringCellValue().contains("Obra"));
            Row encabezado = hoja.getRow(7);
            assertEquals("N°", encabezado.getCell(0).getStringCellValue());
            Row datos = hoja.getRow(8);
            assertNotNull(datos);
            assertEquals("1", datos.getCell(0).getStringCellValue());
            assertEquals("Accidente", datos.getCell(1).getStringCellValue());
            assertEquals("0000", datos.getCell(2).getStringCellValue());
        }
    }

    @Test
    void libroVacioConservaEncabezados() throws Exception {
        byte[] xlsx = exporter.exportar(documento(List.of()));
        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            Sheet hoja = libro.getSheetAt(0);
            assertEquals("Convecsa SAC", hoja.getRow(0).getCell(0).getStringCellValue());
            assertEquals("N°", hoja.getRow(7).getCell(0).getStringCellValue());
            assertEquals("Tipo", hoja.getRow(7).getCell(1).getStringCellValue());
            assertEquals(null, hoja.getRow(8));
        }
    }

    private static SomaSunafilDocumentoDTO documento(List<List<String>> filas) {
        return SomaSunafilDocumentoDTO.builder()
                .formato(1)
                .titulo("Registro de accidentes")
                .razonSocial("Convecsa SAC")
                .ruc("20100000000")
                .domicilio("Lima")
                .alcance("0000 — Obra piloto")
                .periodo("Histórico")
                .columnas(List.of("N°", "Tipo", "Obra"))
                .filas(filas)
                .build();
    }
}
