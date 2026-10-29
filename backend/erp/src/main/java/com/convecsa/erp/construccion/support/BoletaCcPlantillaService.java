package com.convecsa.erp.construccion.support;

import com.convecsa.erp.construccion.entity.BoletaCc;
import com.convecsa.erp.construccion.entity.BoletaCcConcepto;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import com.convecsa.erp.contrato.support.SueldoEnLetras;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.entity.NominaDocumentoConfiguracion;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class BoletaCcPlantillaService {

    private static final Locale ES_PE = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String render(
            BoletaCc boleta,
            NominaDocumentoConfiguracion configuracion,
            List<TareoCcDia> dias,
            BigDecimal tarifaDia,
            BigDecimal tarifaHora) {
        Persona persona = boleta.getPersona();
        PeriodoCc periodo = boleta.getPeriodo();
        String razon = text(configuracion == null ? "CONVECSA S.A.C." : configuracion.getRazonSocial());
        String ruc = text(configuracion == null ? "20601414067" : configuracion.getRuc());
        String domicilio = text(configuracion == null
                ? "Calle Van Gogh 345, Urb. San Borja, Lima"
                : configuracion.getDomicilio());
        return """
                <!DOCTYPE html>
                <html xmlns="http://www.w3.org/1999/xhtml">
                <head>
                  <meta charset="UTF-8"/>
                  <title>Boleta RCC</title>
                  <style>
                    body { font-family: Arial, Helvetica, sans-serif; font-size: 12px; color: #0f172a; margin: 24px; }
                    h1 { font-size: 16px; margin: 0 0 4px; }
                    .muted { color: #64748b; font-size: 11px; }
                    table { width: 100%%; border-collapse: collapse; margin-top: 12px; }
                    th, td { border: 1px solid #cbd5e1; padding: 6px 8px; }
                    th { background: #e2e8f0; text-align: left; }
                    td.num, th.num { text-align: right; }
                    .totales { font-weight: bold; }
                    .grid { display: table; width: 100%%; margin-top: 8px; }
                    .grid div { display: table-cell; }
                  </style>
                </head>
                <body>
                  <h1>Boleta de pago — Construcción Civil</h1>
                  <p class="muted">%s · RUC %s<br/>%s</p>
                  <div class="grid">
                    <div>
                      <p><strong>Trabajador:</strong> %s, %s<br/>
                      <strong>Documento:</strong> %s</p>
                    </div>
                    <div>
                      <p><strong>Periodo:</strong> %s al %s<br/>
                      <strong>Obra:</strong> %s — %s<br/>
                      <strong>Tarifa día:</strong> %s &nbsp; <strong>Tarifa hora:</strong> %s</p>
                    </div>
                  </div>
                  <table>
                    <thead>
                      <tr>
                        <th>Fecha</th>
                        <th>Código</th>
                        <th class="num">Horas ord.</th>
                        <th class="num">HE 60%%</th>
                        <th class="num">HE 100%%</th>
                      </tr>
                    </thead>
                    <tbody>%s</tbody>
                  </table>
                  <table>
                    <thead><tr><th>Haberes</th><th class="num">Monto (S/)</th></tr></thead>
                    <tbody>%s</tbody>
                  </table>
                  <table>
                    <thead><tr><th>Descuentos</th><th class="num">Monto (S/)</th></tr></thead>
                    <tbody>%s</tbody>
                  </table>
                  <table>
                    <thead><tr><th>Aportes del empleador</th><th class="num">Monto (S/)</th></tr></thead>
                    <tbody>%s</tbody>
                  </table>
                  <table>
                    <tbody>
                      <tr class="totales"><td>Total haberes</td><td class="num">%s</td></tr>
                      <tr class="totales"><td>Total descuentos</td><td class="num">%s</td></tr>
                      <tr class="totales"><td>Neto a pagar</td><td class="num">%s</td></tr>
                    </tbody>
                  </table>
                  <p class="muted">Son: %s</p>
                </body>
                </html>
                """.formatted(
                escape(razon),
                escape(ruc),
                escape(domicilio),
                escape(persona == null ? "" : persona.getApellidos()),
                escape(persona == null ? "" : persona.getNombres()),
                escape(persona == null ? "" : persona.getNumeroDocumento()),
                periodo == null ? "" : periodo.getFechaDesde().format(FECHA),
                periodo == null ? "" : periodo.getFechaHasta().format(FECHA),
                periodo == null || periodo.getProyecto() == null ? "" : escape(periodo.getProyecto().getCodigo()),
                periodo == null || periodo.getProyecto() == null ? "" : escape(periodo.getProyecto().getNombre()),
                dinero(tarifaDia),
                dinero(tarifaHora),
                filasTareo(dias),
                filas(boleta, CategoriaConcepto.HABER),
                filas(boleta, CategoriaConcepto.DESCUENTO),
                filas(boleta, CategoriaConcepto.APORTE_EMPLEADOR),
                dinero(boleta.getTotalHaberes()),
                dinero(boleta.getTotalDescuentos()),
                dinero(boleta.getNeto()),
                escape(SueldoEnLetras.de(boleta.getNeto())));
    }

    private static String filasTareo(List<TareoCcDia> dias) {
        if (dias == null || dias.isEmpty()) {
            return "<tr><td colspan=\"5\">Sin tareo en el periodo</td></tr>";
        }
        StringBuilder filas = new StringBuilder();
        for (TareoCcDia dia : dias) {
            filas.append("<tr><td>")
                    .append(dia.getFecha() == null ? "" : dia.getFecha().format(FECHA))
                    .append("</td><td>")
                    .append(escape(dia.getCodigo() == null ? "" : dia.getCodigo().name()))
                    .append("</td><td class=\"num\">")
                    .append(horas(dia.getHorasOrd()))
                    .append("</td><td class=\"num\">")
                    .append(horas(dia.getHe60()))
                    .append("</td><td class=\"num\">")
                    .append(horas(dia.getHe100()))
                    .append("</td></tr>");
        }
        return filas.toString();
    }

    private static String filas(BoletaCc boleta, CategoriaConcepto categoria) {
        StringBuilder filas = new StringBuilder();
        for (BoletaCcConcepto concepto : boleta.getConceptos()) {
            if (concepto.getCategoria() != categoria) {
                continue;
            }
            filas.append("<tr><td>")
                    .append(escape(concepto.getLabel()))
                    .append("</td><td class=\"num\">")
                    .append(dinero(concepto.getMonto()))
                    .append("</td></tr>");
        }
        if (filas.isEmpty()) {
            return "<tr><td>-</td><td class=\"num\">0.00</td></tr>";
        }
        return filas.toString();
    }

    private static String dinero(BigDecimal monto) {
        if (monto == null) {
            return "0.00";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(ES_PE);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("#,##0.00", symbols).format(monto);
    }

    private static String horas(BigDecimal valor) {
        if (valor == null) {
            return "0.00";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(ES_PE);
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("0.##", symbols).format(valor);
    }

    private static String escape(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
