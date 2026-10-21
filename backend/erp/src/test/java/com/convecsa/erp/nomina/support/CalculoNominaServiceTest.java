package com.convecsa.erp.nomina.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.nomina.catalogo.Afp;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.domain.CodigoConcepto;
import com.convecsa.erp.nomina.domain.EstadoBoletaPago;
import com.convecsa.erp.nomina.domain.OrigenConcepto;
import com.convecsa.erp.nomina.entity.BoletaConcepto;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.nomina.entity.AfpTasa;
import com.convecsa.erp.nomina.entity.NominaConfiguracion;
import com.convecsa.erp.nomina.repository.AfpTasaRepository;
import com.convecsa.erp.nomina.repository.NominaConfiguracionRepository;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalculoNominaServiceTest {

    private final AfpTasaRepository tasas = mock(AfpTasaRepository.class);
    private final NominaConfiguracionRepository configuraciones = mock(NominaConfiguracionRepository.class);
    private final CalculoNominaService calculo = new CalculoNominaService(tasas, configuraciones);

    @BeforeEach
    void prepararConfiguracion() {
        NominaConfiguracion configuracion = new NominaConfiguracion();
        configuracion.setTasaEssalud(new BigDecimal("0.09"));
        configuracion.setTasaSctrSalud(BigDecimal.ZERO);
        configuracion.setTasaSctrPension(BigDecimal.ZERO);
        configuracion.setTasaVidaLey(BigDecimal.ZERO);
        configuracion.setTasaEssaludVida(BigDecimal.ZERO);
        when(configuraciones.findById(NominaConfiguracion.ID_UNICO)).thenReturn(Optional.of(configuracion));
        when(tasas.findByAfpIdAndTipoComisionAndActivoTrue(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> Optional.of(tasa(
                        invocation.getArgument(1).toString().equals("NO_APLICA") ? new BigDecimal("0.13") : new BigDecimal("0.10"),
                        invocation.getArgument(1).toString().equals("FLUJO") ? new BigDecimal("0.0147") : BigDecimal.ZERO,
                        invocation.getArgument(1).toString().equals("NO_APLICA") ? BigDecimal.ZERO : new BigDecimal("0.0137"))));
    }

    @Test
    void sueldo1500HabitatSinAsignacion() {
        BoletaPago boleta = boleta(new BigDecimal("1500.00"), habitat(), 30, false);

        calculo.calcular(boleta);

        assertMonto(boleta, CodigoConcepto.SUELDO_BASICO, "1500.00");
        assertMonto(boleta, CodigoConcepto.APORTE_OBLIG, "150.00");
        assertMonto(boleta, CodigoConcepto.COMISION_AFP, "22.05");
        assertMonto(boleta, CodigoConcepto.PRIMA_SEGURO, "20.55");
        assertMonto(boleta, CodigoConcepto.ESSALUD, "135.00");
        assertEquals(new BigDecimal("1500.00"), boleta.getTotalHaberes());
        assertEquals(new BigDecimal("192.60"), boleta.getTotalDescuentos());
        assertEquals(new BigDecimal("135.00"), boleta.getTotalAportesEmpleador());
        assertEquals(new BigDecimal("1307.40"), boleta.getNeto());
        assertTrue(boleta.getConceptos().stream().noneMatch(c -> c.getCodigo() == CodigoConcepto.ASIG_FAMILIAR));
    }

    @Test
    void sueldo1500HabitatConAsignacion() {
        BoletaPago boleta = boleta(new BigDecimal("1500.00"), habitat(), 30, true);

        calculo.calcular(boleta);

        assertMonto(boleta, CodigoConcepto.ASIG_FAMILIAR, "113.00");
        assertMonto(boleta, CodigoConcepto.APORTE_OBLIG, "161.30");
        assertMonto(boleta, CodigoConcepto.COMISION_AFP, "23.71");
        assertMonto(boleta, CodigoConcepto.PRIMA_SEGURO, "22.10");
        assertMonto(boleta, CodigoConcepto.ESSALUD, "145.17");
        assertEquals(new BigDecimal("1613.00"), boleta.getTotalHaberes());
        assertEquals(new BigDecimal("207.11"), boleta.getTotalDescuentos());
        assertEquals(new BigDecimal("1405.89"), boleta.getNeto());
    }

    @Test
    void sueldo2000Onp() {
        BoletaPago boleta = boleta(new BigDecimal("2000.00"), onp(), 30, false);

        calculo.calcular(boleta);

        assertMonto(boleta, CodigoConcepto.APORTE_ONP, "260.00");
        assertMonto(boleta, CodigoConcepto.ESSALUD, "180.00");
        assertTrue(boleta.getConceptos().stream().noneMatch(c -> c.getCodigo() == CodigoConcepto.COMISION_AFP));
        assertEquals(new BigDecimal("260.00"), boleta.getTotalDescuentos());
        assertEquals(new BigDecimal("1740.00"), boleta.getNeto());
    }

    @Test
    void veinteDiasProrrateaSueldo() {
        BoletaPago boleta = boleta(new BigDecimal("1500.00"), habitat(), 20, false);

        calculo.calcular(boleta);

        assertMonto(boleta, CodigoConcepto.SUELDO_BASICO, "1000.00");
        assertMonto(boleta, CodigoConcepto.APORTE_OBLIG, "100.00");
        assertMonto(boleta, CodigoConcepto.COMISION_AFP, "14.70");
        assertMonto(boleta, CodigoConcepto.PRIMA_SEGURO, "13.70");
        assertEquals(new BigDecimal("871.60"), boleta.getNeto());
    }

    @Test
    void extraHaberEntraEnBaseYSePreserva() {
        BoletaPago boleta = boleta(new BigDecimal("1500.00"), habitat(), 30, false);
        BoletaConcepto extra = new BoletaConcepto();
        extra.setCategoria(CategoriaConcepto.HABER);
        extra.setCodigo(CodigoConcepto.EXTRA);
        extra.setLabel("Bonificación");
        extra.setMonto(new BigDecimal("100.00"));
        extra.setOrden(9);
        extra.setOrigen(OrigenConcepto.MANUAL);
        boleta.addConcepto(extra);

        calculo.calcular(boleta);

        assertEquals(1, boleta.getConceptos().stream().filter(c -> c.getOrigen() == OrigenConcepto.MANUAL).count());
        assertEquals(new BigDecimal("1600.00"), boleta.getTotalHaberes());
        assertMonto(boleta, CodigoConcepto.APORTE_OBLIG, "160.00");
    }

    @Test
    void conceptoDeTareoEntraEnBaseYSePreservaAlRecalcular() {
        BoletaPago boleta = boleta(new BigDecimal("1500.00"), habitat(), 30, false);
        BoletaConcepto extra = new BoletaConcepto();
        extra.setCategoria(CategoriaConcepto.HABER);
        extra.setCodigo(CodigoConcepto.EXTRA);
        extra.setLabel("Horas extras 25% (240 min)");
        extra.setMonto(new BigDecimal("31.25"));
        extra.setOrden(1);
        extra.setOrigen(OrigenConcepto.TAREO);
        boleta.addConcepto(extra);

        calculo.calcular(boleta);
        calculo.calcular(boleta);

        assertEquals(1, boleta.getConceptos().stream()
                .filter(c -> c.getOrigen() == OrigenConcepto.TAREO).count());
        assertEquals(new BigDecimal("1531.25"), boleta.getTotalHaberes());
    }

    private static BoletaPago boleta(BigDecimal sueldo, Afp afp, int dias, boolean asignacion) {
        Contrato contrato = new Contrato();
        contrato.setSueldo(sueldo);
        Persona persona = new Persona();
        BoletaPago boleta = new BoletaPago();
        boleta.setContrato(contrato);
        boleta.setPersona(persona);
        boleta.setAfp(afp);
        boleta.setDiasTrabajados(dias);
        boleta.setAplicaAsigFamiliar(asignacion);
        boleta.setEstado(EstadoBoletaPago.BORRADOR);
        return boleta;
    }

    private static Afp habitat() {
        Afp afp = new Afp();
        afp.setId(1L);
        afp.setCodigo("HABITAT");
        afp.setLabel("AFP Habitat");
        afp.setAporteObligatorio(new BigDecimal("0.1000"));
        afp.setComisionFlujo(new BigDecimal("0.0147"));
        afp.setPrimaSeguro(new BigDecimal("0.0137"));
        afp.setEsOnp(false);
        return afp;
    }

    private static Afp onp() {
        Afp afp = new Afp();
        afp.setId(2L);
        afp.setCodigo("ONP");
        afp.setLabel("ONP");
        afp.setAporteObligatorio(new BigDecimal("0.1300"));
        afp.setComisionFlujo(BigDecimal.ZERO);
        afp.setPrimaSeguro(BigDecimal.ZERO);
        afp.setEsOnp(true);
        return afp;
    }

    private static void assertMonto(BoletaPago boleta, CodigoConcepto codigo, String esperado) {
        BigDecimal monto = boleta.getConceptos().stream()
                .filter(concepto -> concepto.getCodigo() == codigo)
                .map(BoletaConcepto::getMonto)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Falta concepto " + codigo));
        assertEquals(new BigDecimal(esperado).setScale(2, RoundingMode.HALF_UP), monto);
    }

    private static AfpTasa tasa(BigDecimal aporte, BigDecimal comision, BigDecimal prima) {
        AfpTasa tasa = new AfpTasa();
        tasa.setAporteObligatorio(aporte);
        tasa.setComision(comision);
        tasa.setPrimaSeguro(prima);
        tasa.setActivo(true);
        return tasa;
    }
}
