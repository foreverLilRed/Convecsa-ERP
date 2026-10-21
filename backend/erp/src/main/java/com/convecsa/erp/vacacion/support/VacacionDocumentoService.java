package com.convecsa.erp.vacacion.support;

import com.convecsa.erp.vacacion.entity.VacacionSolicitud;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class VacacionDocumentoService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String generar(VacacionSolicitud solicitud) {
        String persona = esc(solicitud.getPersona().getNombres() + " " + solicitud.getPersona().getApellidos());
        String documento = esc(solicitud.getPersona().getNumeroDocumento());
        String titulo = solicitud.getTipo().name().equals("ADELANTADA")
                ? "CONVENIO DE ADELANTO DE VACACIONES"
                : "CONSTANCIA DE PROGRAMACIÓN VACACIONAL";
        String sustento = solicitud.getTipo().name().equals("ADELANTADA")
                ? "El presente acuerdo deja constancia del adelanto vacacional convenido por ambas partes conforme al Decreto Legislativo N.° 1405 y su reglamento."
                : "El presente documento deja constancia de la programación del descanso vacacional acordada por ambas partes.";
        return """
                <!doctype html><html lang="es"><head><meta charset="utf-8"><title>%s</title>
                <style>@page{size:A4;margin:25mm}body{font-family:Arial,sans-serif;color:#172033;font-size:12px;line-height:1.65}h1{text-align:center;font-size:17px;margin:20px 0 30px}.box{border:1px solid #cbd5e1;border-radius:8px;padding:16px;margin:18px 0}.row{display:flex;justify-content:space-between;border-bottom:1px solid #e2e8f0;padding:6px 0}.row:last-child{border:0}.sign{display:flex;gap:70px;margin-top:80px}.sign div{width:45%%;text-align:center;border-top:1px solid #334155;padding-top:8px}@media print{button{display:none}}</style>
                </head><body><button onclick="window.print()">Imprimir / guardar PDF</button><h1>%s</h1>
                <p>Conste por el presente documento el acuerdo celebrado entre <strong>CONVECSA</strong> y <strong>%s</strong>, identificado(a) con documento N.° <strong>%s</strong>.</p>
                <div class="box"><div class="row"><span>Tipo</span><strong>%s</strong></div><div class="row"><span>Periodo acumulativo</span><strong>%s al %s</strong></div><div class="row"><span>Días calendario</span><strong>%d</strong></div><div class="row"><span>Descanso</span><strong>%s al %s</strong></div><div class="row"><span>Retorno laboral</span><strong>%s</strong></div></div>
                <p>%s</p><p><strong>Observaciones:</strong> %s</p><div class="sign"><div>Representante del empleador</div><div>%s<br>Trabajador(a)</div></div></body></html>
                """.formatted(
                titulo, titulo, persona, documento, solicitud.getTipo().name(),
                FECHA.format(solicitud.getPeriodoInicio()), FECHA.format(solicitud.getPeriodoFin()),
                solicitud.getDiasSolicitados(), FECHA.format(solicitud.getFechaInicio()),
                FECHA.format(solicitud.getFechaFin()), FECHA.format(solicitud.getFechaRetorno()),
                sustento, esc(solicitud.getObservacion() == null ? "Sin observaciones" : solicitud.getObservacion()), persona);
    }

    private static String esc(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
