package com.convecsa.erp.nomina.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.contrato.catalogo.CategoriaContrato;
import com.convecsa.erp.contrato.catalogo.RegimenLaboral;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.nomina.catalogo.Afp;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.domain.CodigoConcepto;
import com.convecsa.erp.nomina.domain.OrigenConcepto;
import com.convecsa.erp.nomina.entity.BoletaConcepto;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.nomina.entity.PeriodoNomina;
import com.convecsa.erp.nomina.entity.PlantillaBoleta;
import com.convecsa.erp.nomina.support.BoletaDualFilas.Celda;
import com.convecsa.erp.persona.entity.Persona;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class BoletaPlantillaServiceTest {

    private final BoletaPlantillaService plantillaService = new BoletaPlantillaService();

    @Test
    void renderizaHaberesYNetoEnLayoutDual() {
        BoletaPago boleta = boletaBase();
        boleta.getPersona().setCuspp("123456789012");
        agregar(boleta, CategoriaConcepto.HABER, CodigoConcepto.SUELDO_BASICO, "Sueldo básico", "1500.00");
        agregar(boleta, CategoriaConcepto.DESCUENTO, CodigoConcepto.APORTE_ONP, "Aporte ONP", "195.00");
        agregar(boleta, CategoriaConcepto.APORTE_EMPLEADOR, CodigoConcepto.ESSALUD, "EsSalud", "135.00");
        boleta.setTotalHaberes(new BigDecimal("1500.00"));
        boleta.setTotalDescuentos(new BigDecimal("195.00"));
        boleta.setTotalAportesEmpleador(new BigDecimal("135.00"));
        boleta.setNeto(new BigDecimal("1305.00"));

        String html = plantillaService.render(boleta, plantillaDual());

        assertTrue(html.contains("Copia Empleador"));
        assertTrue(html.contains("Copia Trabajador"));
        assertTrue(html.contains("NETO A PAGAR"));
        assertTrue(html.contains("Sueldo básico"));
        assertTrue(html.contains("Aporte ONP"));
        assertTrue(html.contains("EsSalud"));
        assertTrue(html.contains("1,500.00"));
        assertTrue(html.contains("1,305.00"));
        assertTrue(html.contains("CUSSP:"));
        assertTrue(html.contains("123456789012"));
        assertTrue(html.contains("OPERARIO"));
        assertFalse(html.contains("{{filas_conceptos}}"));
        assertFalse(html.contains("{{neto}}"));
        assertFalse(html.contains("{{ficha_col1}}"));
        assertFalse(html.contains("SENATI"));
        assertFalse(html.contains("Fec. Cese:"));
        assertFalse(html.contains("Código:"));
        assertFalse(html.contains("Fec. Inicio Vac"));
    }

    @Test
    void fecCeseSoloSiElContratoTermino() {
        BoletaPago boleta = boletaBase();
        boleta.getContrato().setFechaFin(LocalDate.of(2026, 9, 30));
        boleta.getContrato().setActivo(false);

        String html = plantillaService.render(boleta, plantillaDual());

        assertTrue(html.contains("Fec. Cese:"));
        assertTrue(html.contains("30/09/2026"));
    }

    @Test
    void regimenUsaElDelContrato() {
        BoletaPago boleta = boletaBase();
        RegimenLaboral regimen = new RegimenLaboral();
        regimen.setCodigo("REGIMEN_GENERAL");
        regimen.setLabel("Régimen General (D.L. 728)");
        CategoriaContrato categoria = new CategoriaContrato();
        categoria.setRegimenLaboral(regimen);
        boleta.getContrato().setCategoriaContrato(categoria);

        assertEquals("Régimen General (D.L. 728)", plantillaService.tokens(boleta).get("regimen"));
    }

    @Test
    void zippeaConceptosYDejaCeldasVacias() {
        String filas = BoletaDualFilas.zippear(
                List.of(new Celda("Sueldo básico", "1,500.00"), new Celda("Asignación familiar", "113.00")),
                List.of(new Celda("Aporte ONP", "195.00")),
                List.of());

        assertTrue(filas.contains("Sueldo básico"));
        assertTrue(filas.contains("Asignación familiar"));
        assertTrue(filas.contains("Aporte ONP"));
        assertFalse(filas.contains("SENATI"));
        assertEquals(2, filas.split("<tr>", -1).length - 1);
    }

    @Test
    void tablaVaciaUsaFilaPlaceholderEnPlantillaAntigua() {
        BoletaPago boleta = boletaBase();
        String filas = BoletaPlantillaService.filas(boleta, CategoriaConcepto.HABER);
        assertTrue(filas.contains("0.00"));
        assertTrue(filas.contains("-"));
    }

    private static PlantillaBoleta plantillaDual() {
        PlantillaBoleta plantilla = new PlantillaBoleta();
        plantilla.setPlantilla(leerPlantillaDual());
        return plantilla;
    }

    private static String leerPlantillaDual() {
        try (InputStream input = BoletaPlantillaServiceTest.class.getClassLoader()
                .getResourceAsStream("templates/nominas/boleta-dual-a4.html")) {
            if (input == null) {
                throw new IllegalStateException("No se encontró la plantilla dual de boleta");
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static BoletaPago boletaBase() {
        Persona persona = new Persona();
        persona.setNombres("PEDRO");
        persona.setApellidos("ROJAS");
        persona.setNumeroDocumento("8644232");
        Contrato contrato = new Contrato();
        contrato.getMetadata().put("cargo", "OPERARIO");
        contrato.setFechaInicio(LocalDate.of(2024, 3, 1));
        contrato.setSueldo(new BigDecimal("1500.00"));
        contrato.setActivo(true);
        PeriodoNomina periodo = new PeriodoNomina();
        periodo.setAnio(2026);
        periodo.setMes(9);
        Afp afp = new Afp();
        afp.setLabel("ONP");
        afp.setEsOnp(true);
        BoletaPago boleta = new BoletaPago();
        boleta.setPersona(persona);
        boleta.setContrato(contrato);
        boleta.setPeriodo(periodo);
        boleta.setAfp(afp);
        boleta.setDiasTrabajados(30);
        return boleta;
    }

    private static void agregar(
            BoletaPago boleta,
            CategoriaConcepto categoria,
            CodigoConcepto codigo,
            String label,
            String monto) {
        BoletaConcepto concepto = new BoletaConcepto();
        concepto.setCategoria(categoria);
        concepto.setCodigo(codigo);
        concepto.setLabel(label);
        concepto.setMonto(new BigDecimal(monto));
        concepto.setOrden(boleta.getConceptos().size() + 1);
        concepto.setOrigen(OrigenConcepto.CALCULADO);
        boleta.addConcepto(concepto);
    }
}
