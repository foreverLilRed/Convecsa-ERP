package com.convecsa.erp.construccion.support;

import com.convecsa.erp.construccion.domain.CategoriaObrera;
import com.convecsa.erp.construccion.domain.CodigoConceptoCc;
import com.convecsa.erp.construccion.domain.SemestreGrati;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CalculoNominaCcService {

    private static final RoundingMode RM = RoundingMode.HALF_UP;

    public CcLiquidacionResult calcular(CcLiquidacionInput in, CcParametros p) {
        if (in == null || in.getCategoria() == null) {
            throw new IllegalArgumentException("La categoría de obrero es obligatoria");
        }
        BigDecimal jornal = p.getJornales().get(in.getCategoria());
        BigDecimal bucPct = p.getBuc().get(in.getCategoria());
        if (jornal == null || bucPct == null) {
            throw new IllegalArgumentException("No hay jornal parametrizado para " + in.getCategoria());
        }

        BigDecimal tarifaHora = jornal.divide(p.getHorasDia(), 4, RM);
        BigDecimal hOrd = nz(in.getHorasOrdinarias());
        BigDecimal hDom = nz(in.getHorasDominical());
        BigDecimal he60h = nz(in.getHe60());
        BigDecimal he100h = nz(in.getHe100());

        BigDecimal remFeriado = r2(n(in.getFeriados()).multiply(jornal));
        BigDecimal feriadoTrab = r2(n(in.getFeriadoTrabajado()).multiply(tarifaHora).multiply(new BigDecimal("2")));
        BigDecimal descansoTrab = r2(n(in.getDescansoTrabajado()).multiply(tarifaHora).multiply(new BigDecimal("2")));
        BigDecimal jornalBasico = r2(hOrd.multiply(tarifaHora));
        BigDecimal dominical = r2(hDom.multiply(tarifaHora));
        BigDecimal baseBuc = n(in.getFeriadoTrabajado()).add(n(in.getDescansoTrabajado())).multiply(tarifaHora)
                .add(jornalBasico);
        BigDecimal buc = r2(bucPct.multiply(baseBuc));
        BigDecimal movilidad = r2(p.getMovilidadDia().multiply(n(in.getDiasMovilidad()))
                .add(p.getMovilidadFeriado().multiply(n(in.getDiasMovilidadFeriado()))));
        BigDecimal dm = r2(n(in.getDiasDescansoMedico()).multiply(jornal));
        BigDecimal lic = r2(n(in.getDiasLicencia()).multiply(jornal));
        BigDecimal baseBen = remFeriado.add(jornalBasico).add(dm).add(lic);
        BigDecimal vacaciones = r2(p.getVacaciones().multiply(baseBen));
        BigDecimal cts = r2(p.getCts().multiply(baseBen));
        int mesesGrati = in.getSemestreGrati() == SemestreGrati.NAVIDAD ? p.getGratiMesesNav() : p.getGratiMesesFp();
        BigDecimal gratiDia = jornal.multiply(BigDecimal.valueOf(p.getGratiJornales()))
                .divide(BigDecimal.valueOf(mesesGrati), 10, RM)
                .divide(BigDecimal.valueOf(30), 10, RM);
        BigDecimal grati = r2(gratiDia.multiply(n(in.getDiasValidacionGrati())));
        BigDecimal bonif29351 = r2(grati.multiply(p.getBonifLey29351()));
        BigDecimal escolaridad = r2(jornal.multiply(BigDecimal.valueOf(p.getEscolaridadJornales()))
                .divide(BigDecimal.valueOf(360), 10, RM)
                .multiply(n(in.getHijosEscolaridad()))
                .multiply(n(in.getDiasValidacionGrati())));
        BigDecimal baeDia = BigDecimal.ZERO;
        if (in.getCategoria() == CategoriaObrera.OPERARIO && in.getEspecialidadBae() != null) {
            BigDecimal tasa = p.getBae().getOrDefault(in.getEspecialidadBae(), BigDecimal.ZERO);
            baeDia = jornal.multiply(tasa);
        }
        BigDecimal bae = r2(baeDia.multiply(n(in.getDiasBae())));
        BigDecimal alturaPct = alturaPorPisos(in.getPisos(), p.getAlturaTramo());
        BigDecimal altura = r2(jornal.multiply(alturaPct).multiply(n(in.getDiasAltura())));
        BigDecimal altitud = r2(p.getAltitudDia().multiply(n(in.getDiasAltitud())));
        BigDecimal cota = r2(p.getCotaCeroDia().multiply(n(in.getDiasCotaCero())));
        BigDecimal altasTemp = r2(p.getAltasTempDia().multiply(n(in.getDiasAltasTemp())));
        BigDecimal agua = r2(p.getContactoAgua().multiply(jornal).multiply(n(in.getDiasContactoAgua())));
        BigDecimal aguasServ = r2(p.getAguasServidas().multiply(jornal).multiply(n(in.getDiasAguasServidas())));
        BigDecimal nocturno = r2(p.getNocturno().multiply(jornal)
                .divide(p.getHorasDia(), 10, RM)
                .multiply(new BigDecimal("7"))
                .multiply(n(in.getDiasNocturno())));
        BigDecimal he60 = r2(tarifaHora.multiply(p.getHe60Factor()).multiply(he60h));
        BigDecimal he100 = r2(tarifaHora.multiply(p.getHe100Factor()).multiply(he100h));
        BigDecimal indemnHe = r2(p.getCts().multiply(he60h.add(he100h)).multiply(tarifaHora));

        List<CcConceptoCalculado> haberes = new ArrayList<>();
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.FERIA_TRABAJADO, "Trabajo en feriado", feriadoTrab);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.DESCANSO_TRABAJADO, "Descanso trabajado", descansoTrab);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.REM_FERIADO, "Rem. feriado", remFeriado);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.JORNAL_BASICO, "Jornal básico", jornalBasico);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.DOMINICAL, "Dominical", dominical);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.BUC, "BUC", buc);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.MOVILIDAD, "Movilidad", movilidad);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.VACACIONES_10, "Vacaciones 10%", vacaciones);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.GRATIFICACION, "Gratificación", grati);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.BONIF_LEY_29351, "Bonif. Ley 29351", bonif29351);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.CTS_15, "CTS 15%", cts);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.ESCOLARIDAD, "Escolaridad", escolaridad);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.HE_60, "Horas extras 60%", he60);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.HE_100, "Horas extras 100%", he100);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.INDEMNIZACION_HE, "Indemnización HE 15%", indemnHe);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.BAE, "BAE", bae);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.ALTURA, "Altura", altura);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.ALTITUD, "Altitud", altitud);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.COTA_CERO, "Cota cero", cota);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.ALTAS_TEMP, "Altas temperaturas", altasTemp);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.CONTACTO_AGUA, "Contacto con agua", agua);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.AGUAS_SERVIDAS, "Aguas servidas", aguasServ);
        add(haberes, CategoriaConcepto.HABER, CodigoConceptoCc.NOCTURNO, "Nocturno", nocturno);

        BigDecimal totalHaberes = sum(haberes);
        BigDecimal totalAfecto = remFeriado.add(feriadoTrab).add(descansoTrab)
                .add(jornalBasico).add(dominical).add(buc)
                .add(dm).add(lic).add(vacaciones).add(he60).add(he100)
                .add(altura).add(altitud).add(cota).add(altasTemp).add(agua).add(aguasServ).add(nocturno);

        List<CcConceptoCalculado> desc = new ArrayList<>();
        if (in.isOnp()) {
            add(desc, CategoriaConcepto.DESCUENTO, CodigoConceptoCc.ONP, "ONP 13%", r2(totalAfecto.multiply(new BigDecimal("0.13"))));
        } else {
            add(desc, CategoriaConcepto.DESCUENTO, CodigoConceptoCc.COMISION_AFP, "Comisión AFP", r2(totalAfecto.multiply(nz(in.getComisionAfp()))));
            add(desc, CategoriaConcepto.DESCUENTO, CodigoConceptoCc.PRIMA_AFP, "Prima AFP", r2(totalAfecto.multiply(nz(in.getPrimaAfp()))));
            add(desc, CategoriaConcepto.DESCUENTO, CodigoConceptoCc.APORTE_AFP, "Aporte AFP", r2(totalAfecto.multiply(nz(in.getAporteAfp()))));
        }
        add(desc, CategoriaConcepto.DESCUENTO, CodigoConceptoCc.CONAFOVICER, "CONAFOVICER 2%",
                r2(remFeriado.add(jornalBasico).add(dominical).multiply(p.getConafovicer())));
        add(desc, CategoriaConcepto.DESCUENTO, CodigoConceptoCc.RENTA_5TA, "Renta 5ta", r2(nz(in.getRentaQuinta())));
        add(desc, CategoriaConcepto.DESCUENTO, CodigoConceptoCc.CUOTA_SINDICAL, "Cuota sindical", r2(nz(in.getCuotaSindical())));
        BigDecimal totalDesc = sum(desc);

        List<CcConceptoCalculado> aportes = new ArrayList<>();
        add(aportes, CategoriaConcepto.APORTE_EMPLEADOR, CodigoConceptoCc.ESSALUD, "EsSalud",
                r2(totalAfecto.multiply(p.getEssalud())));
        add(aportes, CategoriaConcepto.APORTE_EMPLEADOR, CodigoConceptoCc.FONDO_CAPACITACION, "Fondo capacitación",
                r2(p.getFondoCapacitacion().multiply(remFeriado.add(jornalBasico).add(dm).add(lic))));

        List<CcConceptoCalculado> todos = new ArrayList<>();
        todos.addAll(haberes);
        todos.addAll(desc);
        todos.addAll(aportes);

        return CcLiquidacionResult.builder()
                .tarifaDia(jornal)
                .tarifaHora(tarifaHora)
                .totalHaberes(totalHaberes)
                .totalAfecto(totalAfecto)
                .totalDescuentos(totalDesc)
                .totalAportes(sum(aportes))
                .neto(r2(totalHaberes.subtract(totalDesc)))
                .conceptos(todos)
                .build();
    }

    static BigDecimal alturaPorPisos(int pisos, BigDecimal tramo) {
        if (pisos < 4) {
            return BigDecimal.ZERO;
        }
        int bloques = Math.min(pisos, 35) / 4;
        return tramo.multiply(BigDecimal.valueOf(bloques));
    }

    private static void add(
            List<CcConceptoCalculado> lista,
            CategoriaConcepto cat,
            CodigoConceptoCc codigo,
            String label,
            BigDecimal monto) {
        if (monto.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        lista.add(new CcConceptoCalculado(cat, codigo, label, monto));
    }

    private static BigDecimal sum(List<CcConceptoCalculado> lista) {
        return lista.stream().map(CcConceptoCalculado::monto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal r2(BigDecimal v) {
        return v.setScale(2, RM);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal n(int v) {
        return BigDecimal.valueOf(v);
    }
}
