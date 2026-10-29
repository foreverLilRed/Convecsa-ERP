package com.convecsa.erp.construccion.support;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.convecsa.erp.construccion.domain.CodigoTareoCc;
import com.convecsa.erp.construccion.entity.BoletaCc;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class BoletaCcPlantillaServiceTest {

    @Test
    void incluyeTarifasYTareoDiario() {
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
        BoletaCc boleta = new BoletaCc();
        boleta.setPeriodo(periodo);
        boleta.setPersona(persona);
        boleta.setTotalHaberes(new BigDecimal("100.00"));
        boleta.setTotalDescuentos(BigDecimal.ZERO);
        boleta.setNeto(new BigDecimal("100.00"));

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

        assertTrue(html.contains("Tarifa día"));
        assertTrue(html.contains("89.30"));
        assertTrue(html.contains("11.16"));
        assertTrue(html.contains("06/04/2026"));
        assertTrue(html.contains("LAB"));
        assertTrue(html.contains("HE 60%"));
    }
}
