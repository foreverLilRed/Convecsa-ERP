package com.convecsa.erp.construccion.support;

import com.convecsa.erp.construccion.domain.TipoRecursoApu;
import com.convecsa.erp.construccion.dto.ApuPartidaRequest;
import com.convecsa.erp.construccion.dto.PartidaPresupuestoRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class PresupuestoExcelParser {

    public static final long TAMANO_MAXIMO = 5L * 1024 * 1024;

    public List<PartidaPresupuestoRequest> parse(byte[] contenido, String nombreOriginal) {
        if (contenido == null || contenido.length == 0) {
            throw new IllegalArgumentException("Adjunte un archivo Excel de presupuesto");
        }
        if (contenido.length > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("El archivo no puede superar 5 MB");
        }
        String nombre = nombreOriginal == null ? "" : nombreOriginal.trim().toLowerCase(Locale.ROOT);
        if (!nombre.endsWith(".xlsx")) {
            throw new IllegalArgumentException("El archivo debe ser .xlsx");
        }
        try (Workbook libro = new XSSFWorkbook(new ByteArrayInputStream(contenido))) {
            Sheet hojaPartidas = buscarHoja(libro, "Partidas");
            if (hojaPartidas == null) {
                throw new IllegalArgumentException("Falta la hoja Partidas");
            }
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = libro.getCreationHelper().createFormulaEvaluator();
            List<PartidaPresupuestoRequest> partidas = leerPartidas(hojaPartidas, formatter, evaluator);
            if (partidas.isEmpty()) {
                throw new IllegalArgumentException("El archivo no tiene partidas");
            }
            Sheet hojaApu = buscarHoja(libro, "Apu");
            if (hojaApu != null) {
                asignarApu(partidas, hojaApu, formatter, evaluator);
            }
            return partidas;
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new IllegalArgumentException("No se pudo leer el archivo Excel");
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("No se pudo leer el archivo Excel");
        }
    }

    private static List<PartidaPresupuestoRequest> leerPartidas(
            Sheet hoja, DataFormatter formatter, FormulaEvaluator evaluator) {
        Map<String, Integer> columnas = encabezados(hoja, formatter, evaluator);
        exigir(columnas, "codigo", "Partidas");
        exigir(columnas, "descripcion", "Partidas");
        List<PartidaPresupuestoRequest> partidas = new ArrayList<>();
        for (int i = 1; i <= hoja.getLastRowNum(); i++) {
            Row fila = hoja.getRow(i);
            if (filaVacia(fila, columnas, formatter, evaluator)) {
                continue;
            }
            PartidaPresupuestoRequest partida = new PartidaPresupuestoRequest();
            partida.setCodigo(texto(fila, columnas, "codigo", formatter, evaluator));
            partida.setCapitulo(texto(fila, columnas, "capitulo", formatter, evaluator));
            partida.setDescripcion(texto(fila, columnas, "descripcion", formatter, evaluator));
            partida.setUnidad(texto(fila, columnas, "unidad", formatter, evaluator));
            partida.setMetrado(decimal(fila, columnas, "metrado", formatter, evaluator));
            partida.setPrecioUnitario(decimal(fila, columnas, "precio_unitario", formatter, evaluator));
            partida.setCentroCosto(texto(fila, columnas, "centro_costo", formatter, evaluator));
            partidas.add(partida);
        }
        return partidas;
    }

    private static void asignarApu(
            List<PartidaPresupuestoRequest> partidas,
            Sheet hoja,
            DataFormatter formatter,
            FormulaEvaluator evaluator) {
        Map<String, Integer> columnas = encabezados(hoja, formatter, evaluator);
        exigir(columnas, "codigo_partida", "Apu");
        exigir(columnas, "tipo", "Apu");
        exigir(columnas, "descripcion", "Apu");
        Map<String, PartidaPresupuestoRequest> porCodigo = new LinkedHashMap<>();
        for (PartidaPresupuestoRequest partida : partidas) {
            if (partida.getCodigo() == null || partida.getCodigo().isBlank()) {
                continue;
            }
            porCodigo.put(partida.getCodigo().trim(), partida);
        }
        for (int i = 1; i <= hoja.getLastRowNum(); i++) {
            Row fila = hoja.getRow(i);
            if (filaVacia(fila, columnas, formatter, evaluator)) {
                continue;
            }
            String codigoPartida = texto(fila, columnas, "codigo_partida", formatter, evaluator);
            if (codigoPartida == null) {
                throw new IllegalArgumentException("Cada recurso APU requiere código de partida");
            }
            PartidaPresupuestoRequest partida = porCodigo.get(codigoPartida);
            if (partida == null) {
                throw new IllegalArgumentException("El APU referencia una partida inexistente: " + codigoPartida);
            }
            ApuPartidaRequest apu = new ApuPartidaRequest();
            apu.setTipo(tipoApu(texto(fila, columnas, "tipo", formatter, evaluator)));
            apu.setCodigo(texto(fila, columnas, "codigo", formatter, evaluator));
            apu.setDescripcion(texto(fila, columnas, "descripcion", formatter, evaluator));
            apu.setUnidad(texto(fila, columnas, "unidad", formatter, evaluator));
            apu.setCantidad(decimal(fila, columnas, "cantidad", formatter, evaluator));
            apu.setPrecioUnitario(decimal(fila, columnas, "precio_unitario", formatter, evaluator));
            partida.getApu().add(apu);
        }
    }

    private static TipoRecursoApu tipoApu(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Cada recurso APU requiere tipo");
        }
        String clave = normalizar(valor).toUpperCase(Locale.ROOT);
        try {
            return TipoRecursoApu.valueOf(clave);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Tipo APU desconocido: " + valor);
        }
    }

    private static Map<String, Integer> encabezados(
            Sheet hoja, DataFormatter formatter, FormulaEvaluator evaluator) {
        Row fila = hoja.getRow(0);
        if (fila == null) {
            throw new IllegalArgumentException("La hoja " + hoja.getSheetName() + " no tiene encabezados");
        }
        Map<String, Integer> columnas = new LinkedHashMap<>();
        for (int i = 0; i < fila.getLastCellNum(); i++) {
            String bruto = celda(fila.getCell(i), formatter, evaluator);
            if (bruto == null) {
                continue;
            }
            String clave = alias(normalizar(bruto));
            if (clave != null) {
                columnas.putIfAbsent(clave, i);
            }
        }
        return columnas;
    }

    private static void exigir(Map<String, Integer> columnas, String campo, String hoja) {
        if (!columnas.containsKey(campo)) {
            throw new IllegalArgumentException("La hoja " + hoja + " requiere la columna " + campo);
        }
    }

    private static boolean filaVacia(
            Row fila, Map<String, Integer> columnas, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (fila == null) {
            return true;
        }
        for (Integer indice : columnas.values()) {
            if (celda(fila.getCell(indice), formatter, evaluator) != null) {
                return false;
            }
        }
        return true;
    }

    private static String texto(
            Row fila, Map<String, Integer> columnas, String campo, DataFormatter formatter, FormulaEvaluator evaluator) {
        Integer indice = columnas.get(campo);
        if (indice == null) {
            return null;
        }
        return celda(fila.getCell(indice), formatter, evaluator);
    }

    private static BigDecimal decimal(
            Row fila, Map<String, Integer> columnas, String campo, DataFormatter formatter, FormulaEvaluator evaluator) {
        String valor = texto(fila, columnas, campo, formatter, evaluator);
        if (valor == null) {
            return null;
        }
        String numerico = valor.replace(" ", "").replace(",", ".");
        try {
            return new BigDecimal(numerico);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Valor numérico inválido: " + valor);
        }
    }

    private static String celda(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null) {
            return null;
        }
        String valor = formatter.formatCellValue(cell, evaluator).trim();
        return valor.isEmpty() ? null : valor;
    }

    private static Sheet buscarHoja(Workbook libro, String nombre) {
        for (int i = 0; i < libro.getNumberOfSheets(); i++) {
            Sheet hoja = libro.getSheetAt(i);
            if (nombre.equalsIgnoreCase(hoja.getSheetName().trim())) {
                return hoja;
            }
        }
        return null;
    }

    private static String alias(String clave) {
        if (clave == null || clave.isEmpty()) {
            return null;
        }
        return switch (clave) {
            case "codigo" -> "codigo";
            case "capitulo" -> "capitulo";
            case "descripcion" -> "descripcion";
            case "unidad" -> "unidad";
            case "metrado" -> "metrado";
            case "precio_unitario", "pu", "p_u", "precio" -> "precio_unitario";
            case "centro_costo", "cc" -> "centro_costo";
            case "codigo_partida", "partida" -> "codigo_partida";
            case "tipo" -> "tipo";
            case "cantidad" -> "cantidad";
            default -> clave;
        };
    }

    static String normalizar(String valor) {
        String nfd = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
    }
}
