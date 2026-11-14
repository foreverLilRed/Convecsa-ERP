package com.convecsa.erp.nomina.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

class BoletaPdfServiceTest {

    private final BoletaPdfService service = new BoletaPdfService();

    @Test
    void generaPdfValidoYCombinaElLote() throws Exception {
        byte[] simple = service.convertir("<html><body><h1>Boleta de pago</h1><p>S/ 1,250.00</p></body></html>");
        assertTrue(simple.length > 100);
        try (PDDocument document = PDDocument.load(simple)) {
            assertEquals(1, document.getNumberOfPages());
        }

        byte[] combinado = service.combinar(List.of(simple, simple));
        try (PDDocument document = PDDocument.load(combinado)) {
            assertEquals(2, document.getNumberOfPages());
        }
    }

    @Test
    void generaMuestraVisualConPlantillaReal() throws Exception {
        String html;
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("templates/nominas/boleta-dual-a4.html")) {
            if (input == null) {
                throw new IllegalStateException("No se encontró la plantilla dual de boleta");
            }
            html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        Map<String, String> tokens = Map.ofEntries(
                Map.entry("persona_nombre_completo", "MORAN GARCIA, PEDRO"),
                Map.entry("persona_dni", "41546529"),
                Map.entry("empleador_razon", "CONVECSA S.A.C."),
                Map.entry("empleador_ruc", "20601414067"),
                Map.entry("empleador_domicilio", "San Borja, Lima"),
                Map.entry("empleador_representante", "Representante Legal"),
                Map.entry("logo_empresa_html", ""),
                Map.entry("firma_representante_html", ""),
                Map.entry("periodo_titulo", "OCTUBRE 2026"),
                Map.entry("ficha_col1", "<table class=\"kv\"><tr><td class=\"lbl\">Apellidos y Nombres:</td>"
                        + "<td class=\"val\">MORAN GARCIA, PEDRO</td></tr></table>"),
                Map.entry("ficha_col2", "<table class=\"kv\"><tr><td class=\"lbl\">Cargo:</td>"
                        + "<td class=\"val\">Administrador</td></tr></table>"),
                Map.entry("ficha_col3", "<table class=\"kv\"><tr><td class=\"lbl\">Básico:</td>"
                        + "<td class=\"val\">2,500.00</td></tr></table>"),
                Map.entry("ficha_col4", "<table class=\"kv\"></table>"),
                Map.entry("filas_conceptos",
                        "<tr><td class=\"concepto\">Sueldo básico</td><td class=\"monto\">2,500.00</td>"
                                + "<td class=\"concepto\">AFP</td><td class=\"monto\">325.00</td>"
                                + "<td class=\"concepto\">EsSalud</td><td class=\"monto\">225.00</td></tr>"),
                Map.entry("total_haberes", "2,500.00"),
                Map.entry("total_descuentos", "325.00"),
                Map.entry("total_aportes", "225.00"),
                Map.entry("neto", "2,175.00"),
                Map.entry("neto_letras", "DOS MIL CIENTO SETENTA Y CINCO CON 00/100 SOLES"));
        for (Map.Entry<String, String> token : tokens.entrySet()) {
            html = html.replace("{{" + token.getKey() + "}}", token.getValue());
        }
        byte[] pdf = service.convertir(html);
        Path output = Path.of("target", "test-output", "boleta-sample.pdf");
        Files.createDirectories(output.getParent());
        Files.write(output, pdf);
        try (PDDocument document = PDDocument.load(pdf)) {
            assertEquals(1, document.getNumberOfPages());
        }
    }
}
