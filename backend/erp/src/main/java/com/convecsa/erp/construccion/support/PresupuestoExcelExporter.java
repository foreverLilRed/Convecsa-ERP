package com.convecsa.erp.construccion.support;

import com.convecsa.erp.construccion.dto.ApuPartidaDTO;
import com.convecsa.erp.construccion.dto.PartidaPresupuestoDTO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class PresupuestoExcelExporter {

    private static final String[] PARTIDAS = {
            "codigo", "capitulo", "descripcion", "unidad", "metrado", "precio_unitario", "centro_costo"
    };
    private static final String[] APU = {
            "codigo_partida", "tipo", "codigo", "descripcion", "unidad", "cantidad", "precio_unitario"
    };

    public byte[] exportar(List<PartidaPresupuestoDTO> partidas) {
        List<PartidaPresupuestoDTO> filas = partidas == null ? List.of() : partidas;
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hojaPartidas = libro.createSheet("Partidas");
            escribirEncabezado(hojaPartidas, PARTIDAS);
            int filaPartida = 1;
            Sheet hojaApu = libro.createSheet("Apu");
            escribirEncabezado(hojaApu, APU);
            int filaApu = 1;
            for (PartidaPresupuestoDTO partida : filas) {
                Row fila = hojaPartidas.createRow(filaPartida++);
                texto(fila, 0, partida.getCodigo());
                texto(fila, 1, partida.getCapitulo());
                texto(fila, 2, partida.getDescripcion());
                texto(fila, 3, partida.getUnidad());
                numero(fila, 4, partida.getMetrado());
                numero(fila, 5, partida.getPrecioUnitario());
                texto(fila, 6, partida.getCentroCosto());
                if (partida.getApu() == null) {
                    continue;
                }
                for (ApuPartidaDTO apu : partida.getApu()) {
                    Row recurso = hojaApu.createRow(filaApu++);
                    texto(recurso, 0, partida.getCodigo());
                    texto(recurso, 1, apu.getTipo() == null ? null : apu.getTipo().name());
                    texto(recurso, 2, apu.getCodigo());
                    texto(recurso, 3, apu.getDescripcion());
                    texto(recurso, 4, apu.getUnidad());
                    numero(recurso, 5, apu.getCantidad());
                    numero(recurso, 6, apu.getPrecioUnitario());
                }
            }
            for (int i = 0; i < PARTIDAS.length; i++) {
                hojaPartidas.autoSizeColumn(i);
            }
            for (int i = 0; i < APU.length; i++) {
                hojaApu.autoSizeColumn(i);
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo generar el Excel de presupuesto", exception);
        }
    }

    private static void escribirEncabezado(Sheet hoja, String[] columnas) {
        Row fila = hoja.createRow(0);
        for (int i = 0; i < columnas.length; i++) {
            texto(fila, i, columnas[i]);
        }
    }

    private static void texto(Row fila, int columna, String valor) {
        Cell cell = fila.createCell(columna);
        if (valor != null) {
            cell.setCellValue(valor);
        }
    }

    private static void numero(Row fila, int columna, BigDecimal valor) {
        Cell cell = fila.createCell(columna);
        if (valor != null) {
            cell.setCellValue(valor.doubleValue());
        }
    }
}
