package com.convecsa.erp.contrato.support;

import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.persona.entity.Persona;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConstanciaTrabajoService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern(
            "d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-PE"));
    private static final String LOGO = cargarLogo();
    private final ContratoRepository contratos;

    @Transactional(readOnly = true)
    public String generar(Long contratoId) {
        Contrato contrato = contratos.findById(contratoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el contrato"));
        if (contrato.getFechaInicio() == null) {
            throw new IllegalArgumentException("El contrato no tiene fecha de inicio para generar la constancia");
        }
        Persona persona = contrato.getPersona();
        LocalDate hoy = LocalDate.now();
        if (contrato.getFechaInicio().isAfter(hoy)) {
            throw new IllegalArgumentException("El contrato todavía no ha iniciado");
        }
        LocalDate fin = contrato.getFechaCorte() != null ? contrato.getFechaCorte() : contrato.getFechaFin();
        if (!contrato.isActivo() && fin == null) {
            throw new IllegalArgumentException("El contrato no tiene fecha de término para generar la constancia");
        }
        boolean vigente = contrato.isActivo() && (fin == null || !fin.isBefore(hoy));
        String periodo = vigente
                ? "labora para nuestra empresa desde el <strong>" + FECHA.format(contrato.getFechaInicio())
                        + "</strong> hasta la fecha"
                : "laboró para nuestra empresa desde el <strong>" + FECHA.format(contrato.getFechaInicio())
                        + "</strong> hasta el <strong>" + FECHA.format(fin) + "</strong>";
        String cargo = dato(contrato.metadata("cargo"), contrato.getTitulo());
        String proyecto = dato(contrato.metadata("proyecto"), contrato.metadata("nombre_obra"));
        String proyectoTexto = proyecto == null ? ""
                : " en el proyecto <strong>" + esc(proyecto) + "</strong>";
        String nombre = esc(persona.getNombres() + " " + persona.getApellidos());
        String tipoDocumento = esc(persona.getTipoDocumento().name());
        String numeroDocumento = esc(persona.getNumeroDocumento());
        return """
                <!doctype html><html lang="es"><head><meta charset="utf-8">
                <title>Constancia de trabajo - %s</title><style>
                @page{size:A4;margin:0}*{box-sizing:border-box}body{margin:0;background:#dbe4ee;font-family:Arial,sans-serif;color:#1e293b}
                .page{width:210mm;min-height:297mm;margin:0 auto 20px;background:white;padding:20mm 22mm 15mm;display:flex;flex-direction:column;box-shadow:0 8px 24px #33415533}
                .brand{height:27mm;border-bottom:2px solid #0052cc;display:flex;align-items:center;padding-bottom:3mm}
                .brand img{max-width:80mm;max-height:22mm;object-fit:contain}
                h1{color:#0052cc;text-align:center;font-size:17pt;letter-spacing:.08em;margin:17mm 0 12mm}
                p{font-size:10pt;line-height:1.7;text-align:justify;margin:0 0 6mm}.saludo{font-weight:700}
                .fecha{margin-top:auto;text-align:right;font-style:italic}.firma{width:64mm;margin-top:20mm;border-top:1px solid #1e293b;padding-top:6px;font-size:9pt}
                .firma strong{display:block}.firma span{color:#475569}.footer{border-top:1px solid #e2e8f0;padding-top:5mm;text-align:center;color:#64748b;font-size:8pt;margin-top:18mm}
                @media print{body{background:white}.page{margin:0;box-shadow:none;width:210mm;min-height:297mm}}
                </style></head><body>
                <main class="page"><div class="brand"><img src="%s" alt="Logo CONVECSA"></div><h1>CONSTANCIA DE TRABAJO</h1>
                <p class="saludo">A QUIEN CORRESPONDA:</p>
                <p>Por medio de la presente, <strong>CONVECSA S.A.C.</strong>, con RUC N.° <strong>20601414067</strong> y domicilio fiscal en Calle Van Gogh 345, Urb. San Borja, Lima, deja constancia de que <strong>%s</strong>, con %s N.° <strong>%s</strong>, %s.</p>
                <p>Durante dicho período, desempeñó el cargo de <strong>%s</strong>%s, según su contrato de trabajo.</p>
                <p>Se expide la presente constancia para los fines que la persona interesada estime convenientes.</p>
                <p class="fecha">Lima, %s</p><div class="firma"><strong>OROSCO MARTÍNEZ JORGE LUIS</strong><span>Gerente General · CONVECSA S.A.C.</span></div>
                <div class="footer">CONVECSA S.A.C. · RUC 20601414067</div></main></body></html>
                """.formatted(nombre, LOGO, nombre, tipoDocumento, numeroDocumento, periodo,
                esc(cargo), proyectoTexto, FECHA.format(hoy));
    }

    private static String cargarLogo() {
        try (InputStream imagen = ConstanciaTrabajoService.class.getResourceAsStream(
                "/documentos/convecsa-logo.png")) {
            if (imagen == null) throw new IllegalStateException("No se encontró el logo de CONVECSA");
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imagen.readAllBytes());
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar el logo de CONVECSA", ex);
        }
    }

    private static String dato(String primero, String segundo) {
        if (primero != null && !primero.isBlank()) return primero.trim();
        return segundo == null || segundo.isBlank() ? null : segundo.trim();
    }

    private static String esc(String valor) {
        return valor == null ? "" : valor.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
