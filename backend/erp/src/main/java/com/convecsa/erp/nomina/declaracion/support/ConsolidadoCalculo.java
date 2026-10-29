package com.convecsa.erp.nomina.declaracion.support;

import com.convecsa.erp.construccion.domain.CodigoConceptoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class ConsolidadoCalculo {

    private final Persona persona;
    private final Proyecto proyecto;
    private final BigDecimal totalHaberes;
    private final BigDecimal totalDescuentos;
    private final BigDecimal totalAportesEmpleador;
    private final BigDecimal totalAfecto;
    private final BigDecimal neto;
    private final BigDecimal horasOrd;
    private final BigDecimal he60;
    private final BigDecimal he100;
    private final int diasLab;
    private final int diasFalta;
    private final int diasDm;
    private final int diasFe;
    private final int diasLic;
    private final List<Concepto> conceptos;

    public ConsolidadoCalculo(
            Persona persona,
            Proyecto proyecto,
            BigDecimal totalHaberes,
            BigDecimal totalDescuentos,
            BigDecimal totalAportesEmpleador,
            BigDecimal totalAfecto,
            BigDecimal neto,
            BigDecimal horasOrd,
            BigDecimal he60,
            BigDecimal he100,
            int diasLab,
            int diasFalta,
            int diasDm,
            int diasFe,
            int diasLic,
            List<Concepto> conceptos) {
        this.persona = persona;
        this.proyecto = proyecto;
        this.totalHaberes = totalHaberes;
        this.totalDescuentos = totalDescuentos;
        this.totalAportesEmpleador = totalAportesEmpleador;
        this.totalAfecto = totalAfecto;
        this.neto = neto;
        this.horasOrd = horasOrd;
        this.he60 = he60;
        this.he100 = he100;
        this.diasLab = diasLab;
        this.diasFalta = diasFalta;
        this.diasDm = diasDm;
        this.diasFe = diasFe;
        this.diasLic = diasLic;
        this.conceptos = List.copyOf(conceptos);
    }

    @Getter
    public static class Concepto {
        private final CategoriaConcepto categoria;
        private final CodigoConceptoCc codigo;
        private final String label;
        private final BigDecimal monto;
        private final int orden;

        public Concepto(
                CategoriaConcepto categoria,
                CodigoConceptoCc codigo,
                String label,
                BigDecimal monto,
                int orden) {
            this.categoria = categoria;
            this.codigo = codigo;
            this.label = label;
            this.monto = monto;
            this.orden = orden;
        }
    }
}
