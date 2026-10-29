package com.convecsa.erp.construccion.support;

import com.convecsa.erp.construccion.domain.CategoriaObrera;
import com.convecsa.erp.construccion.domain.SemestreGrati;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class CcLiquidacionInput {

    private final CategoriaObrera categoria;
    private final String especialidadBae;
    private final int hijosEscolaridad;
    private final boolean onp;
    private final BigDecimal comisionAfp;
    private final BigDecimal primaAfp;
    private final BigDecimal aporteAfp;
    private final BigDecimal cuotaSindical;
    private final BigDecimal rentaQuinta;
    private final BigDecimal horasOrdinarias;
    private final BigDecimal horasDominical;
    private final BigDecimal he60;
    private final BigDecimal he100;
    private final int diasEfectivos;
    private final int faltas;
    private final int feriados;
    private final int feriadoTrabajado;
    private final int descansoTrabajado;
    private final int diasMovilidad;
    private final int diasMovilidadFeriado;
    private final int diasBae;
    private final int pisos;
    private final int diasAltura;
    private final int diasAltitud;
    private final int diasCotaCero;
    private final int diasAltasTemp;
    private final int diasContactoAgua;
    private final int diasAguasServidas;
    private final int diasNocturno;
    private final int diasValidacionGrati;
    private final SemestreGrati semestreGrati;
    private final int diasDescansoMedico;
    private final int diasLicencia;
}
