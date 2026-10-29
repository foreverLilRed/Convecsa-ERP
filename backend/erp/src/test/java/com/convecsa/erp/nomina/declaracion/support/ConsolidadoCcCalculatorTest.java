package com.convecsa.erp.nomina.declaracion.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.convecsa.erp.construccion.domain.CodigoConceptoCc;
import com.convecsa.erp.construccion.domain.CodigoTareoCc;
import com.convecsa.erp.construccion.entity.BoletaCc;
import com.convecsa.erp.construccion.entity.BoletaCcConcepto;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConsolidadoCcCalculatorTest {

    private final ConsolidadoCcCalculator calculator = new ConsolidadoCcCalculator();

    @Test
    void casoOb001MismosTotalesMensualesQueExcel() {
        Persona persona = persona();
        BoletaCc boleta = boletaOb001(persona);
        List<TareoCcDia> tareo = tareoOb001(persona, boleta.getPeriodo());

        List<ConsolidadoCalculo> resultado = calculator.calcular(List.of(boleta), tareo);
        assertEquals(1, resultado.size());
        ConsolidadoCalculo fila = resultado.get(0);

        assertEquals(new BigDecimal("187.50"), fila.getHorasOrd());
        assertEquals(24, fila.getDiasLab());
        assertEquals(1, fila.getDiasFalta());
        assertEquals(new BigDecimal("2092.97"), monto(fila, CodigoConceptoCc.JORNAL_BASICO));
        assertEquals(new BigDecimal("348.83"), monto(fila, CodigoConceptoCc.DOMINICAL));
        assertEquals(new BigDecimal("669.75"), monto(fila, CodigoConceptoCc.BUC));
        assertEquals(new BigDecimal("6421.51"), fila.getTotalHaberes());
        assertEquals(new BigDecimal("5724.46"), fila.getNeto());
        assertEquals("OB001", fila.getProyecto().getCodigo());
    }

    private static BigDecimal monto(ConsolidadoCalculo fila, CodigoConceptoCc codigo) {
        return fila.getConceptos().stream()
                .filter(c -> c.getCodigo() == codigo)
                .map(ConsolidadoCalculo.Concepto::getMonto)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    static Persona persona() {
        Persona persona = new Persona();
        persona.setId(1L);
        persona.setApellidos("QUISPE HUAMAN");
        persona.setNombres("JUAN CARLOS");
        persona.setTipoDocumento(TipoDocumento.DNI);
        persona.setNumeroDocumento("71118318");
        return persona;
    }

    static BoletaCc boletaOb001(Persona persona) {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(1L);
        proyecto.setCodigo("OB001");
        proyecto.setNombre("Obra demo");
        proyecto.setCentroCosto("MANO DE OBRA");

        PeriodoCc periodo = new PeriodoCc();
        periodo.setId(10L);
        periodo.setProyecto(proyecto);
        periodo.setFechaDesde(LocalDate.of(2026, 4, 1));
        periodo.setFechaHasta(LocalDate.of(2026, 4, 30));

        BoletaCc boleta = new BoletaCc();
        boleta.setPersona(persona);
        boleta.setPeriodo(periodo);
        boleta.setTotalHaberes(new BigDecimal("6421.51"));
        boleta.setTotalDescuentos(new BigDecimal("697.05"));
        boleta.setTotalAportesEmpleador(new BigDecimal("420.11"));
        boleta.setTotalAfecto(new BigDecimal("4563.19"));
        boleta.setNeto(new BigDecimal("5724.46"));

        List<BoletaCcConcepto> conceptos = new ArrayList<>();
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.JORNAL_BASICO, "2092.97");
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.DOMINICAL, "348.83");
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.BUC, "669.75");
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.MOVILIDAD, "206.40");
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.VACACIONES_10, "209.30");
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.GRATIFICACION, "408.23");
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.BONIF_LEY_29351, "36.74");
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.CTS_15, "313.95");
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.ESCOLARIDAD, "357.20");
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.BAE, "535.80");
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.ALTURA, "171.46");
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.ALTITUD, "60.00");
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.COTA_CERO, "45.60");
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.ALTAS_TEMP, "108.00");
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.CONTACTO_AGUA, "428.64");
        agregar(conceptos, CategoriaConcepto.HABER, CodigoConceptoCc.AGUAS_SERVIDAS, "428.64");
        agregar(conceptos, CategoriaConcepto.DESCUENTO, CodigoConceptoCc.ONP, "593.21");
        agregar(conceptos, CategoriaConcepto.DESCUENTO, CodigoConceptoCc.CONAFOVICER, "48.84");
        agregar(conceptos, CategoriaConcepto.DESCUENTO, CodigoConceptoCc.CUOTA_SINDICAL, "40.00");
        agregar(conceptos, CategoriaConcepto.DESCUENTO, CodigoConceptoCc.RENTA_5TA, "15.00");
        agregar(conceptos, CategoriaConcepto.APORTE_EMPLEADOR, CodigoConceptoCc.ESSALUD, "410.69");
        agregar(conceptos, CategoriaConcepto.APORTE_EMPLEADOR, CodigoConceptoCc.FONDO_CAPACITACION, "9.42");
        boleta.reemplazarConceptos(conceptos);
        return boleta;
    }

    private static void agregar(
            List<BoletaCcConcepto> conceptos,
            CategoriaConcepto categoria,
            CodigoConceptoCc codigo,
            String monto) {
        BoletaCcConcepto concepto = new BoletaCcConcepto();
        concepto.setCategoria(categoria);
        concepto.setCodigo(codigo);
        concepto.setLabel(codigo.name());
        concepto.setMonto(new BigDecimal(monto));
        conceptos.add(concepto);
    }

    static List<TareoCcDia> tareoOb001(Persona persona, PeriodoCc periodo) {
        List<TareoCcDia> dias = new ArrayList<>();
        LocalDate fecha = LocalDate.of(2026, 4, 1);
        for (int i = 0; i < 24; i++) {
            TareoCcDia dia = new TareoCcDia();
            dia.setPersona(persona);
            dia.setPeriodo(periodo);
            dia.setFecha(fecha.plusDays(i));
            dia.setCodigo(CodigoTareoCc.LAB);
            dia.setHorasOrd(new BigDecimal("7.8125"));
            dias.add(dia);
        }
        TareoCcDia falta = new TareoCcDia();
        falta.setPersona(persona);
        falta.setPeriodo(periodo);
        falta.setFecha(LocalDate.of(2026, 4, 25));
        falta.setCodigo(CodigoTareoCc.F);
        dias.add(falta);
        return dias;
    }
}
