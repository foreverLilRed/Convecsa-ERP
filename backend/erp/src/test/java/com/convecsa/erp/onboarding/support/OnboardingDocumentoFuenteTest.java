package com.convecsa.erp.onboarding.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.contrato.entity.Adenda;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.entity.PlantillaContrato;
import com.convecsa.erp.contrato.support.ContratoPlantillaService;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OnboardingDocumentoFuenteTest {

    private final ContratoPlantillaService renderer = new ContratoPlantillaService();

    @Test
    void doc01UsaPlantillaDelContratoYNoLaDeOnboarding() {
        Contrato contrato = contratoBase();
        PlantillaContrato plantilla = new PlantillaContrato();
        plantilla.setPlantilla("CONTRATO VIVO {{trabajador_nombres}} cargo {{cargo}}");
        contrato.setTipoPlantilla(plantilla);
        contrato.getMetadata().put("cargo", "OPERARIO");

        String html = OnboardingDocumentoFuente.htmlDeCodigo(
                "DOC-01",
                "ONBOARDING MUERTO {{trabajador_nombres}}",
                contrato.getPersona(),
                contrato,
                null,
                Map.of(),
                renderer);

        assertTrue(html.contains("CONTRATO VIVO"));
        assertTrue(html.contains("PEDRO"));
        assertTrue(html.contains("OPERARIO"));
        assertFalse(html.contains("ONBOARDING MUERTO"));
    }

    @Test
    void doc02ConAdendaUsaPlantillaDeLaAdenda() {
        Contrato contrato = contratoBase();
        Adenda adenda = new Adenda();
        adenda.setContrato(contrato);
        adenda.setFechaFinAnterior(LocalDate.of(2026, 7, 15));
        adenda.setFechaFinNueva(LocalDate.of(2026, 10, 15));
        adenda.setSueldo(new BigDecimal("6500.00"));
        adenda.setMotivo("prórroga de obra");
        PlantillaContrato plantilla = new PlantillaContrato();
        plantilla.setPlantilla("ADENDA VIVA {{motivo}} {{trabajador_nombres}}");
        adenda.setPlantillaContrato(plantilla);

        String html = OnboardingDocumentoFuente.htmlDeCodigo(
                "DOC-02",
                "ADENDA ONBOARDING {{trabajador_nombres}}",
                contrato.getPersona(),
                contrato,
                adenda,
                Map.of(),
                renderer);

        assertTrue(html.contains("ADENDA VIVA"));
        assertTrue(html.contains("prórroga de obra"));
        assertFalse(html.contains("ADENDA ONBOARDING"));
    }

    @Test
    void doc02SinAdendaUsaPlantillaDeOnboarding() {
        Contrato contrato = contratoBase();

        String html = OnboardingDocumentoFuente.htmlDeCodigo(
                "DOC-02",
                "ADENDA ONBOARDING {{trabajador_nombres}}",
                contrato.getPersona(),
                contrato,
                null,
                Map.of(),
                renderer);

        assertTrue(html.contains("ADENDA ONBOARDING"));
        assertTrue(html.contains("PEDRO"));
    }

    @Test
    void otroCodigoSiempreUsaPlantillaDeOnboarding() {
        Contrato contrato = contratoBase();
        PlantillaContrato plantilla = new PlantillaContrato();
        plantilla.setPlantilla("CONTRATO VIVO");
        contrato.setTipoPlantilla(plantilla);

        String html = OnboardingDocumentoFuente.htmlDeCodigo(
                "DOC-04",
                "DJ AFP {{trabajador_dni}}",
                contrato.getPersona(),
                contrato,
                null,
                Map.of(),
                renderer);

        assertTrue(html.contains("DJ AFP"));
        assertTrue(html.contains("73567960"));
        assertFalse(html.contains("CONTRATO VIVO"));
    }

    private static Contrato contratoBase() {
        Persona persona = new Persona();
        persona.setNombres("PEDRO");
        persona.setApellidos("QUISPE HUAMAN");
        persona.setNumeroDocumento("73567960");
        Contrato contrato = new Contrato();
        contrato.setPersona(persona);
        contrato.setFechaInicio(LocalDate.of(2026, 1, 15));
        contrato.setFechaFin(LocalDate.of(2026, 7, 15));
        contrato.setSueldo(new BigDecimal("6500.00"));
        return contrato;
    }
}
