package com.convecsa.erp.contrato.support;

import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.persona.entity.Persona;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class ContratoPlantillaService {

    private static final DateTimeFormatter FECHA_LARGA =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-PE"));

    public String render(Contrato contrato) {
        String plantilla = load();
        Map<String, String> valores = tokens(contrato);
        String html = plantilla;
        for (Map.Entry<String, String> entry : valores.entrySet()) {
            html = html.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return html;
    }

    private Map<String, String> tokens(Contrato contrato) {
        Persona persona = contrato.getPersona();
        Map<String, String> valores = new LinkedHashMap<>();
        valores.put("trabajador_nombres", text(persona.getNombres()));
        valores.put("trabajador_apellidos", text(persona.getApellidos()));
        valores.put("trabajador_dni", text(persona.getNumeroDocumento()));
        valores.put("trabajador_direccion", text(persona.getDireccion()));
        valores.put("cargo", text(contrato.getCargo()));
        valores.put("area", text(contrato.getArea()));
        valores.put("causa_objetiva", contrato.getCausaObjetiva() == null ? "" : contrato.getCausaObjetiva().getLabel());
        valores.put("proyecto", text(contrato.getProyecto()));
        valores.put("ubicacion_obra", text(contrato.getUbicacionObra()));
        valores.put("fecha_inicio", fecha(contrato.getFechaInicio()));
        valores.put("fecha_fin", fecha(contrato.getFechaFin()));
        valores.put("duracion", duracion(contrato.getFechaInicio(), contrato.getFechaFin()));
        valores.put("jornada", contrato.getJornada() == null ? "" : contrato.getJornada().getLabel());
        valores.put("sueldo", sueldo(contrato.getSueldo()));
        valores.put("sueldo_letras", SueldoEnLetras.de(contrato.getSueldo()));
        valores.put("metodo_pago", contrato.getMetodoPago() == null ? "" : contrato.getMetodoPago().getLabel().toLowerCase(Locale.ROOT));
        valores.put("ciudad_firma", text(contrato.getCiudadFirma()));
        valores.put("fecha_firma", fecha(contrato.getFechaInicio() == null ? LocalDate.now() : contrato.getFechaInicio()));
        valores.put("empleador_razon", "CONVECSA S.A.C.");
        valores.put("empleador_ruc", "20601414067");
        valores.put("empleador_domicilio", "Calle Van Gogh 345, Urb. San Borja, Lima");
        valores.put("empleador_representante", "OROSCO MARTÍNEZ JORGE LUIS");
        valores.put("empleador_dni_representante", "44200831");
        return valores;
    }

    private static String load() {
        try {
            return new ClassPathResource("templates/contratos/plantillas/contrato-servicio-especifico.html")
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException("No se pudo leer la plantilla de contrato", exception);
        }
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    private static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FECHA_LARGA);
    }

    private static String duracion(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) {
            return "";
        }
        long meses = ChronoUnit.MONTHS.between(inicio, fin);
        if (meses <= 0) {
            long dias = ChronoUnit.DAYS.between(inicio, fin);
            return dias == 1 ? "1 día" : dias + " días";
        }
        return meses == 1 ? "1 mes" : meses + " meses";
    }

    private static String sueldo(java.math.BigDecimal monto) {
        if (monto == null) {
            return "";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("es-PE"));
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("#,##0.00", symbols).format(monto);
    }
}
