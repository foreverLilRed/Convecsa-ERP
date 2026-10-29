package com.convecsa.erp.nomina.declaracion.support;

import com.convecsa.erp.construccion.domain.CodigoConceptoCc;
import com.convecsa.erp.construccion.domain.CodigoTareoCc;
import com.convecsa.erp.construccion.entity.BoletaCc;
import com.convecsa.erp.construccion.entity.BoletaCcConcepto;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConsolidadoCcCalculator {

    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    public List<ConsolidadoCalculo> calcular(List<BoletaCc> boletas, List<TareoCcDia> dias) {
        Map<Long, Acumulado> porPersona = new LinkedHashMap<>();
        for (BoletaCc boleta : boletas) {
            Persona persona = boleta.getPersona();
            Acumulado acc = porPersona.computeIfAbsent(persona.getId(), id -> new Acumulado(persona));
            acc.agregarBoleta(boleta);
        }
        for (TareoCcDia dia : dias) {
            Acumulado acc = porPersona.get(dia.getPersona().getId());
            if (acc != null) {
                acc.agregarTareo(dia);
            }
        }
        List<ConsolidadoCalculo> resultado = new ArrayList<>();
        for (Acumulado acc : porPersona.values()) {
            resultado.add(acc.toCalculo());
        }
        resultado.sort(Comparator.comparing((ConsolidadoCalculo c) -> c.getPersona().getApellidos())
                .thenComparing(c -> c.getPersona().getNombres())
                .thenComparing(c -> c.getPersona().getNumeroDocumento()));
        return resultado;
    }

    private static class Acumulado {
        private final Persona persona;
        private Proyecto proyecto;
        private LocalDate proyectoHasta;
        private BigDecimal totalHaberes = CERO;
        private BigDecimal totalDescuentos = CERO;
        private BigDecimal totalAportes = CERO;
        private BigDecimal totalAfecto = CERO;
        private BigDecimal neto = CERO;
        private BigDecimal horasOrd = CERO;
        private BigDecimal he60 = CERO;
        private BigDecimal he100 = CERO;
        private int diasLab;
        private int diasFalta;
        private int diasDm;
        private int diasFe;
        private int diasLic;
        private final Map<CodigoConceptoCc, ConceptoAcc> conceptos = new LinkedHashMap<>();

        private Acumulado(Persona persona) {
            this.persona = persona;
        }

        private void agregarBoleta(BoletaCc boleta) {
            totalHaberes = totalHaberes.add(n(boleta.getTotalHaberes()));
            totalDescuentos = totalDescuentos.add(n(boleta.getTotalDescuentos()));
            totalAportes = totalAportes.add(n(boleta.getTotalAportesEmpleador()));
            totalAfecto = totalAfecto.add(n(boleta.getTotalAfecto()));
            neto = neto.add(n(boleta.getNeto()));
            LocalDate hasta = boleta.getPeriodo() == null ? null : boleta.getPeriodo().getFechaHasta();
            Proyecto candidato = boleta.getPeriodo() == null ? null : boleta.getPeriodo().getProyecto();
            if (candidato != null && (proyecto == null || (hasta != null && (proyectoHasta == null || hasta.isAfter(proyectoHasta))))) {
                proyecto = candidato;
                proyectoHasta = hasta;
            }
            for (BoletaCcConcepto concepto : boleta.getConceptos()) {
                ConceptoAcc acc = conceptos.computeIfAbsent(concepto.getCodigo(), codigo ->
                        new ConceptoAcc(concepto.getCategoria(), codigo, concepto.getLabel(), concepto.getOrden()));
                acc.monto = acc.monto.add(n(concepto.getMonto()));
                acc.orden = Math.min(acc.orden, concepto.getOrden());
            }
        }

        private void agregarTareo(TareoCcDia dia) {
            horasOrd = horasOrd.add(n(dia.getHorasOrd()));
            he60 = he60.add(n(dia.getHe60()));
            he100 = he100.add(n(dia.getHe100()));
            CodigoTareoCc codigo = dia.getCodigo();
            if (codigo == CodigoTareoCc.LAB) {
                diasLab++;
            } else if (codigo == CodigoTareoCc.F) {
                diasFalta++;
            } else if (codigo == CodigoTareoCc.DM) {
                diasDm++;
            } else if (codigo == CodigoTareoCc.FE) {
                diasFe++;
            } else if (codigo == CodigoTareoCc.LIC) {
                diasLic++;
            }
        }

        private ConsolidadoCalculo toCalculo() {
            List<ConsolidadoCalculo.Concepto> lista = new ArrayList<>();
            conceptos.values().stream()
                    .filter(c -> c.monto.signum() != 0)
                    .sorted(Comparator.comparingInt(c -> c.orden))
                    .forEach(c -> lista.add(new ConsolidadoCalculo.Concepto(
                            c.categoria, c.codigo, c.label, c.monto.setScale(2, RoundingMode.HALF_UP), c.orden)));
            return new ConsolidadoCalculo(
                    persona,
                    proyecto,
                    totalHaberes.setScale(2, RoundingMode.HALF_UP),
                    totalDescuentos.setScale(2, RoundingMode.HALF_UP),
                    totalAportes.setScale(2, RoundingMode.HALF_UP),
                    totalAfecto.setScale(2, RoundingMode.HALF_UP),
                    neto.setScale(2, RoundingMode.HALF_UP),
                    horasOrd.setScale(2, RoundingMode.HALF_UP),
                    he60.setScale(2, RoundingMode.HALF_UP),
                    he100.setScale(2, RoundingMode.HALF_UP),
                    diasLab,
                    diasFalta,
                    diasDm,
                    diasFe,
                    diasLic,
                    lista);
        }
    }

    private static class ConceptoAcc {
        private final CategoriaConcepto categoria;
        private final CodigoConceptoCc codigo;
        private final String label;
        private BigDecimal monto = CERO;
        private int orden;

        private ConceptoAcc(CategoriaConcepto categoria, CodigoConceptoCc codigo, String label, int orden) {
            this.categoria = categoria;
            this.codigo = codigo;
            this.label = label;
            this.orden = orden;
        }
    }

    private static BigDecimal n(BigDecimal valor) {
        return valor == null ? CERO : valor;
    }
}
