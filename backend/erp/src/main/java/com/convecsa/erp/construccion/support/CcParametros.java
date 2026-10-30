package com.convecsa.erp.construccion.support;

import com.convecsa.erp.construccion.domain.CategoriaObrera;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class CcParametros {

    private final BigDecimal horasDia;
    private final BigDecimal movilidadDia;
    private final BigDecimal movilidadFeriado;
    private final BigDecimal conafovicer;
    private final BigDecimal fondoCapacitacion;
    private final BigDecimal essalud;
    private final BigDecimal sctrSalud;
    private final BigDecimal sctrPension;
    private final BigDecimal vacaciones;
    private final BigDecimal cts;
    private final BigDecimal he60Factor;
    private final BigDecimal he100Factor;
    private final BigDecimal alturaTramo;
    private final BigDecimal altitudDia;
    private final BigDecimal cotaCeroDia;
    private final BigDecimal altasTempDia;
    private final BigDecimal nocturno;
    private final BigDecimal contactoAgua;
    private final BigDecimal aguasServidas;
    private final BigDecimal bonifLey29351;
    private final int gratiJornales;
    private final int gratiMesesFp;
    private final int gratiMesesNav;
    private final int escolaridadJornales;
    private final Map<CategoriaObrera, BigDecimal> jornales;
    private final Map<CategoriaObrera, BigDecimal> buc;
    private final Map<String, BigDecimal> bae;
}
