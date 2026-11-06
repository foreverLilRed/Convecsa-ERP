package com.convecsa.erp.contrato.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.contrato.catalogo.CategoriaContrato;
import com.convecsa.erp.contrato.catalogo.PlantillaCampoEsquema;
import com.convecsa.erp.contrato.catalogo.RegimenLaboral;
import com.convecsa.erp.contrato.entity.Adenda;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.entity.PlantillaContrato;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContratoPlantillaServiceTest {

    @Test
    void sustituyeDatosDePersonaYContrato() {
        Contrato contrato = contratoBase();
        contrato.getMetadata().put("cargo", "RESIDENTE DE OBRA");
        contrato.getMetadata().put("proyecto", "Obra Central");

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
        assertTrue(html.contains("CONTRATO DE TRABAJO A PLAZO FIJO POR OBRA DETERMINADA O SERVICIO ESPECÍFICO"));
        assertTrue(html.contains("EL TRABAJADOR"));
        assertTrue(html.contains("señor(a)"));
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
    void sustituyeNuevosDatosDePersonaContratoYVigencia() {
        Contrato contrato = contratoBase();
        contrato.setTitulo("Contrato de supervisión");
        contrato.setFechaCorte(LocalDate.of(2026, 6, 30));
        contrato.setObservaciones("Renovable por acuerdo de las partes");
        contrato.setActivo(true);
        contrato.getPersona().setCorreo("pedro@convecsa.pe");
        contrato.getPersona().setDistrito("San Borja");
        contrato.getPersona().setPension("AFP Integra");
        contrato.getPersona().setCuspp("123456789012");

        CategoriaContrato categoria = new CategoriaContrato();
        categoria.setCodigo("RG_PLAZO_FIJO_OBRA");
        categoria.setLabel("Plazo Fijo - Por Obra Determinada o Servicio Específico");
        RegimenLaboral regimen = new RegimenLaboral();
        regimen.setCodigo("REGIMEN_GENERAL");
        regimen.setLabel("Régimen General (D.L. 728)");
        categoria.setRegimenLaboral(regimen);
        contrato.setCategoriaContrato(categoria);

        PlantillaContrato plantilla = new PlantillaContrato();
        plantilla.setCodigo("CONTRATO_BASE");
        plantilla.setNombre("Contrato base");
        contrato.setTipoPlantilla(plantilla);

        String html = new ContratoPlantillaService().apply(
                "{{titulo}}|{{categoria_contrato}}|{{categoria_contrato_codigo}}|{{regimen_laboral}}|"
                        + "{{regimen_laboral_codigo}}|{{modalidad_contrato}}|{{plantilla_nombre}}|"
                        + "{{trabajador_nombre_completo}}|{{trabajador_correo}}|{{trabajador_distrito}}|"
                        + "{{trabajador_pension}}|{{trabajador_cuspp}}|{{fecha_corte}}|{{activo}}|{{observaciones}}",
                contrato);

        assertEquals(
                "Contrato de supervisión|Plazo Fijo - Por Obra Determinada o Servicio Específico|RG_PLAZO_FIJO_OBRA|"
                        + "Régimen General (D.L. 728)|REGIMEN_GENERAL|"
                        + "Plazo Fijo - Por Obra Determinada o Servicio Específico|Contrato base|PEDRO QUISPE HUAMAN|"
                        + "pedro@convecsa.pe|San Borja|AFP Integra|123456789012|30 de junio de 2026|Sí|"
                        + "Renovable por acuerdo de las partes",
                html);
    }

    @Test
    void renderUsaHtmlDeLaPlantillaEnBd() {
        Contrato contrato = contratoBase();
        contrato.getMetadata().put("cargo", "INGENIERO");
        PlantillaContrato plantilla = new PlantillaContrato();
        plantilla.setPlantilla("<p>{{trabajador_nombres}} — {{cargo}}</p>");
        contrato.setTipoPlantilla(plantilla);

        String html = new ContratoPlantillaService().render(contrato);

        assertTrue(html.contains("PEDRO"));
        assertTrue(html.contains("INGENIERO"));
        assertFalse(html.contains("{{cargo}}"));
    }

    @Test
    void esquemaDesdeHtmlExtraeCamposUnicosEnOrden() {
        List<PlantillaCampoEsquema> esquema = ContratoPlantillaService.esquemaDesdeHtml(
                "<p>{{fecha_inicio}} {{meta.campo}} y {{meta.ciudad_firma}} otra vez {{meta.campo}}</p>");

        assertEquals(2, esquema.size());
        assertEquals("campo", esquema.get(0).getClave());
        assertEquals("Campo", esquema.get(0).getLabel());
        assertEquals("text", esquema.get(0).getTipo());
        assertNull(esquema.get(0).getCatalogo());
        assertTrue(esquema.get(0).isRequerido());
        assertEquals("ciudad_firma", esquema.get(1).getClave());
        assertEquals("Ciudad firma", esquema.get(1).getLabel());
        assertEquals("text", esquema.get(1).getTipo());
        assertTrue(esquema.get(1).isRequerido());
    }

    @Test
    void renderAdendaSustituyeProrrogaYMotivo() {
        Contrato contrato = contratoBase();
        contrato.getMetadata().put("cargo", "RESIDENTE DE OBRA");
        contrato.getMetadata().put("proyecto", "Clínica");
        Adenda adenda = new Adenda();
        adenda.setContrato(contrato);
        adenda.setFechaFinAnterior(LocalDate.of(2026, 7, 15));
        adenda.setFechaFinNueva(LocalDate.of(2026, 10, 15));
        adenda.setSueldo(new BigDecimal("6500.00"));
        adenda.setMotivo("ampliación del plazo de ejecución de la obra principal");
        adenda.setCiudadEmision("Lima");
        adenda.setFechaEmision(LocalDate.of(2026, 7, 1));
        PlantillaContrato plantilla = new PlantillaContrato();
        plantilla.setPlantilla("De {{fecha_fin_anterior}} a {{fecha_fin_nueva}} ({{duracion_adenda}}). {{motivo}} {{cargo}}");
        adenda.setPlantillaContrato(plantilla);

        String html = new ContratoPlantillaService().render(adenda);

        assertTrue(html.contains("15 de julio de 2026"));
        assertTrue(html.contains("15 de octubre de 2026"));
        assertTrue(html.contains("3 meses"));
        assertTrue(html.contains("ampliación del plazo"));
        assertTrue(html.contains("RESIDENTE DE OBRA"));
        assertFalse(html.contains("{{motivo}}"));
    }

    @Test
    void esquemaDesdeHtmlSinTokensMetaQuedaVacio() {
        List<PlantillaCampoEsquema> esquema = ContratoPlantillaService.esquemaDesdeHtml(
                "<p>{{trabajador_nombres}} {{fecha_inicio}}</p>");

        assertTrue(esquema.isEmpty());
    }

    @Test
    void renderOnboardingUsaPersonaYDejaMetaDesconocidoVacio() {
        Persona persona = personaBase();
        persona.setDistrito("San Borja");
        persona.setCorreo("pedro@convecsa.pe");
        persona.setFechaNacimiento(java.time.LocalDate.of(1990, 5, 12));
        persona.setGradoInstruccion(com.convecsa.erp.persona.domain.GradoInstruccion.UNIVERSITARIO);

        String html = new ContratoPlantillaService().render(
                "{{trabajador_nombres}} {{trabajador_dni}} {{meta.apellido_paterno}} {{meta.correo}} {{meta.cuspp}}",
                persona,
                null);

        assertTrue(html.contains("PEDRO"));
        assertTrue(html.contains("73567960"));
        assertTrue(html.contains("QUISPE"));
        assertTrue(html.contains("pedro@convecsa.pe"));
        assertFalse(html.contains("{{"));
        assertTrue(html.endsWith(" "));
    }

    @Test
    void partirApellidosSeparaPaternoYMaterno() {
        String[] partes = ContratoPlantillaService.partirApellidos("QUISPE HUAMAN");
        assertEquals("QUISPE", partes[0]);
        assertEquals("HUAMAN", partes[1]);
    }

    private static Persona personaBase() {
        Persona persona = new Persona();
        persona.setNombres("PEDRO");
        persona.setApellidos("QUISPE HUAMAN");
        persona.setNumeroDocumento("73567960");
        persona.setDireccion("Av. Los Alamos 123");
        return persona;
    }

    private static Contrato contratoBase() {
        Contrato contrato = new Contrato();
        contrato.setPersona(personaBase());
        contrato.setFechaInicio(LocalDate.of(2026, 1, 15));
        contrato.setFechaFin(LocalDate.of(2026, 7, 15));
        contrato.setSueldo(new BigDecimal("6500.00"));
        return contrato;
    }
}
