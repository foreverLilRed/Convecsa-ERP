package com.convecsa.erp.contrato.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.contrato.catalogo.CausaObjetiva;
import com.convecsa.erp.contrato.catalogo.Jornada;
import com.convecsa.erp.contrato.catalogo.MetodoPago;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ContratoPlantillaServiceTest {

    @Test
    void sustituyeDatosDePersonaYContrato() {
        Persona persona = new Persona();
        persona.setNombres("PEDRO");
        persona.setApellidos("QUISPE HUAMAN");
        persona.setNumeroDocumento("73567960");
        persona.setDireccion("Av. Los Alamos 123");

        CausaObjetiva causa = new CausaObjetiva();
        causa.setLabel("OSC Instalación de Sellos Cortafuego y Descenso Vertical");
        Jornada jornada = new Jornada();
        jornada.setLabel("48 horas semanales");
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setLabel("Depósito bancario");

        Contrato contrato = new Contrato();
        contrato.setPersona(persona);
        contrato.setCargo("RESIDENTE DE OBRA");
        contrato.setArea("Operaciones");
        contrato.setCausaObjetiva(causa);
        contrato.setProyecto("Obra Central");
        contrato.setUbicacionObra("Lima");
        contrato.setFechaInicio(LocalDate.of(2026, 1, 15));
        contrato.setFechaFin(LocalDate.of(2026, 7, 15));
        contrato.setJornada(jornada);
        contrato.setSueldo(new BigDecimal("6500.00"));
        contrato.setMetodoPago(metodoPago);
        contrato.setCiudadFirma("Lima");

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
}
