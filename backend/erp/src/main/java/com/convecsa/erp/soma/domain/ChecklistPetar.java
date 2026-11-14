package com.convecsa.erp.soma.domain;

import java.util.List;
import java.util.Map;

public final class ChecklistPetar {

    private static final Map<String, String> ETIQUETAS = Map.ofEntries(
            Map.entry("eppCompleto", "EPP completo"),
            Map.entry("arnesCertificado", "Arnés certificado"),
            Map.entry("lineasVida", "Líneas de vida"),
            Map.entry("puntosAnclaje", "Puntos de anclaje"),
            Map.entry("areaDelimitada", "Área delimitada"),
            Map.entry("charlaAts", "Charla / ATS"),
            Map.entry("taludesEstables", "Taludes estables"),
            Map.entry("apuntalamiento", "Apuntalamiento"),
            Map.entry("accesoEgreso", "Acceso y egreso"),
            Map.entry("serviciosSubterraneos", "Servicios subterráneos identificados"),
            Map.entry("extintorCercano", "Extintor cercano"),
            Map.entry("areaLibreCombustibles", "Área libre de combustibles"),
            Map.entry("vigilanciaIncendio", "Vigilancia contra incendio"),
            Map.entry("vigiaExterior", "Vigía exterior"),
            Map.entry("ventilacion", "Ventilación"),
            Map.entry("rescateListo", "Rescate listo"),
            Map.entry("atmosferaMedida", "Atmósfera medida"),
            Map.entry("riggerAsignado", "Rigger asignado"),
            Map.entry("eslingasCertificadas", "Eslingas certificadas"),
            Map.entry("areaIzajeDelimitada", "Área de izaje delimitada"),
            Map.entry("bloqueoEtiquetado", "Bloqueo y etiquetado"),
            Map.entry("tensionVerificada", "Tensión verificada"),
            Map.entry("eppDielectrico", "EPP dieléctrico"));

    private ChecklistPetar() {}

    public static List<String> itemsPara(TipoTrabajoPetar tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de trabajo es obligatorio");
        }
        return switch (tipo) {
            case ALTURA -> List.of(
                    "eppCompleto", "arnesCertificado", "lineasVida", "puntosAnclaje", "areaDelimitada", "charlaAts");
            case EXCAVACION_ZANJA -> List.of(
                    "eppCompleto",
                    "taludesEstables",
                    "apuntalamiento",
                    "accesoEgreso",
                    "serviciosSubterraneos",
                    "charlaAts");
            case TRABAJO_CALIENTE -> List.of(
                    "eppCompleto", "extintorCercano", "areaLibreCombustibles", "vigilanciaIncendio", "charlaAts");
            case ESPACIO_CONFINADO -> List.of(
                    "eppCompleto", "vigiaExterior", "ventilacion", "rescateListo", "atmosferaMedida", "charlaAts");
            case IZAJE_CARGAS -> List.of(
                    "eppCompleto", "riggerAsignado", "eslingasCertificadas", "areaIzajeDelimitada", "charlaAts");
            case ENERGIA_ELECTRICA -> List.of(
                    "eppCompleto", "bloqueoEtiquetado", "tensionVerificada", "eppDielectrico", "charlaAts");
        };
    }

    public static String etiqueta(String clave) {
        if (clave == null) {
            return null;
        }
        return ETIQUETAS.getOrDefault(clave, clave);
    }
}
