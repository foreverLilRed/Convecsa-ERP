package com.convecsa.erp.nomina.support;

import com.convecsa.erp.contrato.catalogo.CategoriaContrato;
import com.convecsa.erp.contrato.catalogo.RegimenLaboral;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.support.SueldoEnLetras;
import com.convecsa.erp.nomina.catalogo.Afp;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.entity.BoletaConcepto;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.nomina.entity.NominaDocumentoConfiguracion;
import com.convecsa.erp.nomina.entity.PlantillaBoleta;
import com.convecsa.erp.nomina.support.BoletaDualFilas.Celda;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.entity.PersonaCuentaBancaria;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class BoletaPlantillaService {

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
        return BoletaDualFilas.aplicar(html, tokens(boleta, configuracion));
    }

    Map<String, String> tokens(BoletaPago boleta) {
        return tokens(boleta, null);
    }

    Map<String, String> tokens(BoletaPago boleta, NominaDocumentoConfiguracion configuracion) {
        Persona persona = boleta.getPersona();
        Contrato contrato = boleta.getContrato();
        PersonaCuentaBancaria cuenta = cuentaPrincipal(persona);
        String apellidos = BoletaDualFilas.text(persona == null ? null : persona.getApellidos());
        String nombres = BoletaDualFilas.text(persona == null ? null : persona.getNombres());
        String nombreCompleto = (apellidos + (nombres.isBlank() ? "" : ", " + nombres)).trim();
        if (nombreCompleto.startsWith(",")) {
            nombreCompleto = nombres;
        }
        Map<String, String> valores = new LinkedHashMap<>();
        valores.put("persona_nombres", nombres);
        valores.put("persona_apellidos", apellidos);
        valores.put("persona_nombre_completo", nombreCompleto);
        valores.put("persona_dni", BoletaDualFilas.text(persona == null ? null : persona.getNumeroDocumento()));
        valores.put("persona_cargo", contrato == null ? "" : BoletaDualFilas.text(contrato.metadata("cargo")));
        valores.put("persona_cci", cuenta == null ? "" : BoletaDualFilas.text(cuenta.getCci()));
        valores.put("persona_banco", cuenta == null || cuenta.getBanco() == null ? "" : cuenta.getBanco().name());
        valores.put("empleador_razon", BoletaDualFilas.escape(
                configuracion == null ? "CONVECSA S.A.C." : configuracion.getRazonSocial()));
        valores.put("empleador_ruc", BoletaDualFilas.escape(
                configuracion == null ? "20601414067" : configuracion.getRuc()));
        valores.put("empleador_domicilio", BoletaDualFilas.escape(configuracion == null
                ? "Calle Van Gogh 345, Urb. San Borja, Lima"
                : configuracion.getDomicilio()));
        valores.put("empleador_representante", BoletaDualFilas.escape(
                configuracion == null ? "" : configuracion.getRepresentante()));
        valores.put("logo_empresa_html", BoletaDualFilas.imagen(
                configuracion == null ? null : configuracion.getLogoDataUrl(), "company-logo"));
        valores.put("firma_representante_html", BoletaDualFilas.imagen(
                configuracion == null ? null : configuracion.getFirmaDataUrl(), "signature-image"));
        String periodoTitulo = periodoTitulo(boleta);
        valores.put("periodo_titulo", BoletaDualFilas.escape(periodoTitulo));
        valores.put("periodo_mes_largo", BoletaDualFilas.mesLargo(
                boleta.getPeriodo() == null ? 0 : boleta.getPeriodo().getMes()));
        valores.put("periodo_anio", boleta.getPeriodo() == null ? "" : String.valueOf(boleta.getPeriodo().getAnio()));
        valores.put("dias_trabajados", String.valueOf(boleta.getDiasTrabajados()));
        valores.put("afp_nombre", boleta.getAfp() == null ? "" : BoletaDualFilas.text(boleta.getAfp().getLabel()));
        valores.put("regimen", regimenDelContrato(contrato));
        valores.put("tabla_haberes", filas(boleta, CategoriaConcepto.HABER));
        valores.put("tabla_descuentos", filas(boleta, CategoriaConcepto.DESCUENTO));
        valores.put("tabla_aportes", filas(boleta, CategoriaConcepto.APORTE_EMPLEADOR));
        valores.put("filas_conceptos", filasConceptos(boleta));
        valores.put("ficha_col1", fichaCol1(persona, boleta.getAfp(), nombreCompleto));
        valores.put("ficha_col2", fichaCol2(contrato));
        valores.put("ficha_col3", fichaCol3(contrato, boleta.getDiasTrabajados()));
        valores.put("ficha_col4", BoletaDualFilas.columna());
        valores.put("total_haberes", BoletaDualFilas.dinero(boleta.getTotalHaberes()));
        valores.put("total_descuentos", BoletaDualFilas.dinero(boleta.getTotalDescuentos()));
        valores.put("total_aportes", BoletaDualFilas.dinero(boleta.getTotalAportesEmpleador()));
        valores.put("neto", BoletaDualFilas.dinero(boleta.getNeto()));
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
                    .append(BoletaDualFilas.escape(concepto.getLabel()))
                    .append("</td><td class=\"num\">")
                    .append(BoletaDualFilas.dinero(concepto.getMonto()))
                    .append("</td></tr>");
        }
        if (filas.isEmpty()) {
            return "<tr><td>-</td><td class=\"num\">0.00</td></tr>";
        }
        return filas.toString();
    }

    static String dinero(BigDecimal monto) {
        return BoletaDualFilas.dinero(monto);
    }

    static String mesLargo(int mes) {
        return BoletaDualFilas.mesLargo(mes);
    }

    static String escape(String value) {
        return BoletaDualFilas.escape(value);
    }

    private static String filasConceptos(BoletaPago boleta) {
        return BoletaDualFilas.zippear(
                celdas(boleta, CategoriaConcepto.HABER),
                celdas(boleta, CategoriaConcepto.DESCUENTO),
                celdas(boleta, CategoriaConcepto.APORTE_EMPLEADOR));
    }

    private static List<Celda> celdas(BoletaPago boleta, CategoriaConcepto categoria) {
        List<Celda> celdas = new ArrayList<>();
        for (BoletaConcepto concepto : boleta.getConceptos()) {
            if (concepto.getCategoria() != categoria) {
                continue;
            }
            celdas.add(new Celda(concepto.getLabel(), BoletaDualFilas.dinero(concepto.getMonto())));
        }
        return celdas;
    }

    private static String fichaCol1(Persona persona, Afp afp, String nombreCompleto) {
        return BoletaDualFilas.columna(
                BoletaDualFilas.dato("Apellidos y Nombres:", nombreCompleto),
                BoletaDualFilas.dato("Documento:", persona == null ? "" : persona.getNumeroDocumento()),
                BoletaDualFilas.dato("Reg. Pens:", regimenPrevisional(persona, afp)),
                BoletaDualFilas.dato("CUSSP:", persona == null ? "" : persona.getCuspp()));
    }

    private static String fichaCol2(Contrato contrato) {
        return BoletaDualFilas.columna(
                BoletaDualFilas.dato("Cargo:", contrato == null ? "" : contrato.metadata("cargo")),
                BoletaDualFilas.dato("Fec. Ingreso:",
                        contrato == null ? "" : BoletaDualFilas.fecha(contrato.getFechaInicio())),
                BoletaDualFilas.dato("Fec. Cese:", fechaCese(contrato)));
    }

    private static String fichaCol3(Contrato contrato, int diasTrabajados) {
        String basico = contrato == null || contrato.getSueldo() == null
                ? ""
                : BoletaDualFilas.dinero(contrato.getSueldo());
        return BoletaDualFilas.columna(
                BoletaDualFilas.dato("Básico:", basico),
                BoletaDualFilas.dato("Días computable:", String.valueOf(diasTrabajados)));
    }

    private static String fechaCese(Contrato contrato) {
        if (contrato == null) {
            return "";
        }
        if (contrato.getFechaFin() != null) {
            return BoletaDualFilas.fecha(contrato.getFechaFin());
        }
        if (!contrato.isActivo() && contrato.getFechaCorte() != null) {
            return BoletaDualFilas.fecha(contrato.getFechaCorte());
        }
        return "";
    }

    private static String regimenPrevisional(Persona persona, Afp afp) {
        if (persona != null && persona.getPension() != null && !persona.getPension().isBlank()) {
            return persona.getPension();
        }
        if (afp == null) {
            return "";
        }
        if (afp.isEsOnp()) {
            return "ONP";
        }
        return BoletaDualFilas.text(afp.getLabel());
    }

    private static String periodoTitulo(BoletaPago boleta) {
        if (boleta.getPeriodo() == null) {
            return "";
        }
        String mes = BoletaDualFilas.mesLargo(boleta.getPeriodo().getMes());
        if (mes.isBlank()) {
            return String.valueOf(boleta.getPeriodo().getAnio());
        }
        return (mes + " " + boleta.getPeriodo().getAnio()).toUpperCase();
    }

    private static String regimenDelContrato(Contrato contrato) {
        if (contrato == null || contrato.getCategoriaContrato() == null) {
            return "";
        }
        CategoriaContrato categoria = contrato.getCategoriaContrato();
        RegimenLaboral regimen = categoria.getRegimenLaboral();
        return regimen == null ? "" : BoletaDualFilas.text(regimen.getLabel());
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
