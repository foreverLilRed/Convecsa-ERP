package com.convecsa.erp.soma.support;

import com.convecsa.erp.nomina.support.BoletaPdfService;
import com.convecsa.erp.soma.dto.SomaSunafilDocumentoDTO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SomaSunafilPdfService {

    private final BoletaPdfService boletaPdfService;

    public byte[] generar(SomaSunafilDocumentoDTO documento) {
        String plantilla = cargarPlantilla(documento.getFormato());
        String logo = "";
        if (documento.getLogoDataUrl() != null && !documento.getLogoDataUrl().isBlank()) {
            logo = "<img src=\"" + escAttr(documento.getLogoDataUrl()) + "\" alt=\"Logo\" style=\"height:48px;\"/>";
        }
        String html = plantilla
                .replace("{{logo}}", logo)
                .replace("{{razonSocial}}", esc(nulo(documento.getRazonSocial())))
                .replace("{{ruc}}", esc(nulo(documento.getRuc())))
                .replace("{{domicilio}}", esc(nulo(documento.getDomicilio())))
                .replace("{{titulo}}", esc(nulo(documento.getTitulo())))
                .replace("{{alcance}}", esc(nulo(documento.getAlcance())))
                .replace("{{periodo}}", esc(nulo(documento.getPeriodo())))
                .replace("{{thead}}", thead(documento.getColumnas()))
                .replace("{{filas}}", filas(documento.getFilas(), documento.getColumnas().size()));
        return boletaPdfService.convertir(html);
    }

    private static String cargarPlantilla(int formato) {
        ClassPathResource resource = new ClassPathResource("templates/soma/sunafil-formato-" + formato + ".html");
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("No se encontró la plantilla SUNAFIL formato " + formato, ex);
        }
    }

    private static String thead(List<String> columnas) {
        StringBuilder sb = new StringBuilder();
        for (String columna : columnas) {
            sb.append("<th>").append(esc(nulo(columna))).append("</th>");
        }
        return sb.toString();
    }

    private static String filas(List<List<String>> filas, int columnas) {
        if (filas == null || filas.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (List<String> fila : filas) {
            sb.append("<tr>");
            for (int i = 0; i < columnas; i++) {
                String valor = fila != null && i < fila.size() ? fila.get(i) : "";
                sb.append("<td>").append(esc(nulo(valor))).append("</td>");
            }
            sb.append("</tr>");
        }
        return sb.toString();
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
