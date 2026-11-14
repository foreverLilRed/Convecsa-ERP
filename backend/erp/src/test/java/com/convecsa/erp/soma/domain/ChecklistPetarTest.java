package com.convecsa.erp.soma.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ChecklistPetarTest {

    @Test
    void itemsParaCadaTipoTieneClavesFijas() {
        assertEquals(
                List.of(
                        "eppCompleto",
                        "arnesCertificado",
                        "lineasVida",
                        "puntosAnclaje",
                        "areaDelimitada",
                        "charlaAts"),
                ChecklistPetar.itemsPara(TipoTrabajoPetar.ALTURA));
        assertEquals(
                List.of(
                        "eppCompleto",
                        "taludesEstables",
                        "apuntalamiento",
                        "accesoEgreso",
                        "serviciosSubterraneos",
                        "charlaAts"),
                ChecklistPetar.itemsPara(TipoTrabajoPetar.EXCAVACION_ZANJA));
        assertEquals(
                List.of("eppCompleto", "extintorCercano", "areaLibreCombustibles", "vigilanciaIncendio", "charlaAts"),
                ChecklistPetar.itemsPara(TipoTrabajoPetar.TRABAJO_CALIENTE));
        assertEquals(
                List.of("eppCompleto", "vigiaExterior", "ventilacion", "rescateListo", "atmosferaMedida", "charlaAts"),
                ChecklistPetar.itemsPara(TipoTrabajoPetar.ESPACIO_CONFINADO));
        assertEquals(
                List.of("eppCompleto", "riggerAsignado", "eslingasCertificadas", "areaIzajeDelimitada", "charlaAts"),
                ChecklistPetar.itemsPara(TipoTrabajoPetar.IZAJE_CARGAS));
        assertEquals(
                List.of("eppCompleto", "bloqueoEtiquetado", "tensionVerificada", "eppDielectrico", "charlaAts"),
                ChecklistPetar.itemsPara(TipoTrabajoPetar.ENERGIA_ELECTRICA));
    }

    @Test
    void etiquetaEnEspanol() {
        assertEquals("EPP completo", ChecklistPetar.etiqueta("eppCompleto"));
        assertEquals("Charla / ATS", ChecklistPetar.etiqueta("charlaAts"));
        assertTrue(ChecklistPetar.etiqueta("claveDesconocida").equals("claveDesconocida"));
        assertThrows(IllegalArgumentException.class, () -> ChecklistPetar.itemsPara(null));
    }
}
