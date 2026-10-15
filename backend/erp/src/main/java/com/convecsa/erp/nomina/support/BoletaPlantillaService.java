package com.convecsa.erp.nomina.support;

import com.convecsa.erp.contrato.support.SueldoEnLetras;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.entity.BoletaConcepto;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.nomina.entity.NominaDocumentoConfiguracion;
import com.convecsa.erp.nomina.entity.PlantillaBoleta;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.entity.PersonaCuentaBancaria;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class BoletaPlantillaService {

    private static final Locale ES_PE = Locale.forLanguageTag("es-PE");

    public String render(BoletaPago boleta, PlantillaBoleta plantilla) {
        return render(boleta, plantilla, null);
    }

    public String render(
            BoletaPago boleta,
            PlantillaBoleta plantilla,
            NominaDocumentoConfiguracion configuracion) {
        if (boleta == null) {
            throw new IllegalArgumentException("La boleta es obligatoria");
        }
        String html = plantilla == null || plantilla.getPlantilla() == null ? "" : plantilla.getPlantilla();
        Map<String, String> valores = tokens(boleta, configuracion);
        for (Map.Entry<String, String> entry : valores.entrySet()) {
            html = html.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return html;
    }

    Map<String, String> tokens(BoletaPago boleta) {
        return tokens(boleta, null);
    }

    Map<String, String> tokens(BoletaPago boleta, NominaDocumentoConfiguracion configuracion) {
        Persona persona = boleta.getPersona();
        PersonaCuentaBancaria cuenta = cuentaPrincipal(persona);
        Map<String, String> valores = new LinkedHashMap<>();
        valores.put("persona_nombres", text(persona == null ? null : persona.getNombres()));
        valores.put("persona_apellidos", text(persona == null ? null : persona.getApellidos()));
        valores.put("persona_dni", text(persona == null ? null : persona.getNumeroDocumento()));
        valores.put("persona_cargo", boleta.getContrato() == null ? "" : text(boleta.getContrato().getCargo()));
        valores.put("persona_cci", cuenta == null ? "" : text(cuenta.getCci()));
        valores.put("persona_banco", cuenta == null || cuenta.getBanco() == null ? "" : cuenta.getBanco().name());
        valores.put("empleador_razon", escape(configuracion == null ? "CONVECSA S.A.C." : configuracion.getRazonSocial()));
        valores.put("empleador_ruc", escape(configuracion == null ? "20601414067" : configuracion.getRuc()));
        valores.put("empleador_domicilio", escape(configuracion == null
                ? "Calle Van Gogh 345, Urb. San Borja, Lima"
                : configuracion.getDomicilio()));
        valores.put("empleador_representante", escape(configuracion == null
                ? ""
                : configuracion.getRepresentante()));
        valores.put("logo_empresa_html", imagen(configuracion == null ? null : configuracion.getLogoDataUrl(), "company-logo"));
        valores.put("firma_representante_html", imagen(
                configuracion == null ? null : configuracion.getFirmaDataUrl(), "signature-image"));
        valores.put("periodo_mes_largo", mesLargo(boleta.getPeriodo() == null ? 0 : boleta.getPeriodo().getMes()));
        valores.put("periodo_anio", boleta.getPeriodo() == null ? "" : String.valueOf(boleta.getPeriodo().getAnio()));
        valores.put("dias_trabajados", String.valueOf(boleta.getDiasTrabajados()));
        valores.put("afp_nombre", boleta.getAfp() == null ? "" : text(boleta.getAfp().getLabel()));
        valores.put("regimen", "Pequeña Empresa - REMYPE");
        valores.put("tabla_haberes", filas(boleta, CategoriaConcepto.HABER));
        valores.put("tabla_descuentos", filas(boleta, CategoriaConcepto.DESCUENTO));
        valores.put("tabla_aportes", filas(boleta, CategoriaConcepto.APORTE_EMPLEADOR));
        valores.put("total_haberes", dinero(boleta.getTotalHaberes()));
        valores.put("total_descuentos", dinero(boleta.getTotalDescuentos()));
        valores.put("neto", dinero(boleta.getNeto()));
        valores.put("neto_letras", SueldoEnLetras.de(boleta.getNeto()));
        return valores;
    }

    static String filas(BoletaPago boleta, CategoriaConcepto categoria) {
        StringBuilder filas = new StringBuilder();
        for (BoletaConcepto concepto : boleta.getConceptos()) {
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

    static String dinero(BigDecimal monto) {
        if (monto == null) {
            return "0.00";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(ES_PE);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("#,##0.00", symbols).format(monto);
    }

    static String mesLargo(int mes) {
        if (mes < 1 || mes > 12) {
            return "";
        }
        String nombre = Month.of(mes).getDisplayName(TextStyle.FULL, ES_PE);
        if (nombre.isEmpty()) {
            return nombre;
        }
        return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1);
    }

    static String escape(String value) {
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

    private static String imagen(String dataUrl, String cssClass) {
        if (dataUrl == null || !dataUrl.matches("^data:image/(png|jpeg);base64,[A-Za-z0-9+/=\\r\\n]+$")) {
            return "";
        }
        return "<img class=\"" + cssClass + "\" src=\"" + dataUrl.replace("\r", "").replace("\n", "") + "\"/>";
    }

    private static PersonaCuentaBancaria cuentaPrincipal(Persona persona) {
        if (persona == null || persona.getCuentasBancarias() == null || persona.getCuentasBancarias().isEmpty()) {
            return null;
        }
        return persona.getCuentasBancarias().stream()
                .filter(PersonaCuentaBancaria::isPrincipal)
                .findFirst()
                .orElse(persona.getCuentasBancarias().get(0));
    }
}
