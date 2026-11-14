package com.convecsa.erp.nomina.support;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class BoletaDualFilas {

    private static final Locale ES_PE = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public record Celda(String concepto, String monto) {
        static Celda vacia() {
            return new Celda("", "");
        }
    }

    private BoletaDualFilas() {
    }

    public static String zippear(List<Celda> ingresos, List<Celda> descuentos, List<Celda> aportes) {
        int n = Math.max(tamano(ingresos), Math.max(tamano(descuentos), tamano(aportes)));
        if (n == 0) {
            return fila(Celda.vacia(), Celda.vacia(), Celda.vacia());
        }
        StringBuilder filas = new StringBuilder();
        for (int i = 0; i < n; i++) {
            filas.append(fila(en(ingresos, i), en(descuentos, i), en(aportes, i)));
        }
        return filas.toString();
    }

    public static String columna(String... filas) {
        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("<table class=\"kv\">");
        for (String fila : filas) {
            if (fila != null && !fila.isBlank()) {
                cuerpo.append(fila);
            }
        }
        cuerpo.append("</table>");
        return cuerpo.toString();
    }

    public static String dato(String etiqueta, String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        return "<tr><td class=\"lbl\">"
                + escape(etiqueta)
                + "</td><td class=\"val\">"
                + escape(valor)
                + "</td></tr>";
    }

    public static String dinero(BigDecimal monto) {
        if (monto == null) {
            return "0.00";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(ES_PE);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("#,##0.00", symbols).format(monto);
    }

    public static String horas(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(ES_PE);
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("0.##", symbols).format(valor);
    }

    public static String fecha(LocalDate valor) {
        return valor == null ? "" : valor.format(FECHA);
    }

    public static String mesLargo(int mes) {
        if (mes < 1 || mes > 12) {
            return "";
        }
        String nombre = Month.of(mes).getDisplayName(TextStyle.FULL, ES_PE);
        if (nombre.isEmpty()) {
            return nombre;
        }
        return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1);
    }

    public static String escape(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public static String text(String value) {
        return value == null ? "" : value;
    }

    public static String imagen(String dataUrl, String cssClass) {
        if (dataUrl == null || !dataUrl.matches("^data:image/(png|jpeg);base64,[A-Za-z0-9+/=\\r\\n]+$")) {
            return "";
        }
        return "<img class=\"" + cssClass + "\" src=\"" + dataUrl.replace("\r", "").replace("\n", "") + "\"/>";
    }

    public static String aplicar(String html, Map<String, String> valores) {
        String resultado = html == null ? "" : html;
        for (Map.Entry<String, String> entry : valores.entrySet()) {
            resultado = resultado.replace("{{" + entry.getKey() + "}}", entry.getValue() == null ? "" : entry.getValue());
        }
        return resultado;
    }

    public static boolean tieneMonto(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) > 0;
    }

    private static String fila(Celda ingreso, Celda descuento, Celda aporte) {
        return "<tr>"
                + tdConcepto(ingreso)
                + tdMonto(ingreso)
                + tdConcepto(descuento)
                + tdMonto(descuento)
                + tdConcepto(aporte)
                + tdMonto(aporte)
                + "</tr>";
    }

    private static String tdConcepto(Celda celda) {
        return "<td class=\"concepto\">" + escape(celda == null ? "" : text(celda.concepto())) + "</td>";
    }

    private static String tdMonto(Celda celda) {
        return "<td class=\"monto\">" + escape(celda == null ? "" : text(celda.monto())) + "</td>";
    }

    private static int tamano(List<Celda> lista) {
        return lista == null ? 0 : lista.size();
    }

    private static Celda en(List<Celda> lista, int indice) {
        if (lista == null || indice >= lista.size()) {
            return Celda.vacia();
        }
        Celda celda = lista.get(indice);
        return celda == null ? Celda.vacia() : celda;
    }
}
