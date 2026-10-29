package com.convecsa.erp.nomina.declaracion.support;

import com.convecsa.erp.nomina.declaracion.entity.ReciboHonorario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RxhParser {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter PE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public List<ReciboHonorario> parsear(String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El archivo de recibos por honorarios está vacío");
        }
        String normalizado = contenido.replace("\uFEFF", "").replace("\r\n", "\n").replace('\r', '\n');
        List<ReciboHonorario> filas = new ArrayList<>();
        int nro = 0;
        for (String cruda : normalizado.split("\n")) {
            nro++;
            String linea = cruda.trim();
            if (linea.isBlank() || linea.startsWith("#")) {
                continue;
            }
            String[] cols = linea.split("\\|", -1);
            if (cols.length < 16) {
                throw new IllegalArgumentException(
                        "La línea " + nro + " no tiene 16 columnas del layout SUNAT de RxH");
            }
            ReciboHonorario recibo = new ReciboHonorario();
            recibo.setTipoDocumento(cols[0].trim());
            recibo.setNumeroDocumento(cols[1].trim());
            recibo.setApellidoPaterno(vacioANull(cols[2]));
            recibo.setApellidoMaterno(vacioANull(cols[3]));
            recibo.setNombres(vacioANull(cols[4]));
            recibo.setDomiciliado(booleano(cols[5]));
            recibo.setTipoComprobante(cols[7].trim().isBlank() ? "R" : cols[7].trim());
            recibo.setSerie(cols[8].trim());
            recibo.setNumero(cols[9].trim());
            recibo.setMonto(decimal(cols[10], nro));
            recibo.setFechaEmision(fecha(cols[11]));
            recibo.setFechaPago(fecha(cols[12]));
            recibo.setRetencionCuarta(booleano(cols[13]));
            recibo.setRetencionPension(booleano(cols[14]));
            recibo.setImporteAportePension(cols[15].isBlank() ? BigDecimal.ZERO : decimal(cols[15], nro));
            if (recibo.getNumeroDocumento().isBlank() || recibo.getSerie().isBlank() || recibo.getNumero().isBlank()) {
                throw new IllegalArgumentException("La línea " + nro + " no tiene documento, serie o número");
            }
            filas.add(recibo);
        }
        if (filas.isEmpty()) {
            throw new IllegalArgumentException("El archivo de recibos por honorarios no contiene filas");
        }
        return filas;
    }

    public static String dniDesdeDocumento(String tipoDocumento, String numeroDocumento) {
        if (numeroDocumento == null) {
            return null;
        }
        String numero = numeroDocumento.trim();
        if ("06".equals(tipoDocumento) && numero.startsWith("10") && numero.length() >= 11) {
            return numero.substring(2, 10);
        }
        if ("01".equals(tipoDocumento) || tipoDocumento == null || tipoDocumento.isBlank()) {
            return numero;
        }
        return numero;
    }

    private static BigDecimal decimal(String valor, int nro) {
        try {
            return new BigDecimal(valor.trim().replace(",", ""));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Monto inválido en la línea " + nro);
        }
    }

    private static LocalDate fecha(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String texto = valor.trim();
        try {
            return LocalDate.parse(texto, ISO);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(texto, PE);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Fecha inválida: " + texto);
            }
        }
    }

    private static boolean booleano(String valor) {
        if (valor == null) {
            return false;
        }
        String texto = valor.trim().toUpperCase(Locale.ROOT);
        return texto.equals("1") || texto.equals("S") || texto.equals("SI") || texto.equals("TRUE");
    }

    private static String vacioANull(String valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.trim();
        return texto.isBlank() ? null : texto;
    }
}
