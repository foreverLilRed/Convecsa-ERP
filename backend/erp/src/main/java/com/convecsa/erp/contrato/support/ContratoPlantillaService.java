package com.convecsa.erp.contrato.support;

import com.convecsa.erp.contrato.catalogo.CategoriaContrato;
import com.convecsa.erp.contrato.catalogo.PlantillaCampoEsquema;
import com.convecsa.erp.contrato.catalogo.RegimenLaboral;
import com.convecsa.erp.contrato.entity.Adenda;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.entity.PlantillaContrato;
import com.convecsa.erp.persona.domain.Genero;
import com.convecsa.erp.persona.domain.GradoInstruccion;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.entity.PersonaCelular;
import com.convecsa.erp.persona.entity.PersonaCuentaBancaria;
import com.convecsa.erp.persona.entity.PersonaExperienciaLaboral;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class ContratoPlantillaService {

    private static final DateTimeFormatter FECHA_LARGA =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-PE"));
    private static final Pattern META_TOKEN = Pattern.compile("\\{\\{meta\\.([a-zA-Z0-9_]+)\\}\\}");
    private static final Set<String> TOKENS_FIJOS = Set.of(
            "trabajador_id",
            "trabajador_nombres",
            "trabajador_apellidos",
            "trabajador_nombre_completo",
            "trabajador_tratamiento",
            "trabajador_calidad",
            "trabajador_apellido_paterno",
            "trabajador_apellido_materno",
            "trabajador_tipo_documento",
            "trabajador_dni",
            "trabajador_numero_documento",
            "trabajador_fecha_nacimiento",
            "trabajador_fecha_emision_documento",
            "trabajador_fecha_caducidad_documento",
            "trabajador_correo",
            "trabajador_telefono",
            "trabajador_direccion",
            "trabajador_departamento",
            "trabajador_provincia",
            "trabajador_distrito",
            "trabajador_genero",
            "trabajador_grado_instruccion",
            "trabajador_pension",
            "trabajador_cuspp",
            "trabajador_tipo_comision_afp",
            "trabajador_banco",
            "trabajador_numero_cuenta",
            "trabajador_cci",
            "trabajador_contacto_emergencia_nombre",
            "trabajador_contacto_emergencia_telefono",
            "trabajador_contacto_emergencia_parentesco",
            "trabajador_activo",
            "trabajador_foto_url",
            "trabajador_fecha_vigencia_emo",
            "trabajador_aptitud_medica",
            "trabajador_restricciones_medicas",
            "contrato_id",
            "titulo",
            "categoria_contrato",
            "categoria_contrato_codigo",
            "regimen_laboral",
            "regimen_laboral_codigo",
            "modalidad_contrato",
            "plantilla_nombre",
            "plantilla_codigo",
            "activo",
            "observaciones",
            "fecha_inicio",
            "fecha_fin",
            "fecha_corte",
            "duracion",
            "sueldo",
            "sueldo_letras",
            "fecha_firma",
            "fecha_fin_anterior",
            "fecha_fin_nueva",
            "duracion_adenda",
            "motivo",
            "empleador_razon",
            "empleador_ruc",
            "empleador_domicilio",
            "empleador_representante",
            "empleador_dni_representante",
            "fecha_cese",
            "tiempo_servicio",
            "tipo_cese",
            "motivo_cese",
            "liq_cts",
            "liq_grati",
            "liq_vacaciones",
            "liq_total",
            "liq_total_letras");

    public String render(Contrato contrato) {
        return apply(htmlDe(contrato), contrato);
    }

    public String render(Adenda adenda) {
        Contrato contrato = adenda.getContrato();
        PlantillaContrato plantilla = adenda.getPlantillaContrato();
        String html = plantilla != null && plantilla.getPlantilla() != null && !plantilla.getPlantilla().isBlank()
                ? plantilla.getPlantilla()
                : htmlDe(contrato);
        Map<String, String> valores = tokens(contrato);
        valores.put("fecha_fin_anterior", fecha(adenda.getFechaFinAnterior()));
        valores.put("fecha_fin_nueva", fecha(adenda.getFechaFinNueva()));
        valores.put("duracion_adenda", duracion(adenda.getFechaFinAnterior(), adenda.getFechaFinNueva()));
        valores.put("motivo", text(adenda.getMotivo()));
        valores.put("sueldo", sueldo(adenda.getSueldo()));
        valores.put("sueldo_letras", SueldoEnLetras.de(adenda.getSueldo()));
        if (adenda.getCiudadEmision() != null && !adenda.getCiudadEmision().isBlank()) {
            valores.put("ciudad_firma", text(adenda.getCiudadEmision()));
        }
        valores.put("fecha_firma", fecha(adenda.getFechaEmision()));
        String resultado = sustituirFijos(html, valores);
        Map<String, String> metadata = new LinkedHashMap<>();
        if (contrato.getMetadata() != null) {
            metadata.putAll(contrato.getMetadata());
        }
        if (adenda.getMetadata() != null) {
            metadata.putAll(adenda.getMetadata());
        }
        return replaceMeta(resultado, metadata);
    }

    public String render(String plantilla, Persona persona, Contrato contrato) {
        return render(plantilla, persona, contrato, Map.of());
    }

    public String render(String plantilla, Persona persona, Contrato contrato, Map<String, String> extras) {
        Map<String, String> valores = contrato == null ? tokensPersona(persona) : tokens(contrato);
        if (extras != null && !extras.isEmpty()) {
            valores.putAll(extras);
        }
        String html = sustituirFijos(plantilla, valores);
        Map<String, String> metadata = new LinkedHashMap<>(metaDesdePersona(persona));
        if (contrato != null && contrato.getMetadata() != null) {
            metadata.putAll(contrato.getMetadata());
        }
        return replaceMeta(html, metadata);
    }

    String apply(String plantilla, Contrato contrato) {
        String html = sustituirFijos(plantilla, tokens(contrato));
        return replaceMeta(html, contrato.getMetadata());
    }

    private static String sustituirFijos(String plantilla, Map<String, String> valores) {
        String html = plantilla;
        for (Map.Entry<String, String> entry : valores.entrySet()) {
            html = html.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return html;
    }

    static String replaceMeta(String html, Map<String, String> metadata) {
        Map<String, String> valores = metadata == null ? Collections.emptyMap() : metadata;
        Matcher matcher = META_TOKEN.matcher(html);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String clave = matcher.group(1);
            String valor = valores.getOrDefault(clave, "");
            matcher.appendReplacement(result, Matcher.quoteReplacement(valor));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public static List<PlantillaCampoEsquema> esquemaDesdeHtml(String html) {
        if (html == null || html.isBlank()) {
            return new ArrayList<>();
        }
        LinkedHashSet<String> claves = new LinkedHashSet<>();
        Matcher matcher = META_TOKEN.matcher(html);
        while (matcher.find()) {
            String clave = matcher.group(1);
            if (clave != null && !clave.isBlank()) {
                claves.add(clave);
            }
        }
        List<PlantillaCampoEsquema> esquema = new ArrayList<>();
        for (String clave : claves) {
            PlantillaCampoEsquema campo = new PlantillaCampoEsquema();
            campo.setClave(clave);
            campo.setLabel(labelDesdeClave(clave));
            campo.setTipo("text");
            campo.setCatalogo(null);
            campo.setRequerido(true);
            esquema.add(campo);
        }
        return esquema;
    }

    static String labelDesdeClave(String clave) {
        String conEspacios = clave.replace('_', ' ');
        if (conEspacios.isEmpty()) {
            return clave;
        }
        return Character.toUpperCase(conEspacios.charAt(0)) + conEspacios.substring(1);
    }

    static Map<String, String> metaDesdePersona(Persona persona) {
        Map<String, String> meta = new LinkedHashMap<>();
        String[] apellidos = partirApellidos(persona.getApellidos());
        meta.put("apellido_paterno", apellidos[0]);
        meta.put("apellido_materno", apellidos[1]);
        meta.put("fecha_nacimiento", fecha(persona.getFechaNacimiento()));
        meta.put("distrito", text(persona.getDistrito()));
        meta.put("correo", text(persona.getCorreo()));
        meta.put("telefono", primerCelular(persona));
        meta.put("nivel_educacion", grado(persona.getGradoInstruccion()));
        List<PersonaExperienciaLaboral> experiencias = experienciasOrdenadas(persona);
        for (int i = 0; i < 5; i++) {
            int indice = i + 1;
            if (i < experiencias.size()) {
                PersonaExperienciaLaboral experiencia = experiencias.get(i);
                meta.put("exp" + indice + "_empresa", text(experiencia.getOrganizacion()));
                meta.put("exp" + indice + "_cargo", text(experiencia.getCargo()));
                meta.put("exp" + indice + "_tiempo", tiempoExperiencia(experiencia));
            } else {
                meta.put("exp" + indice + "_empresa", "");
                meta.put("exp" + indice + "_cargo", "");
                meta.put("exp" + indice + "_tiempo", "");
            }
        }
        return meta;
    }

    private Map<String, String> tokens(Contrato contrato) {
        Persona persona = contrato.getPersona();
        Map<String, String> valores = tokensPersona(persona);
        valores.put("titulo", text(contrato.getTitulo()));
        CategoriaContrato categoria = contrato.getCategoriaContrato();
        RegimenLaboral regimen = categoria == null ? null : categoria.getRegimenLaboral();
        valores.put("categoria_contrato", categoria == null ? "" : categoria.getLabel());
        valores.put("categoria_contrato_codigo", categoria == null ? "" : categoria.getCodigo());
        valores.put("modalidad_contrato", categoria == null ? "" : categoria.getLabel());
        valores.put("regimen_laboral", regimen == null ? "" : regimen.getLabel());
        valores.put("regimen_laboral_codigo", regimen == null ? "" : regimen.getCodigo());
        valores.put("contrato_id", contrato.getId() == null ? "" : contrato.getId().toString());
        valores.put("plantilla_nombre", contrato.getTipoPlantilla() == null ? "" : text(contrato.getTipoPlantilla().getNombre()));
        valores.put("plantilla_codigo", contrato.getTipoPlantilla() == null ? "" : text(contrato.getTipoPlantilla().getCodigo()));
        valores.put("cargo", text(contrato.metadata("cargo")));
        valores.put("area", text(contrato.metadata("area")));
        valores.put("causa_objetiva", text(contrato.metadata("causa_objetiva")));
        valores.put("proyecto", text(contrato.metadata("proyecto")));
        valores.put("ubicacion_obra", text(contrato.metadata("ubicacion_obra")));
        valores.put("fecha_inicio", fecha(contrato.getFechaInicio()));
        valores.put("fecha_fin", fecha(contrato.getFechaFin()));
        valores.put("fecha_corte", fecha(contrato.getFechaCorte()));
        valores.put("duracion", duracion(contrato.getFechaInicio(), contrato.getFechaFin()));
        valores.put("jornada", text(contrato.metadata("jornada")));
        valores.put("sueldo", sueldo(contrato.getSueldo()));
        valores.put("sueldo_letras", SueldoEnLetras.de(contrato.getSueldo()));
        valores.put("metodo_pago", text(contrato.metadata("metodo_pago")).toLowerCase(Locale.ROOT));
        valores.put("ciudad_firma", text(contrato.metadata("ciudad_firma")));
        valores.put("observaciones", text(contrato.getObservaciones()));
        valores.put("activo", contrato.isActivo() ? "Sí" : "No");
        valores.put("fecha_firma", fecha(contrato.getFechaInicio() == null ? LocalDate.now() : contrato.getFechaInicio()));
        if (contrato.getMetadata() != null) {
            for (Map.Entry<String, String> extra : contrato.getMetadata().entrySet()) {
                if (extra.getKey() == null || TOKENS_FIJOS.contains(extra.getKey()) || extra.getValue() == null) {
                    continue;
                }
                valores.put(extra.getKey(), extra.getValue());
            }
        }
        return valores;
    }

    private Map<String, String> tokensPersona(Persona persona) {
        Map<String, String> valores = new LinkedHashMap<>();
        String[] apellidos = partirApellidos(persona.getApellidos());
        PersonaCuentaBancaria cuenta = cuentaPrincipal(persona);
        valores.put("trabajador_id", persona.getId() == null ? "" : persona.getId().toString());
        valores.put("trabajador_nombres", text(persona.getNombres()));
        valores.put("trabajador_apellidos", text(persona.getApellidos()));
        valores.put("trabajador_nombre_completo", (text(persona.getNombres()) + " " + text(persona.getApellidos())).trim());
        valores.put("trabajador_tratamiento", tratamiento(persona.getGenero()));
        valores.put("trabajador_calidad", calidad(persona.getGenero()));
        valores.put("trabajador_apellido_paterno", apellidos[0]);
        valores.put("trabajador_apellido_materno", apellidos[1]);
        valores.put("trabajador_tipo_documento", enumLabel(persona.getTipoDocumento()));
        valores.put("trabajador_dni", text(persona.getNumeroDocumento()));
        valores.put("trabajador_numero_documento", text(persona.getNumeroDocumento()));
        valores.put("trabajador_fecha_nacimiento", fecha(persona.getFechaNacimiento()));
        valores.put("trabajador_fecha_emision_documento", fecha(persona.getFechaEmisionDni()));
        valores.put("trabajador_fecha_caducidad_documento", fecha(persona.getFechaCaducidadDni()));
        valores.put("trabajador_correo", text(persona.getCorreo()));
        valores.put("trabajador_telefono", primerCelular(persona));
        valores.put("trabajador_direccion", text(persona.getDireccion()));
        valores.put("trabajador_departamento", text(persona.getDepartamento()));
        valores.put("trabajador_provincia", text(persona.getProvincia()));
        valores.put("trabajador_distrito", text(persona.getDistrito()));
        valores.put("trabajador_genero", enumLabel(persona.getGenero()));
        valores.put("trabajador_grado_instruccion", grado(persona.getGradoInstruccion()));
        valores.put("trabajador_pension", text(persona.getPension()));
        valores.put("trabajador_cuspp", text(persona.getCuspp()));
        valores.put("trabajador_tipo_comision_afp", enumLabel(persona.getTipoComisionAfp()));
        valores.put("trabajador_banco", cuenta == null ? "" : enumLabel(cuenta.getBanco()));
        valores.put("trabajador_numero_cuenta", cuenta == null ? "" : text(cuenta.getNumeroCuenta()));
        valores.put("trabajador_cci", cuenta == null ? "" : text(cuenta.getCci()));
        valores.put("trabajador_contacto_emergencia_nombre", text(persona.getContactoEmergenciaNombre()));
        valores.put("trabajador_contacto_emergencia_telefono", text(persona.getContactoEmergenciaTelefono()));
        valores.put("trabajador_contacto_emergencia_parentesco", text(persona.getContactoEmergenciaParentesco()));
        valores.put("trabajador_activo", persona.isActivo() ? "Sí" : "No");
        valores.put("trabajador_foto_url", text(persona.getFotoUrl()));
        valores.put("trabajador_fecha_vigencia_emo", fecha(persona.getFechaVigenciaEmo()));
        valores.put("trabajador_aptitud_medica", enumLabel(persona.getAptitudMedica()));
        valores.put("trabajador_restricciones_medicas", text(persona.getRestriccionesMedicas()));
        valores.put("contrato_id", "");
        valores.put("titulo", "");
        valores.put("categoria_contrato", "");
        valores.put("categoria_contrato_codigo", "");
        valores.put("regimen_laboral", "");
        valores.put("regimen_laboral_codigo", "");
        valores.put("modalidad_contrato", "");
        valores.put("plantilla_nombre", "");
        valores.put("plantilla_codigo", "");
        valores.put("activo", "");
        valores.put("observaciones", "");
        valores.put("cargo", "");
        valores.put("area", "");
        valores.put("causa_objetiva", "");
        valores.put("proyecto", "");
        valores.put("ubicacion_obra", "");
        valores.put("fecha_inicio", "");
        valores.put("fecha_fin", "");
        valores.put("fecha_corte", "");
        valores.put("duracion", "");
        valores.put("jornada", "");
        valores.put("sueldo", "");
        valores.put("sueldo_letras", "");
        valores.put("metodo_pago", "");
        valores.put("ciudad_firma", "");
        valores.put("fecha_firma", fecha(LocalDate.now()));
        valores.put("empleador_razon", "CONVECSA S.A.C.");
        valores.put("empleador_ruc", "20601414067");
        valores.put("empleador_domicilio", "Calle Van Gogh 345, Urb. San Borja, Lima");
        valores.put("empleador_representante", "OROSCO MARTÍNEZ JORGE LUIS");
        valores.put("empleador_dni_representante", "44200831");
        return valores;
    }

    private static String htmlDe(Contrato contrato) {
        PlantillaContrato plantilla = contrato.getTipoPlantilla();
        if (plantilla != null && plantilla.getPlantilla() != null && !plantilla.getPlantilla().isBlank()) {
            return plantilla.getPlantilla();
        }
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
        LocalDate afterMonths = inicio.plusMonths(meses);
        long dias = ChronoUnit.DAYS.between(afterMonths, fin);
        if (meses <= 0) {
            return dias == 1 ? "1 día" : dias + " días";
        }
        String parteMeses = meses == 1 ? "1 mes" : meses + " meses";
        if (dias <= 0) {
            return parteMeses;
        }
        return parteMeses + " con " + (dias == 1 ? "1 día" : dias + " días");
    }

    private static String tratamiento(Genero genero) {
        if (genero == Genero.FEMENINO) {
            return "doña";
        }
        if (genero == Genero.MASCULINO) {
            return "don";
        }
        return "señor(a)";
    }

    private static String calidad(Genero genero) {
        return genero == Genero.FEMENINO ? "LA TRABAJADORA" : "EL TRABAJADOR";
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

    static String[] partirApellidos(String apellidos) {
        String valor = text(apellidos).trim();
        if (valor.isEmpty()) {
            return new String[] {"", ""};
        }
        int espacio = valor.indexOf(' ');
        if (espacio < 0) {
            return new String[] {valor, ""};
        }
        return new String[] {valor.substring(0, espacio), valor.substring(espacio + 1).trim()};
    }

    private static String primerCelular(Persona persona) {
        if (persona.getCelulares() == null || persona.getCelulares().isEmpty()) {
            return "";
        }
        PersonaCelular celular = persona.getCelulares().get(0);
        return celular == null ? "" : text(celular.getNumero());
    }

    private static PersonaCuentaBancaria cuentaPrincipal(Persona persona) {
        if (persona.getCuentasBancarias() == null || persona.getCuentasBancarias().isEmpty()) {
            return null;
        }
        return persona.getCuentasBancarias().stream()
                .filter(PersonaCuentaBancaria::isPrincipal)
                .findFirst()
                .orElse(persona.getCuentasBancarias().get(0));
    }

    private static String enumLabel(Enum<?> value) {
        if (value == null) {
            return "";
        }
        String raw = value.name().replace('_', ' ').toLowerCase(Locale.ROOT);
        if (raw.length() <= 3) {
            return raw.toUpperCase(Locale.ROOT);
        }
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }

    private static List<PersonaExperienciaLaboral> experienciasOrdenadas(Persona persona) {
        if (persona.getExperienciasLaborales() == null || persona.getExperienciasLaborales().isEmpty()) {
            return List.of();
        }
        return persona.getExperienciasLaborales().stream()
                .sorted(Comparator.comparing(
                        PersonaExperienciaLaboral::getFechaInicioLaboral,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .toList();
    }

    private static String tiempoExperiencia(PersonaExperienciaLaboral experiencia) {
        if (experiencia.getFechaInicioLaboral() == null) {
            return "";
        }
        DateTimeFormatter mesAnio = DateTimeFormatter.ofPattern("MM/yyyy");
        String inicio = experiencia.getFechaInicioLaboral().format(mesAnio);
        String fin = experiencia.getFechaFin() == null ? "Actualidad" : experiencia.getFechaFin().format(mesAnio);
        return inicio + " – " + fin;
    }

    private static String grado(GradoInstruccion gradoInstruccion) {
        if (gradoInstruccion == null) {
            return "";
        }
        return switch (gradoInstruccion) {
            case PRIMARIA -> "Primaria";
            case SECUNDARIA -> "Secundaria";
            case TECNICO -> "Técnico";
            case UNIVERSITARIO -> "Universitario";
            case POSGRADO -> "Posgrado";
        };
    }
}
