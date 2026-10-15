package com.convecsa.erp.nomina.support;

import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.nomina.catalogo.Afp;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.domain.CodigoConcepto;
import com.convecsa.erp.nomina.domain.OrigenConcepto;
import com.convecsa.erp.nomina.entity.BoletaConcepto;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.nomina.entity.AfpTasa;
import com.convecsa.erp.nomina.entity.NominaConfiguracion;
import com.convecsa.erp.nomina.domain.TipoComisionAfp;
import com.convecsa.erp.nomina.repository.AfpTasaRepository;
import com.convecsa.erp.nomina.repository.NominaConfiguracionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalculoNominaService {

    public static final BigDecimal ASIGNACION_FAMILIAR = new BigDecimal("113.00");
    public static final int DIAS_MES = 30;

    private final AfpTasaRepository afpTasaRepository;
    private final NominaConfiguracionRepository configuracionRepository;

    public void calcular(BoletaPago boleta) {
        if (boleta == null) {
            throw new IllegalArgumentException("La boleta es obligatoria");
        }
        Contrato contrato = boleta.getContrato();
        if (contrato == null || contrato.getSueldo() == null) {
            throw new IllegalArgumentException("La boleta no tiene un sueldo de contrato");
        }
        Afp afp = boleta.getAfp();
        if (afp == null) {
            throw new IllegalArgumentException("La boleta no tiene AFP u ONP");
        }
        int dias = boleta.getDiasTrabajados();
        if (dias < 0 || dias > DIAS_MES) {
            throw new IllegalArgumentException("Los días trabajados deben estar entre 0 y 30");
        }

        boleta.getConceptos().removeIf(concepto -> concepto.getOrigen() == OrigenConcepto.CALCULADO);

        BigDecimal sueldoProrrateado = contrato.getSueldo()
                .multiply(BigDecimal.valueOf(dias))
                .divide(BigDecimal.valueOf(DIAS_MES), 2, RoundingMode.HALF_UP);

        agregar(boleta, CategoriaConcepto.HABER, CodigoConcepto.SUELDO_BASICO, "Sueldo básico", sueldoProrrateado);

        BigDecimal asignacion = BigDecimal.ZERO;
        if (boleta.isAplicaAsigFamiliar()) {
            asignacion = ASIGNACION_FAMILIAR;
            agregar(boleta, CategoriaConcepto.HABER, CodigoConcepto.ASIG_FAMILIAR, "Asignación familiar", asignacion);
        }

        BigDecimal extrasHaber = boleta.getConceptos().stream()
                .filter(concepto -> concepto.getOrigen() == OrigenConcepto.MANUAL)
                .filter(concepto -> concepto.getCategoria() == CategoriaConcepto.HABER)
                .map(BoletaConcepto::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal base = sueldoProrrateado.add(asignacion).add(extrasHaber);

        AfpTasa tasa = tasaPara(boleta, afp);
        if (afp.isEsOnp()) {
            agregar(
                    boleta,
                    CategoriaConcepto.DESCUENTO,
                    CodigoConcepto.APORTE_ONP,
                    "Aporte ONP",
                    aplicar(base, tasa.getAporteObligatorio()));
        } else {
            agregar(
                    boleta,
                    CategoriaConcepto.DESCUENTO,
                    CodigoConcepto.APORTE_OBLIG,
                    "Aporte obligatorio AFP",
                    aplicar(base, tasa.getAporteObligatorio()));
            agregar(
                    boleta,
                    CategoriaConcepto.DESCUENTO,
                    CodigoConcepto.COMISION_AFP,
                    "Comisión AFP",
                    aplicar(base, tasa.getComision()));
            agregar(
                    boleta,
                    CategoriaConcepto.DESCUENTO,
                    CodigoConcepto.PRIMA_SEGURO,
                    "Prima de seguro",
                    aplicar(baseAsegurable(base, tasa), tasa.getPrimaSeguro()));
        }

        NominaConfiguracion configuracion = configuracionRepository.findById(NominaConfiguracion.ID_UNICO)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la configuración de aportes de nómina"));

        agregar(
                boleta,
                CategoriaConcepto.APORTE_EMPLEADOR,
                CodigoConcepto.ESSALUD,
                "EsSalud (aporte empleador)",
                aplicar(base, configuracion.getTasaEssalud()));
        agregar(boleta, CategoriaConcepto.APORTE_EMPLEADOR, CodigoConcepto.SCTR_SALUD,
                "SCTR Salud (aporte empleador)", aplicar(base, configuracion.getTasaSctrSalud()));
        agregar(boleta, CategoriaConcepto.APORTE_EMPLEADOR, CodigoConcepto.SCTR_PENSION,
                "SCTR Pensión (aporte empleador)", aplicar(base, configuracion.getTasaSctrPension()));
        agregar(boleta, CategoriaConcepto.APORTE_EMPLEADOR, CodigoConcepto.VIDA_LEY,
                "Seguro Vida Ley (aporte empleador)", aplicar(base, configuracion.getTasaVidaLey()));
        agregar(boleta, CategoriaConcepto.APORTE_EMPLEADOR, CodigoConcepto.ESSALUD_VIDA,
                "EsSalud +Vida (aporte empleador)", aplicar(base, configuracion.getTasaEssaludVida()));

        boleta.getConceptos().sort(Comparator
                .comparing((BoletaConcepto concepto) -> concepto.getCategoria().ordinal())
                .thenComparing(concepto -> concepto.getOrigen() == OrigenConcepto.CALCULADO ? 0 : 1)
                .thenComparingInt(BoletaConcepto::getOrden));
        int orden = 1;
        for (BoletaConcepto concepto : boleta.getConceptos()) {
            concepto.setOrden(orden++);
        }

        boleta.setTotalHaberes(sumar(boleta, CategoriaConcepto.HABER));
        boleta.setTotalDescuentos(sumar(boleta, CategoriaConcepto.DESCUENTO));
        boleta.setTotalAportesEmpleador(sumar(boleta, CategoriaConcepto.APORTE_EMPLEADOR));
        boleta.setNeto(boleta.getTotalHaberes().subtract(boleta.getTotalDescuentos()).setScale(2, RoundingMode.HALF_UP));
    }

    private static void agregar(
            BoletaPago boleta,
            CategoriaConcepto categoria,
            CodigoConcepto codigo,
            String label,
            BigDecimal monto) {
        BoletaConcepto concepto = new BoletaConcepto();
        concepto.setCategoria(categoria);
        concepto.setCodigo(codigo);
        concepto.setLabel(label);
        concepto.setMonto(monto.setScale(2, RoundingMode.HALF_UP));
        concepto.setOrden(boleta.getConceptos().size() + 1);
        concepto.setOrigen(OrigenConcepto.CALCULADO);
        boleta.addConcepto(concepto);
    }

    static BigDecimal aplicar(BigDecimal base, BigDecimal tasa) {
        BigDecimal origen = base == null ? BigDecimal.ZERO : base;
        BigDecimal factor = tasa == null ? BigDecimal.ZERO : tasa;
        return origen.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private AfpTasa tasaPara(BoletaPago boleta, Afp afp) {
        TipoComisionAfp tipo = afp.isEsOnp()
                ? TipoComisionAfp.NO_APLICA
                : boleta.getPersona().getTipoComisionAfp();
        return afpTasaRepository.findByAfpIdAndTipoComisionAndActivoTrue(afp.getId(), tipo)
                .orElseThrow(() -> new IllegalArgumentException("No hay tasa activa para la AFP y tipo de comisión del trabajador"));
    }

    private static BigDecimal baseAsegurable(BigDecimal base, AfpTasa tasa) {
        if (tasa.getTopeRemuneracionAsegurable() == null) {
            return base;
        }
        return base.min(tasa.getTopeRemuneracionAsegurable());
    }

    private static BigDecimal sumar(BoletaPago boleta, CategoriaConcepto categoria) {
        return boleta.getConceptos().stream()
                .filter(concepto -> concepto.getCategoria() == categoria)
                .map(BoletaConcepto::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
