package com.convecsa.erp.construccion.support;

import com.convecsa.erp.construccion.domain.CategoriaObrera;
import com.convecsa.erp.construccion.domain.CodigoTareoCc;
import com.convecsa.erp.construccion.entity.BoletaCc;
import com.convecsa.erp.construccion.entity.BoletaCcConcepto;
import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.support.SueldoEnLetras;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.entity.NominaDocumentoConfiguracion;
import com.convecsa.erp.nomina.support.BoletaDualFilas;
import com.convecsa.erp.nomina.support.BoletaDualFilas.Celda;
import com.convecsa.erp.persona.entity.Persona;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class BoletaCcPlantillaService {

    private static final String PLANTILLA = "templates/nominas/boleta-dual-a4.html";

    public String render(
            BoletaCc boleta,
            NominaDocumentoConfiguracion configuracion,
            List<TareoCcDia> dias,
            BigDecimal tarifaDia,
            BigDecimal tarifaHora) {
        return BoletaDualFilas.aplicar(plantilla(), tokens(boleta, configuracion, dias, tarifaDia, tarifaHora));
    }

    Map<String, String> tokens(
            BoletaCc boleta,
            NominaDocumentoConfiguracion configuracion,
            List<TareoCcDia> dias,
            BigDecimal tarifaDia,
            BigDecimal tarifaHora) {
        Persona persona = boleta.getPersona();
        Contrato contrato = boleta.getContrato();
        PeriodoCc periodo = boleta.getPeriodo();
        ContratoCc contratoCc = contrato == null ? null : contrato.getConstruccionCivil();
        String apellidos = BoletaDualFilas.text(persona == null ? null : persona.getApellidos());
        String nombres = BoletaDualFilas.text(persona == null ? null : persona.getNombres());
        String nombreCompleto = (apellidos + (nombres.isBlank() ? "" : ", " + nombres)).trim();
        if (nombreCompleto.startsWith(",")) {
            nombreCompleto = nombres;
        }
        ResumenTareo tareo = resumir(dias);
        Map<String, String> valores = new LinkedHashMap<>();
        valores.put("persona_nombres", nombres);
        valores.put("persona_apellidos", apellidos);
        valores.put("persona_nombre_completo", nombreCompleto);
        valores.put("persona_dni", BoletaDualFilas.text(persona == null ? null : persona.getNumeroDocumento()));
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
        valores.put("periodo_titulo", BoletaDualFilas.escape(periodoTitulo(periodo)));
        valores.put("filas_conceptos", BoletaDualFilas.zippear(
                celdas(boleta, CategoriaConcepto.HABER),
                celdas(boleta, CategoriaConcepto.DESCUENTO),
                celdas(boleta, CategoriaConcepto.APORTE_EMPLEADOR)));
        valores.put("ficha_col1", fichaCol1(persona, nombreCompleto));
        valores.put("ficha_col2", fichaCol2(contrato, contratoCc));
        valores.put("ficha_col3", fichaCol3(tarifaDia, tareo));
        valores.put("ficha_col4", fichaCol4(tareo));
        valores.put("total_haberes", BoletaDualFilas.dinero(boleta.getTotalHaberes()));
        valores.put("total_descuentos", BoletaDualFilas.dinero(boleta.getTotalDescuentos()));
        valores.put("total_aportes", BoletaDualFilas.dinero(boleta.getTotalAportesEmpleador()));
        valores.put("neto", BoletaDualFilas.dinero(boleta.getNeto()));
        valores.put("neto_letras", SueldoEnLetras.de(boleta.getNeto()));
        return valores;
    }

    private static String fichaCol1(Persona persona, String nombreCompleto) {
        return BoletaDualFilas.columna(
                BoletaDualFilas.dato("Apellidos y Nombres:", nombreCompleto),
                BoletaDualFilas.dato("Documento:", persona == null ? "" : persona.getNumeroDocumento()),
                BoletaDualFilas.dato("Reg. Pens:", persona == null ? "" : persona.getPension()),
                BoletaDualFilas.dato("CUSSP:", persona == null ? "" : persona.getCuspp()));
    }

    private static String fichaCol2(Contrato contrato, ContratoCc contratoCc) {
        String categoria = contratoCc == null ? "" : etiqueta(contratoCc.getCategoria());
        String cargo = contrato == null ? "" : BoletaDualFilas.text(contrato.metadata("cargo"));
        return BoletaDualFilas.columna(
                BoletaDualFilas.dato("Categoría:", categoria),
                BoletaDualFilas.dato("Cargo:", cargo),
                BoletaDualFilas.dato("Fec. Ingreso:",
                        contrato == null ? "" : BoletaDualFilas.fecha(contrato.getFechaInicio())),
                BoletaDualFilas.dato("Fec. Cese:", fechaCese(contrato)));
    }

    private static String fichaCol3(BigDecimal tarifaDia, ResumenTareo tareo) {
        return BoletaDualFilas.columna(
                BoletaDualFilas.dato("Básico:", BoletaDualFilas.tieneMonto(tarifaDia)
                        ? BoletaDualFilas.dinero(tarifaDia)
                        : ""),
                BoletaDualFilas.dato("Días D Medico:", tareo.diasMedicos > 0
                        ? String.valueOf(tareo.diasMedicos)
                        : ""),
                BoletaDualFilas.dato("Hor. Ord.:", BoletaDualFilas.tieneMonto(tareo.horasOrd)
                        ? BoletaDualFilas.horas(tareo.horasOrd)
                        : ""),
                BoletaDualFilas.dato("Días computable:", tareo.conTareo
                        ? String.valueOf(tareo.diasLaborados)
                        : ""));
    }

    private static String fichaCol4(ResumenTareo tareo) {
        return BoletaDualFilas.columna(
                BoletaDualFilas.dato("H. Extra 60%:", BoletaDualFilas.tieneMonto(tareo.he60)
                        ? BoletaDualFilas.horas(tareo.he60)
                        : ""),
                BoletaDualFilas.dato("H. Extra 100%:", BoletaDualFilas.tieneMonto(tareo.he100)
                        ? BoletaDualFilas.horas(tareo.he100)
                        : ""));
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

    private static String periodoTitulo(PeriodoCc periodo) {
        if (periodo == null || periodo.getFechaDesde() == null || periodo.getFechaHasta() == null) {
            return "";
        }
        return BoletaDualFilas.fecha(periodo.getFechaDesde())
                + " AL "
                + BoletaDualFilas.fecha(periodo.getFechaHasta());
    }

    private static String etiqueta(CategoriaObrera categoria) {
        if (categoria == null) {
            return "";
        }
        String nombre = categoria.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1);
    }

    private static List<Celda> celdas(BoletaCc boleta, CategoriaConcepto categoria) {
        List<Celda> celdas = new ArrayList<>();
        for (BoletaCcConcepto concepto : boleta.getConceptos()) {
            if (concepto.getCategoria() != categoria) {
                continue;
            }
            celdas.add(new Celda(concepto.getLabel(), BoletaDualFilas.dinero(concepto.getMonto())));
        }
        return celdas;
    }

    private static ResumenTareo resumir(List<TareoCcDia> dias) {
        ResumenTareo resumen = new ResumenTareo();
        if (dias == null || dias.isEmpty()) {
            return resumen;
        }
        resumen.conTareo = true;
        for (TareoCcDia dia : dias) {
            if (dia.getHorasOrd() != null) {
                resumen.horasOrd = resumen.horasOrd.add(dia.getHorasOrd());
            }
            if (dia.getHe60() != null) {
                resumen.he60 = resumen.he60.add(dia.getHe60());
            }
            if (dia.getHe100() != null) {
                resumen.he100 = resumen.he100.add(dia.getHe100());
            }
            if (dia.getCodigo() == CodigoTareoCc.LAB) {
                resumen.diasLaborados++;
            }
            if (dia.getCodigo() == CodigoTareoCc.DM) {
                resumen.diasMedicos++;
            }
        }
        return resumen;
    }

    private static String plantilla() {
        try {
            return new ClassPathResource(PLANTILLA).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("No se encontró la plantilla dual de boleta", ex);
        }
    }

    private static final class ResumenTareo {
        private boolean conTareo;
        private int diasLaborados;
        private int diasMedicos;
        private BigDecimal horasOrd = BigDecimal.ZERO;
        private BigDecimal he60 = BigDecimal.ZERO;
        private BigDecimal he100 = BigDecimal.ZERO;
    }
}
