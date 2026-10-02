package com.convecsa.erp.contrato.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.entity.PlantillaContrato;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ContratoPlantillaServiceTest {

    @Test
    void sustituyeDatosDePersonaYContrato() {
        Contrato contrato = contratoBase();
        contrato.setCargo("RESIDENTE DE OBRA");
        contrato.setProyecto("Obra Central");

        String html = new ContratoPlantillaService().render(contrato);

        assertTrue(html.contains("PEDRO"));
        assertTrue(html.contains("QUISPE HUAMAN"));
        assertTrue(html.contains("73567960"));
        assertTrue(html.contains("15 de enero de 2026"));
        assertTrue(html.contains("15 de julio de 2026"));
        assertTrue(html.contains("6 meses"));
        assertTrue(html.contains("6,500.00"));
        assertTrue(html.contains("Seis mil quinientos soles con 00/100"));
        assertFalse(html.contains("{{trabajador_nombres}}"));
        assertFalse(html.contains("{{sueldo}}"));
    }

    @Test
    void sustituyeMetadataComoTokenFijoYMeta() {
        Contrato contrato = contratoBase();
        contrato.getMetadata().put("cargo", "RESIDENTE DE OBRA");
        contrato.getMetadata().put("ciudad_firma", "Arequipa");

        String html = new ContratoPlantillaService().apply(
                "Cargo {{cargo}} en {{meta.ciudad_firma}}. Extra: {{meta.inexistente}}.",
                contrato);

        assertTrue(html.contains("RESIDENTE DE OBRA"));
        assertTrue(html.contains("Arequipa"));
        assertFalse(html.contains("{{cargo}}"));
        assertFalse(html.contains("{{meta.ciudad_firma}}"));
        assertFalse(html.contains("{{meta.inexistente}}"));
        assertTrue(html.endsWith("Extra: ."));
    }

    @Test
    void renderUsaHtmlDeLaPlantillaEnBd() {
        Contrato contrato = contratoBase();
        contrato.getMetadata().put("cargo", "INGENIERO");
        PlantillaContrato plantilla = new PlantillaContrato();
        plantilla.setPlantilla("<p>{{trabajador_nombres}} — {{cargo}}</p>");
        contrato.setPlantillaContrato(plantilla);

        String html = new ContratoPlantillaService().render(contrato);

        assertTrue(html.contains("PEDRO"));
        assertTrue(html.contains("INGENIERO"));
        assertFalse(html.contains("{{cargo}}"));
    }

    private static Contrato contratoBase() {
        Persona persona = new Persona();
        persona.setNombres("PEDRO");
        persona.setApellidos("QUISPE HUAMAN");
        persona.setNumeroDocumento("73567960");
        persona.setDireccion("Av. Los Alamos 123");

        Contrato contrato = new Contrato();
        contrato.setPersona(persona);
        contrato.setFechaInicio(LocalDate.of(2026, 1, 15));
        contrato.setFechaFin(LocalDate.of(2026, 7, 15));
        contrato.setSueldo(new BigDecimal("6500.00"));
        return contrato;
    }
}
