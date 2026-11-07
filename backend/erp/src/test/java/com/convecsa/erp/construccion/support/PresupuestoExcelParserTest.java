package com.convecsa.erp.construccion.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.construccion.domain.TipoRecursoApu;
import com.convecsa.erp.construccion.dto.ApuPartidaDTO;
import com.convecsa.erp.construccion.dto.ApuPartidaRequest;
import com.convecsa.erp.construccion.dto.PartidaPresupuestoDTO;
import com.convecsa.erp.construccion.dto.PartidaPresupuestoRequest;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class PresupuestoExcelParserTest {

    private final PresupuestoExcelParser parser = new PresupuestoExcelParser();
    private final PresupuestoExcelExporter exporter = new PresupuestoExcelExporter();

    @Test
    void leePartidasYApuConAliasDeEncabezado() throws Exception {
        byte[] archivo = libro((libro) -> {
            Sheet partidas = libro.createSheet("Partidas");
            Row encabezado = partidas.createRow(0);
            encabezado.createCell(0).setCellValue("Código");
            encabezado.createCell(1).setCellValue("Capítulo");
            encabezado.createCell(2).setCellValue("Descripción");
            encabezado.createCell(3).setCellValue("Unidad");
            encabezado.createCell(4).setCellValue("Metrado");
            encabezado.createCell(5).setCellValue("P.U.");
            encabezado.createCell(6).setCellValue("Centro costo");
            Row p1 = partidas.createRow(1);
            p1.createCell(0).setCellValue("01.01");
            p1.createCell(2).setCellValue("Excavación");
            p1.createCell(3).setCellValue("m3");
            p1.createCell(4).setCellValue(3);
            p1.createCell(5).setCellValue(99);
            Row p2 = partidas.createRow(2);
            p2.createCell(0).setCellValue("01.02");
            p2.createCell(2).setCellValue("Relleno");
            p2.createCell(5).setCellValue(10);

            Sheet apu = libro.createSheet("Apu");
            Row apuHead = apu.createRow(0);
            apuHead.createCell(0).setCellValue("codigo_partida");
            apuHead.createCell(1).setCellValue("tipo");
            apuHead.createCell(2).setCellValue("codigo");
            apuHead.createCell(3).setCellValue("descripcion");
            apuHead.createCell(4).setCellValue("unidad");
            apuHead.createCell(5).setCellValue("cantidad");
            apuHead.createCell(6).setCellValue("precio_unitario");
            Row a1 = apu.createRow(1);
            a1.createCell(0).setCellValue("01.01");
            a1.createCell(1).setCellValue("MANO_OBRA");
            a1.createCell(3).setCellValue("Jornal");
            a1.createCell(5).setCellValue(2);
            a1.createCell(6).setCellValue(10);
            Row a2 = apu.createRow(2);
            a2.createCell(0).setCellValue("01.01");
            a2.createCell(1).setCellValue("MATERIAL");
            a2.createCell(3).setCellValue("Arena");
            a2.createCell(5).setCellValue(1);
            a2.createCell(6).setCellValue(5);
        });

        List<PartidaPresupuestoRequest> filas = parser.parse(archivo, "presupuesto.xlsx");
        assertEquals(2, filas.size());
        assertEquals("01.01", filas.get(0).getCodigo());
        assertEquals(2, filas.get(0).getApu().size());
        ApuPartidaRequest jornal = filas.get(0).getApu().get(0);
        assertEquals(TipoRecursoApu.MANO_OBRA, jornal.getTipo());
        assertEquals(0, new BigDecimal("2").compareTo(jornal.getCantidad()));
        assertEquals(0, new BigDecimal("10").compareTo(jornal.getPrecioUnitario()));
        ApuPartidaRequest material = filas.get(0).getApu().get(1);
        assertEquals(TipoRecursoApu.MATERIAL, material.getTipo());
        assertEquals(0, new BigDecimal("1").compareTo(material.getCantidad()));
        assertEquals(0, new BigDecimal("5").compareTo(material.getPrecioUnitario()));
        assertEquals("01.02", filas.get(1).getCodigo());
        assertTrue(filas.get(1).getApu().isEmpty());
        assertEquals(0, new BigDecimal("10").compareTo(filas.get(1).getPrecioUnitario()));
    }

    @Test
    void roundTripConPlantillaExportada() {
        PartidaPresupuestoDTO partida = PartidaPresupuestoDTO.builder()
                .codigo("02.01")
                .descripcion("Encofrado")
                .unidad("m2")
                .metrado(new BigDecimal("4"))
                .precioUnitario(new BigDecimal("12.50"))
                .centroCosto("CC-1")
                .orden(0)
                .apu(List.of(ApuPartidaDTO.builder()
                        .tipo(TipoRecursoApu.EQUIPO)
                        .descripcion("Mezcladora")
                        .cantidad(new BigDecimal("1"))
                        .precioUnitario(new BigDecimal("12.50"))
                        .parcial(new BigDecimal("12.50"))
                        .orden(0)
                        .build()))
                .build();
        byte[] archivo = exporter.exportar(List.of(partida));
        List<PartidaPresupuestoRequest> filas = parser.parse(archivo, "plantilla.xlsx");
        assertEquals(1, filas.size());
        assertEquals("02.01", filas.get(0).getCodigo());
        assertEquals("CC-1", filas.get(0).getCentroCosto());
        assertEquals(1, filas.get(0).getApu().size());
        assertEquals(TipoRecursoApu.EQUIPO, filas.get(0).getApu().get(0).getTipo());
    }

    @Test
    void rechazaHojaPartidasSinDatos() throws Exception {
        byte[] archivo = libro((libro) -> {
            Sheet partidas = libro.createSheet("Partidas");
            Row encabezado = partidas.createRow(0);
            encabezado.createCell(0).setCellValue("codigo");
            encabezado.createCell(1).setCellValue("descripcion");
        });
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> parser.parse(archivo, "vacio.xlsx"));
        assertEquals("El archivo no tiene partidas", error.getMessage());
    }

    @Test
    void rechazaTipoApuDesconocido() throws Exception {
        byte[] archivo = libro((libro) -> {
            Sheet partidas = libro.createSheet("Partidas");
            Row encabezado = partidas.createRow(0);
            encabezado.createCell(0).setCellValue("codigo");
            encabezado.createCell(1).setCellValue("descripcion");
            Row p1 = partidas.createRow(1);
            p1.createCell(0).setCellValue("01.01");
            p1.createCell(1).setCellValue("Excavación");
            Sheet apu = libro.createSheet("Apu");
            Row apuHead = apu.createRow(0);
            apuHead.createCell(0).setCellValue("codigo_partida");
            apuHead.createCell(1).setCellValue("tipo");
            apuHead.createCell(2).setCellValue("descripcion");
            Row a1 = apu.createRow(1);
            a1.createCell(0).setCellValue("01.01");
            a1.createCell(1).setCellValue("FOO");
            a1.createCell(2).setCellValue("X");
        });
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> parser.parse(archivo, "malo.xlsx"));
        assertEquals("Tipo APU desconocido: FOO", error.getMessage());
    }

    @Test
    void rechazaExtensionDistintaDeXlsx() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> parser.parse(new byte[] {1, 2, 3}, "presupuesto.xls"));
        assertEquals("El archivo debe ser .xlsx", error.getMessage());
    }

    private static byte[] libro(LibroWriter writer) throws Exception {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            writer.write(libro);
            libro.write(salida);
            return salida.toByteArray();
        }
    }

    @FunctionalInterface
    private interface LibroWriter {
        void write(XSSFWorkbook libro);
    }
}
