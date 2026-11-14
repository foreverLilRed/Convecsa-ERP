package com.convecsa.erp.construccion.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.construccion.domain.CategoriaObrera;
import com.convecsa.erp.construccion.domain.CodigoTareoCc;
import com.convecsa.erp.construccion.entity.BoletaCc;
import com.convecsa.erp.construccion.entity.BoletaCcConcepto;
import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.domain.OrigenConcepto;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class BoletaCcPlantillaServiceTest {

    @Test
    void incluyeLayoutDualYHe60SiHayTareo() {
        BoletaCc boleta = boletaBase();
        agregar(boleta, CategoriaConcepto.HABER, "Jornal básico", "89.30");

        TareoCcDia dia = new TareoCcDia();
        dia.setFecha(LocalDate.of(2026, 4, 6));
        dia.setCodigo(CodigoTareoCc.LAB);
        dia.setHorasOrd(new BigDecimal("8.50"));
        dia.setHe60(new BigDecimal("2.00"));
        dia.setHe100(BigDecimal.ZERO);

        String html = new BoletaCcPlantillaService().render(
                boleta,
                null,
                List.of(dia),
                new BigDecimal("89.30"),
                new BigDecimal("11.16"));

        assertTrue(html.contains("Copia Empleador"));
        assertTrue(html.contains("Copia Trabajador"));
        assertTrue(html.contains("NETO A PAGAR"));
        assertTrue(html.contains("Básico:"));
        assertTrue(html.contains("89.30"));
        assertTrue(html.contains("H. Extra 60%:"));
        assertTrue(html.contains(">2<") || html.contains(">2</td>"));
        assertTrue(html.contains("Operario"));
        assertTrue(html.contains("Jornal básico"));
        assertFalse(html.contains("H. Extra 100%:"));
        assertFalse(html.contains("<th>Fecha</th>"));
        assertFalse(html.contains(">LAB<"));
        assertFalse(html.contains("{{filas_conceptos}}"));
        assertFalse(html.contains("SENATI"));
    }

    @Test
    void noPintaHeNiGrillaSiNoHayTareo() {
        BoletaCc boleta = boletaBase();
        String html = new BoletaCcPlantillaService().render(
                boleta,
                null,
                List.of(),
                new BigDecimal("89.30"),
                new BigDecimal("11.16"));

        assertTrue(html.contains("Copia Empleador"));
        assertFalse(html.contains("H. Extra 60%:"));
        assertFalse(html.contains("H. Extra 100%:"));
        assertFalse(html.contains("Días D Medico:"));
        assertFalse(html.contains("Hor. Ord.:"));
        assertFalse(html.contains("Días computable:"));
        assertFalse(html.contains("<th>Fecha</th>"));
    }

    private static BoletaCc boletaBase() {
        Proyecto proyecto = new Proyecto();
        proyecto.setCodigo("2003");
        proyecto.setNombre("OBRA DEMO");
        PeriodoCc periodo = new PeriodoCc();
        periodo.setProyecto(proyecto);
        periodo.setFechaDesde(LocalDate.of(2026, 4, 6));
        periodo.setFechaHasta(LocalDate.of(2026, 4, 12));
        Persona persona = new Persona();
        persona.setNombres("JUAN");
        persona.setApellidos("PEREZ");
        persona.setNumeroDocumento("71118318");
        persona.setPension("AFP Integra");
        Contrato contrato = new Contrato();
        contrato.setFechaInicio(LocalDate.of(2025, 1, 10));
        contrato.setActivo(true);
        ContratoCc contratoCc = new ContratoCc();
        contratoCc.setCategoria(CategoriaObrera.OPERARIO);
        contrato.setConstruccionCivil(contratoCc);
        BoletaCc boleta = new BoletaCc();
        boleta.setPeriodo(periodo);
        boleta.setPersona(persona);
        boleta.setContrato(contrato);
        boleta.setTotalHaberes(new BigDecimal("100.00"));
        boleta.setTotalDescuentos(BigDecimal.ZERO);
        boleta.setNeto(new BigDecimal("100.00"));
        return boleta;
    }

    private static void agregar(BoletaCc boleta, CategoriaConcepto categoria, String label, String monto) {
        BoletaCcConcepto concepto = new BoletaCcConcepto();
        concepto.setCategoria(categoria);
        concepto.setLabel(label);
        concepto.setMonto(new BigDecimal(monto));
        concepto.setOrden(boleta.getConceptos().size());
        concepto.setOrigen(OrigenConcepto.CALCULADO);
        boleta.getConceptos().add(concepto);
    }
}
