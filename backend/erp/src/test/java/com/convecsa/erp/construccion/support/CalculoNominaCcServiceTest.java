package com.convecsa.erp.construccion.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.convecsa.erp.construccion.domain.CategoriaObrera;
import com.convecsa.erp.construccion.domain.CodigoConceptoCc;
import com.convecsa.erp.construccion.domain.SemestreGrati;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CalculoNominaCcServiceTest {

    private final CalculoNominaCcService calculo = new CalculoNominaCcService();

    @Test
    void casoOb001Planilla1MismosCentesimosQueExcel() {
        CcLiquidacionResult resultado = calculo.calcular(ob001(), parametros2026());

        assertEquals(new BigDecimal("89.30"), resultado.getTarifaDia());
        assertEquals(new BigDecimal("11.1625"), resultado.getTarifaHora());
        assertMonto(resultado, CodigoConceptoCc.JORNAL_BASICO, "2092.97");
        assertMonto(resultado, CodigoConceptoCc.DOMINICAL, "348.83");
        assertMonto(resultado, CodigoConceptoCc.BUC, "669.75");
        assertMonto(resultado, CodigoConceptoCc.MOVILIDAD, "206.40");
        assertMonto(resultado, CodigoConceptoCc.VACACIONES_10, "209.30");
        assertMonto(resultado, CodigoConceptoCc.GRATIFICACION, "408.23");
        assertMonto(resultado, CodigoConceptoCc.BONIF_LEY_29351, "36.74");
        assertMonto(resultado, CodigoConceptoCc.CTS_15, "313.95");
        assertMonto(resultado, CodigoConceptoCc.ESCOLARIDAD, "357.20");
        assertMonto(resultado, CodigoConceptoCc.BAE, "535.80");
        assertMonto(resultado, CodigoConceptoCc.ALTURA, "171.46");
        assertMonto(resultado, CodigoConceptoCc.ALTITUD, "60.00");
        assertMonto(resultado, CodigoConceptoCc.COTA_CERO, "45.60");
        assertMonto(resultado, CodigoConceptoCc.ALTAS_TEMP, "108.00");
        assertMonto(resultado, CodigoConceptoCc.CONTACTO_AGUA, "428.64");
        assertMonto(resultado, CodigoConceptoCc.AGUAS_SERVIDAS, "428.64");
        assertMonto(resultado, CodigoConceptoCc.ONP, "593.21");
        assertMonto(resultado, CodigoConceptoCc.CONAFOVICER, "48.84");
        assertMonto(resultado, CodigoConceptoCc.CUOTA_SINDICAL, "40.00");
        assertMonto(resultado, CodigoConceptoCc.RENTA_5TA, "15.00");
        assertMonto(resultado, CodigoConceptoCc.ESSALUD, "410.69");
        assertMonto(resultado, CodigoConceptoCc.FONDO_CAPACITACION, "9.42");
        assertEquals(new BigDecimal("6421.51"), resultado.getTotalHaberes());
        assertEquals(new BigDecimal("4563.19"), resultado.getTotalAfecto());
        assertEquals(new BigDecimal("697.05"), resultado.getTotalDescuentos());
        assertEquals(new BigDecimal("5724.46"), resultado.getNeto());
    }

    @Test
    void sctrDeLaObraEsAporteEmpleadorYNoCambiaElNeto() {
        CcParametros conSctr = parametros2026().toBuilder()
                .sctrSalud(new BigDecimal("0.010000"))
                .sctrPension(new BigDecimal("0.012000"))
                .build();
        CcLiquidacionResult sinSctr = calculo.calcular(ob001(), parametros2026());
        CcLiquidacionResult conAportes = calculo.calcular(ob001(), conSctr);

        assertMonto(conAportes, CodigoConceptoCc.SCTR_SALUD, "45.63");
        assertMonto(conAportes, CodigoConceptoCc.SCTR_PENSION, "54.76");
        assertEquals(sinSctr.getNeto(), conAportes.getNeto());
        assertEquals(0, sinSctr.getConceptos().stream()
                .filter(item -> item.codigo() == CodigoConceptoCc.SCTR_SALUD
                        || item.codigo() == CodigoConceptoCc.SCTR_PENSION)
                .count());
    }

    private static CcLiquidacionInput ob001() {
        return CcLiquidacionInput.builder()
                .categoria(CategoriaObrera.OPERARIO)
                .especialidadBae("SOLDADOR_6G")
                .hijosEscolaridad(2)
                .onp(true)
                .cuotaSindical(new BigDecimal("40.00"))
                .rentaQuinta(new BigDecimal("15.00"))
                .horasOrdinarias(new BigDecimal("187.5"))
                .horasDominical(new BigDecimal("31.25"))
                .he60(BigDecimal.ZERO)
                .he100(BigDecimal.ZERO)
                .diasEfectivos(24)
                .faltas(1)
                .diasMovilidad(24)
                .diasBae(24)
                .pisos(5)
                .diasAltura(24)
                .diasAltitud(24)
                .diasCotaCero(24)
                .diasAltasTemp(24)
                .diasContactoAgua(24)
                .diasAguasServidas(24)
                .diasValidacionGrati(24)
                .semestreGrati(SemestreGrati.FIESTAS_PATRIAS)
                .build();
    }

    static CcParametros parametros2026() {
        Map<CategoriaObrera, BigDecimal> jornales = new EnumMap<>(CategoriaObrera.class);
        jornales.put(CategoriaObrera.OPERARIO, new BigDecimal("89.30"));
        jornales.put(CategoriaObrera.OFICIAL, new BigDecimal("69.75"));
        jornales.put(CategoriaObrera.PEON, new BigDecimal("62.80"));
        Map<CategoriaObrera, BigDecimal> buc = new EnumMap<>(CategoriaObrera.class);
        buc.put(CategoriaObrera.OPERARIO, new BigDecimal("0.32"));
        buc.put(CategoriaObrera.OFICIAL, new BigDecimal("0.30"));
        buc.put(CategoriaObrera.PEON, new BigDecimal("0.30"));
        return CcParametros.builder()
                .horasDia(new BigDecimal("8"))
                .movilidadDia(new BigDecimal("8.60"))
                .movilidadFeriado(new BigDecimal("8.60"))
                .conafovicer(new BigDecimal("0.02"))
                .fondoCapacitacion(new BigDecimal("0.0045"))
                .essalud(new BigDecimal("0.09"))
                .vacaciones(new BigDecimal("0.10"))
                .cts(new BigDecimal("0.15"))
                .he60Factor(new BigDecimal("1.6"))
                .he100Factor(new BigDecimal("2.0"))
                .alturaTramo(new BigDecimal("0.08"))
                .altitudDia(new BigDecimal("2.50"))
                .cotaCeroDia(new BigDecimal("1.90"))
                .altasTempDia(new BigDecimal("4.50"))
                .nocturno(new BigDecimal("0.25"))
                .contactoAgua(new BigDecimal("0.20"))
                .aguasServidas(new BigDecimal("0.20"))
                .bonifLey29351(new BigDecimal("0.09"))
                .gratiJornales(40)
                .gratiMesesFp(7)
                .gratiMesesNav(5)
                .escolaridadJornales(30)
                .jornales(jornales)
                .buc(buc)
                .bae(Map.of(
                        "EQUIPO_MEDIANO", new BigDecimal("0.08"),
                        "EQUIPO_PESADO", new BigDecimal("0.10"),
                        "ELECTROMECANICO", new BigDecimal("0.22"),
                        "TOPOGRAFO", new BigDecimal("0.09"),
                        "SOLDADOR_6G", new BigDecimal("0.25")))
                .build();
    }

    private static void assertMonto(CcLiquidacionResult resultado, CodigoConceptoCc codigo, String esperado) {
        BigDecimal monto = resultado.getConceptos().stream()
                .filter(item -> item.codigo() == codigo)
                .map(CcConceptoCalculado::monto)
                .findFirst()
                .orElse(BigDecimal.ZERO);
        assertEquals(new BigDecimal(esperado), monto, codigo.name());
    }
}
