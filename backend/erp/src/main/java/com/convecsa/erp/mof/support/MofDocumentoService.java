package com.convecsa.erp.mof.support;

import com.convecsa.erp.mof.dto.MofPuestoDTO;
import com.convecsa.erp.onboarding.support.OnboardingHtmlPacket;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MofDocumentoService {

    private static final Map<String, String> GRUPO_LABELS = Map.of(
            "STAFF DE OBRA", "STAFF DE OBRA Y CONTROL DE PRODUCCIÓN",
            "ADMINISTRACIÓN", "GESTIÓN ADMINISTRATIVA Y SOPORTE",
            "HÍBRIDOS", "CARGOS HÍBRIDOS Y FORMACIÓN",
            "TECNICOS", "TÉCNICOS ESPECIALISTAS DE CAMPO",
            "AUXILIARES", "AUXILIARES DE CAMPO");

    public String render(List<MofPuestoDTO> puestos) {
        StringBuilder cards = new StringBuilder();
        for (MofPuestoDTO puesto : puestos) {
            cards.append(card(puesto));
        }
        return OnboardingHtmlPacket.conEstiloImpresion("""
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8"/>
                  <title>MOF - CONVECSA</title>
                  <style>
                    @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap');
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    @page { size: A4 portrait; margin: 0; }
                    html, body { background: #f1f5f9; font-family: Inter, sans-serif; color: #0f172a; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
                    .wrap { max-width: 820px; margin: 0 auto; padding: 24px 16px 40px; }
                    .iso-header { width: 100%%; border-collapse: collapse; background: #fff; border: 2px solid #0f172a; margin-bottom: 24px; }
                    .iso-header td { border: 1px solid #cbd5e1; padding: 8px 12px; vertical-align: middle; }
                    .iso-logo { width: 25%%; text-align: center; background: #fafafa; }
                    .iso-title { width: 50%%; text-align: center; }
                    .iso-meta { width: 25%%; font-size: 7.5pt; color: #334155; line-height: 1.4; }
                    @media print {
                      html, body { background: #fff !important; }
                      .wrap { padding: 10mm 12mm !important; max-width: 100%% !important; }
                    }
                  </style>
                </head>
                <body>
                  <div class="wrap">
                    <table class="iso-header">
                      <tr>
                        <td class="iso-logo">
                          <div style="font-size:16pt;font-weight:900;color:#1d4ed8;letter-spacing:-0.02em;">CONVECSA</div>
                        </td>
                        <td class="iso-title">
                          <div style="font-size:10pt;font-weight:800;color:#0f172a;letter-spacing:.02em;text-transform:uppercase;">CONVECSA S.A.C.</div>
                          <div style="font-size:11pt;font-weight:900;color:#1d4ed8;margin:2px 0;">MANUAL DE ORGANIZACIÓN Y FUNCIONES</div>
                          <div style="font-size:7.5pt;color:#64748b;font-weight:600;text-transform:uppercase;letter-spacing:.05em;">Sistema de Gestión de Calidad (SGC)</div>
                        </td>
                        <td class="iso-meta">
                          <div style="border-bottom:1px solid #e2e8f0;margin-bottom:3px;"><strong>Código:</strong> CVSA-MOF</div>
                          <div style="border-bottom:1px solid #e2e8f0;margin-bottom:3px;"><strong>Versión:</strong> Rev. 00</div>
                          <div><strong>Fecha:</strong> 01/09/2025</div>
                        </td>
                      </tr>
                    </table>
                    %s
                  </div>
                </body>
                </html>
                """.formatted(cards));
    }

    private static String card(MofPuestoDTO puesto) {
        String grupo = GRUPO_LABELS.getOrDefault(puesto.getGrupo(), puesto.getGrupo());
        StringBuilder fnRows = new StringBuilder();
        List<String> funciones = puesto.getFunciones() == null ? List.of() : puesto.getFunciones();
        for (int i = 0; i < funciones.size(); i++) {
            fnRows.append("""
                    <tr style="border-bottom:1px solid #f3f4f6;">
                      <td style="padding:5px 8px 5px 4px;font-size:8.5pt;color:#1d4ed8;font-weight:700;vertical-align:top;width:24px;">%s.</td>
                      <td style="padding:5px 4px;font-size:8.5pt;line-height:1.5;color:#374151;">%s</td>
                    </tr>
                    """.formatted(letra(i), esc(funciones.get(i))));
        }
        return """
                <div style="page-break-inside:avoid;margin-bottom:24px;border:1px solid #d1d5db;border-radius:8px;overflow:hidden;background:#fff;">
                  <div style="background:#f8fafc;padding:12px 16px;border-bottom:2px solid #1d4ed8;">
                    <div style="display:flex;align-items:center;gap:8px;margin-bottom:2px;">
                      <span style="background:#1d4ed8;color:#fff;font-size:7.5pt;font-weight:800;padding:2px 8px;border-radius:4px;">%s</span>
                      <span style="font-size:8pt;color:#4b5563;font-weight:700;text-transform:uppercase;">%s</span>
                    </div>
                    <div style="font-size:13pt;font-weight:800;color:#0f172a;text-transform:uppercase;">%s</div>
                  </div>
                  <div style="padding:14px 16px;">
                    <div style="margin-bottom:12px;">
                      <div style="font-size:7.5pt;color:#1d4ed8;font-weight:800;text-transform:uppercase;margin-bottom:4px;">1. Propósito general del cargo</div>
                      <div style="background:#eff6ff;border-left:3px solid #1d4ed8;padding:8px 12px;font-size:8.5pt;color:#1e293b;line-height:1.55;">%s</div>
                    </div>
                    <div style="margin-bottom:12px;display:grid;grid-template-columns:1fr 1fr;gap:10px;font-size:8.5pt;">
                      <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:6px;padding:8px 12px;">
                        <span style="font-weight:800;color:#475569;font-size:7.5pt;text-transform:uppercase;display:block;margin-bottom:2px;">Superior inmediato</span>
                        <span style="color:#1e293b;font-weight:600;">%s</span>
                      </div>
                      <div style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:6px;padding:8px 12px;">
                        <span style="font-weight:800;color:#475569;font-size:7.5pt;text-transform:uppercase;display:block;margin-bottom:2px;">Personal a cargo</span>
                        <span style="color:#1e293b;font-weight:600;">%s</span>
                      </div>
                    </div>
                    <div style="margin-bottom:14px;">
                      <div style="font-size:7.5pt;color:#1d4ed8;font-weight:800;text-transform:uppercase;margin-bottom:6px;">2. Funciones y responsabilidades principales</div>
                      <table style="width:100%%;border-collapse:collapse;border:1px solid #e5e7eb;">%s</table>
                    </div>
                    <div>
                      <div style="font-size:7.5pt;color:#1d4ed8;font-weight:800;text-transform:uppercase;margin-bottom:6px;">3. Perfil y requisitos mínimos del puesto</div>
                      <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:8px;">
                        %s
                        %s
                        %s
                      </div>
                    </div>
                  </div>
                </div>
                """.formatted(
                esc(puesto.getCodigo()),
                esc(grupo),
                esc(puesto.getCargo()),
                esc(puesto.getProposito()),
                esc(puesto.getSuperior()),
                esc(puesto.getSubordinados()),
                fnRows,
                requisitoBox("#f0f9ff", "#bae6fd", "#0284c7", "Educación / formación", puesto.getEducacion()),
                requisitoBox("#fff7ed", "#ffedd5", "#ea580c", "Experiencia previa", puesto.getExperiencia()),
                requisitoBox("#f8fafc", "#e2e8f0", "#475569", "Competencias clave", puesto.getCompetencias()));
    }

    private static String requisitoBox(String bg, String border, String color, String titulo, List<String> items) {
        StringBuilder bullets = new StringBuilder();
        for (String item : items == null ? List.<String>of() : items) {
            bullets.append("<div style=\"font-size:7.5pt;color:#334155;margin-bottom:3px;\">• ")
                    .append(esc(item))
                    .append("</div>");
        }
        return """
                <div style="background:%s;border:1px solid %s;border-radius:6px;padding:8px 10px;">
                  <div style="font-size:7pt;font-weight:800;color:%s;text-transform:uppercase;margin-bottom:4px;border-bottom:1px solid %s;">%s</div>
                  %s
                </div>
                """.formatted(bg, border, color, border, esc(titulo), bullets);
    }

    static String letra(int indice) {
        return String.valueOf((char) ('a' + (indice % 26)));
    }

    static String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

}
