package com.convecsa.erp.soma.support;

import com.convecsa.erp.soma.dto.SomaSunafilDocumentoDTO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class SomaSunafilExcelExporter {

    public byte[] exportar(SomaSunafilDocumentoDTO documento) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Formato " + documento.getFormato());
            int fila = 0;
            texto(hoja.createRow(fila++), 0, nulo(documento.getRazonSocial()));
            texto(hoja.createRow(fila++), 0, "RUC " + nulo(documento.getRuc()));
            texto(hoja.createRow(fila++), 0, nulo(documento.getDomicilio()));
            texto(hoja.createRow(fila++), 0, "Obra: " + nulo(documento.getAlcance()));
            texto(hoja.createRow(fila++), 0, "Periodo: " + nulo(documento.getPeriodo()));
            texto(hoja.createRow(fila++), 0, nulo(documento.getTitulo()));
            fila++;
            List<String> columnas = documento.getColumnas() == null ? List.of() : documento.getColumnas();
            Row encabezado = hoja.createRow(fila++);
            for (int i = 0; i < columnas.size(); i++) {
                texto(encabezado, i, columnas.get(i));
            }
            if (documento.getFilas() != null) {
                for (List<String> datos : documento.getFilas()) {
                    Row row = hoja.createRow(fila++);
                    for (int i = 0; i < columnas.size(); i++) {
                        String valor = datos != null && i < datos.size() ? datos.get(i) : "";
                        texto(row, i, valor);
                    }
                }
            }
            int anchos = Math.max(columnas.size(), 1);
            for (int i = 0; i < anchos; i++) {
                hoja.autoSizeColumn(i);
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo generar el Excel SUNAFIL", exception);
        }
    }

    private static void texto(Row fila, int columna, String valor) {
        Cell cell = fila.createCell(columna);
        if (valor != null) {
            cell.setCellValue(valor);
        }
    }

    private static String nulo(String valor) {
        return valor == null ? "" : valor;
    }
}
