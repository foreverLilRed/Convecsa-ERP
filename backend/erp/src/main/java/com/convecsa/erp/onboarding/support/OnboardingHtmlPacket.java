package com.convecsa.erp.onboarding.support;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OnboardingHtmlPacket {

    private static final Pattern BODY = Pattern.compile("(?is)<body[^>]*>(.*)</body>");
    private static final Pattern STYLE = Pattern.compile("(?is)<style\\b[^>]*>(.*?)</style>");
    private static final String ESTILO_PAGINACION = """
            <style>
            @media print {
              html, body {
                height: auto !important;
                max-height: none !important;
                overflow: visible !important;
                background: #fff !important;
                background-color: #fff !important;
              }
              .pg, .page, .page-wrap, .onboarding-page, .doc-card, .wrap {
                height: auto !important;
                max-height: none !important;
                overflow: visible !important;
                background: #fff !important;
                background-color: #fff !important;
              }
              .onboarding-page { page-break-after: always; break-after: page; }
              .onboarding-page:last-child { page-break-after: auto; break-after: auto; }
              *, *::before, *::after {
                background: transparent !important;
                background-color: transparent !important;
                background-image: none !important;
                box-shadow: none !important;
                color: #111 !important;
                -webkit-print-color-adjust: economy !important;
                print-color-adjust: economy !important;
              }
              html, body, .pg, .page, .page-wrap, .onboarding-page, .doc-card, .wrap {
                background: #fff !important;
                background-color: #fff !important;
              }
            }
            </style>
            """;

    private OnboardingHtmlPacket() {
    }

    public static String unir(List<String> documentos) {
        if (documentos == null || documentos.isEmpty()) {
            return "";
        }
        if (documentos.size() == 1) {
            return conPaginacionAbierta(documentos.get(0));
        }
        StringBuilder estilos = new StringBuilder();
        StringBuilder cuerpos = new StringBuilder();
        for (int i = 0; i < documentos.size(); i++) {
            String html = documentos.get(i);
            estilos.append(estilosDe(html));
            cuerpos.append("<div class=\"onboarding-page\">")
                    .append(cuerpoDe(html))
                    .append("</div>\n");
        }
        return conPaginacionAbierta("""
                <!DOCTYPE html>
                <html lang="es">
                <head>
                <meta charset="UTF-8">
                <title>Expediente de onboarding</title>
                %s
                <style>
                .onboarding-page { page-break-after: always; break-after: page; }
                .onboarding-page:last-child { page-break-after: auto; break-after: auto; }
                </style>
                </head>
                <body>
                %s
                </body>
                </html>
                """.formatted(estilos, cuerpos));
    }

    static String cuerpoDe(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        Matcher matcher = BODY.matcher(html);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return html;
    }

    public static String conImpresionAutomatica(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        String conPaginas = conPaginacionAbierta(html);
        String script = "<script>window.addEventListener('load',function(){window.print();});</script>";
        int cierre = conPaginas.toLowerCase().lastIndexOf("</body>");
        if (cierre >= 0) {
            return conPaginas.substring(0, cierre) + script + conPaginas.substring(cierre);
        }
        return conPaginas + script;
    }

    public static String conEstiloImpresion(String html) {
        return conPaginacionAbierta(html);
    }

    static String conPaginacionAbierta(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        int cierreHead = html.toLowerCase().lastIndexOf("</head>");
        if (cierreHead >= 0) {
            return html.substring(0, cierreHead) + ESTILO_PAGINACION + html.substring(cierreHead);
        }
        int htmlOpen = html.toLowerCase().indexOf("<html");
        if (htmlOpen >= 0) {
            int tagEnd = html.indexOf('>', htmlOpen);
            if (tagEnd >= 0) {
                return html.substring(0, tagEnd + 1)
                        + "<head>" + ESTILO_PAGINACION + "</head>"
                        + html.substring(tagEnd + 1);
            }
        }
        return "<html><head>" + ESTILO_PAGINACION + "</head><body>" + html + "</body></html>";
    }

    static String estilosDe(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        Matcher matcher = STYLE.matcher(html);
        StringBuilder estilos = new StringBuilder();
        while (matcher.find()) {
            estilos.append("<style>").append(matcher.group(1)).append("</style>\n");
        }
        return estilos.toString();
    }
}
