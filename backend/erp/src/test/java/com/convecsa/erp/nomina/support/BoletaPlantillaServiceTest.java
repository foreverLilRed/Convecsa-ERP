package com.convecsa.erp.nomina.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.nomina.catalogo.Afp;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.domain.CodigoConcepto;
import com.convecsa.erp.nomina.domain.OrigenConcepto;
import com.convecsa.erp.nomina.entity.BoletaConcepto;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.nomina.entity.PeriodoNomina;
import com.convecsa.erp.nomina.entity.PlantillaBoleta;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BoletaPlantillaServiceTest {

    private final BoletaPlantillaService plantillaService = new BoletaPlantillaService();

    @Test
    void renderizaHaberesYNeto() {
        BoletaPago boleta = boletaBase();
        agregar(boleta, CategoriaConcepto.HABER, CodigoConcepto.SUELDO_BASICO, "Sueldo básico", "1500.00");
        agregar(boleta, CategoriaConcepto.DESCUENTO, CodigoConcepto.APORTE_ONP, "Aporte ONP", "195.00");
        boleta.setTotalHaberes(new BigDecimal("1500.00"));
        boleta.setTotalDescuentos(new BigDecimal("195.00"));
        boleta.setNeto(new BigDecimal("1305.00"));

        PlantillaBoleta plantilla = new PlantillaBoleta();
        plantilla.setPlantilla(
                "<p>{{persona_nombres}} {{persona_apellidos}}</p>"
                        + "<table>{{tabla_haberes}}</table>"
                        + "<p>Neto {{neto}}</p>");

        String html = plantillaService.render(boleta, plantilla);

        assertTrue(html.contains("PEDRO"));
        assertTrue(html.contains("ROJAS"));
        assertTrue(html.contains("Sueldo básico"));
        assertTrue(html.contains("1,500.00"));
        assertTrue(html.contains("1,305.00"));
        assertFalse(html.contains("{{tabla_haberes}}"));
        assertFalse(html.contains("{{neto}}"));
    }

    @Test
    void tablaVaciaUsaFilaPlaceholder() {
        BoletaPago boleta = boletaBase();
        String filas = BoletaPlantillaService.filas(boleta, CategoriaConcepto.HABER);
        assertTrue(filas.contains("0.00"));
        assertTrue(filas.contains("—"));
    }

    private static BoletaPago boletaBase() {
        Persona persona = new Persona();
        persona.setNombres("PEDRO");
        persona.setApellidos("ROJAS");
        persona.setNumeroDocumento("8644232");
        Contrato contrato = new Contrato();
        contrato.setCargo("OPERARIO");
        PeriodoNomina periodo = new PeriodoNomina();
        periodo.setAnio(2026);
        periodo.setMes(9);
        Afp afp = new Afp();
        afp.setLabel("ONP");
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
