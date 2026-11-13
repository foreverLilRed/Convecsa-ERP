package com.convecsa.erp.soma.support;

import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.nomina.entity.NominaDocumentoConfiguracion;
import com.convecsa.erp.nomina.repository.NominaDocumentoConfiguracionRepository;
import com.convecsa.erp.nomina.support.BoletaPdfService;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.entity.SomaEntregaEpp;
import com.convecsa.erp.soma.entity.SomaEntregaEppItem;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SomaEppKardexPdfService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NominaDocumentoConfiguracionRepository configuracionRepository;
    private final BoletaPdfService boletaPdfService;

    public byte[] generar(Proyecto proyecto, Persona persona, List<SomaEntregaEpp> entregas) {
        String plantilla = cargarPlantilla();
        NominaDocumentoConfiguracion cfg =
                configuracionRepository.findById(NominaDocumentoConfiguracion.ID_UNICO).orElse(null);

        String logo = "";
        String razon = "";
        String ruc = "";
        String domicilio = "";
        if (cfg != null) {
            razon = nulo(cfg.getRazonSocial());
            ruc = nulo(cfg.getRuc());
            domicilio = nulo(cfg.getDomicilio());
            if (cfg.getLogoDataUrl() != null && !cfg.getLogoDataUrl().isBlank()) {
                logo = "<img src=\"" + escAttr(cfg.getLogoDataUrl()) + "\" alt=\"Logo\" style=\"height:48px;\"/>";
            }
        }

        String html = plantilla
                .replace("{{logo}}", logo)
                .replace("{{razonSocial}}", esc(razon))
                .replace("{{ruc}}", esc(ruc))
                .replace("{{domicilio}}", esc(domicilio))
                .replace("{{obraCodigo}}", esc(proyecto.getCodigo()))
                .replace("{{obraNombre}}", esc(proyecto.getNombre()))
                .replace("{{trabajador}}", esc(nombreCompleto(persona)))
                .replace("{{documento}}", esc(persona.getNumeroDocumento()))
                .replace("{{filas}}", armarFilas(entregas));
        return boletaPdfService.convertir(html);
    }

    private static String armarFilas(List<SomaEntregaEpp> entregas) {
        StringBuilder filas = new StringBuilder();
        for (SomaEntregaEpp entrega : entregas) {
            for (SomaEntregaEppItem item : entrega.getItems()) {
                String material = item.getMaterial() == null
                        ? ""
                        : nulo(item.getMaterial().getCodigo()) + " — " + nulo(item.getMaterial().getDescripcion());
                filas.append("<tr>")
                        .append("<td>")
                        .append(esc(fecha(entrega.getFechaEntrega())))
                        .append("</td>")
                        .append("<td>")
                        .append(esc(material))
                        .append("</td>")
                        .append("<td class=\"num\">")
                        .append(esc(cantidad(item.getCantidad())))
                        .append("</td>")
                        .append("<td>")
                        .append(esc(nulo(item.getTalla())))
                        .append("</td>")
                        .append("<td>")
                        .append(esc(entrega.getMotivo() == null ? "" : entrega.getMotivo().name()))
                        .append("</td>")
                        .append("<td>")
                        .append(esc(fecha(item.getFechaVencimientoEstimada())))
                        .append("</td>")
                        .append("<td>")
                        .append(esc(item.getEstadoItem() == null ? "" : item.getEstadoItem().name()))
                        .append("</td>")
                        .append("</tr>");
            }
        }
        if (filas.isEmpty()) {
            filas.append("<tr><td colspan=\"7\">Sin entregas de EPP registradas.</td></tr>");
        }
        return filas.toString();
    }

    private static String cargarPlantilla() {
        ClassPathResource resource = new ClassPathResource("templates/soma/kardex-epp.html");
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("No se encontró la plantilla de kardex EPP", ex);
        }
    }

    private static String nombreCompleto(Persona persona) {
        if (persona == null) {
            return "";
        }
        String apellidos = persona.getApellidos() == null ? "" : persona.getApellidos().trim();
        String nombres = persona.getNombres() == null ? "" : persona.getNombres().trim();
        return (apellidos + " " + nombres).trim();
    }

    private static String fecha(LocalDate valor) {
        return valor == null ? "" : FECHA.format(valor);
    }

    private static String cantidad(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return valor.stripTrailingZeros().toPlainString();
    }

    private static String nulo(String valor) {
        return valor == null ? "" : valor;
    }

    private static String esc(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "";
        }
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String escAttr(String valor) {
        return esc(valor).replace("'", "&#39;");
    }
}
