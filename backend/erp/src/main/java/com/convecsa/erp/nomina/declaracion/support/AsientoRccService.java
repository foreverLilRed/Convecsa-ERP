package com.convecsa.erp.nomina.declaracion.support;

import com.convecsa.erp.construccion.domain.CodigoConceptoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.nomina.declaracion.domain.EstadoDeclaracion;
import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCc;
import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCcConcepto;
import com.convecsa.erp.nomina.declaracion.entity.DeclaracionMensual;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.domain.EstadoPeriodoNomina;
import com.convecsa.erp.nomina.dto.AsientoContableBloqueDTO;
import com.convecsa.erp.nomina.dto.AsientoContableDTO;
import com.convecsa.erp.nomina.dto.AsientoContableLineaDTO;
import com.convecsa.erp.nomina.entity.NominaDocumentoConfiguracion;
import com.convecsa.erp.nomina.support.AsientoContableService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AsientoRccService {

    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private final AsientoContableService asientoContableService;

    public AsientoRccService(AsientoContableService asientoContableService) {
        this.asientoContableService = asientoContableService;
    }

    public AsientoContableDTO construir(DeclaracionMensual declaracion, List<ConsolidadoCc> filas) {
        if (filas.isEmpty()) {
            throw new IllegalArgumentException("No hay consolidado RCC para generar el asiento contable");
        }
        BigDecimal haberes = CERO;
        BigDecimal descuentos = CERO;
        BigDecimal aportes = CERO;
        BigDecimal neto = CERO;
        Map<String, BigDecimal> descuentosPorCuenta = new LinkedHashMap<>();
        Map<String, BigDecimal> aportesGasto = new LinkedHashMap<>();
        Map<String, BigDecimal> aportesPasivo = new LinkedHashMap<>();
        Map<DestinoContable, BigDecimal> destinos = new LinkedHashMap<>();

        for (ConsolidadoCc fila : filas) {
            haberes = haberes.add(n(fila.getTotalHaberes()));
            descuentos = descuentos.add(n(fila.getTotalDescuentos()));
            aportes = aportes.add(n(fila.getTotalAportesEmpleador()));
            neto = neto.add(n(fila.getNeto()));
            acumular(destinos, destino(fila.getProyecto()), n(fila.getTotalHaberes()).add(n(fila.getTotalAportesEmpleador())));
            for (ConsolidadoCcConcepto concepto : fila.getConceptos()) {
                if (concepto.getCategoria() == CategoriaConcepto.DESCUENTO) {
                    acumular(descuentosPorCuenta, cuentaDescuento(concepto.getCodigo()), n(concepto.getMonto()));
                } else if (concepto.getCategoria() == CategoriaConcepto.APORTE_EMPLEADOR) {
                    acumular(aportesGasto, cuentaGastoAporte(concepto.getCodigo()), n(concepto.getMonto()));
                    acumular(aportesPasivo, cuentaPasivoAporte(concepto.getCodigo()), n(concepto.getMonto()));
                }
            }
        }

        List<AsientoContableLineaDTO> devengo = new ArrayList<>();
        agregarDebe(devengo, "6211", "Sueldos y salarios", haberes);
        agregarDebe(devengo, "6271", "Régimen de prestaciones de salud", valor(aportesGasto, "6271"));
        agregarDebe(devengo, "6273", "SCTR, accidentes y enfermedades profesionales", valor(aportesGasto, "6273"));
        agregarDebe(devengo, "627", "Otras cargas de seguridad y previsión social", valor(aportesGasto, "627"));

        agregarHaber(devengo, "40173", "Renta de quinta categoría por pagar", valor(descuentosPorCuenta, "40173"));
        agregarHaber(devengo, "4031", "EsSalud por pagar", valor(aportesPasivo, "4031"));
        agregarHaber(devengo, "4032", "ONP por pagar", valor(descuentosPorCuenta, "4032"));
        agregarHaber(devengo, "4038", "CONAFOVICER por pagar", valor(descuentosPorCuenta, "4038"));
        agregarHaber(devengo, "407", "Administradoras de fondos de pensiones", valor(descuentosPorCuenta, "407"));
        agregarHaber(devengo, "4151", "Fondo de capacitación por pagar", valor(aportesPasivo, "4151"));
        agregarHaber(devengo, "4171", "Cuota sindical por pagar", valor(descuentosPorCuenta, "4171"));
        agregarHaber(devengo, "4111", "Sueldos y salarios por pagar", neto);
        BigDecimal otros = valor(descuentosPorCuenta, "4699").add(valor(aportesPasivo, "4699"));
        agregarHaber(devengo, "4699", "Otras deducciones y seguros por pagar", otros);

        BigDecimal totalDevengoDebe = sumarDebe(devengo);
        BigDecimal totalDevengoHaber = sumarHaber(devengo);
        ajustarDiferencia(devengo, totalDevengoDebe.subtract(totalDevengoHaber));
        totalDevengoDebe = sumarDebe(devengo);
        totalDevengoHaber = sumarHaber(devengo);

        List<AsientoContableLineaDTO> destino = new ArrayList<>();
        destinos.forEach((cuenta, monto) -> agregarDebe(
                destino, cuenta.cuenta(), cuenta.descripcion(), cuenta.auxiliar(), monto));
        BigDecimal totalDestinoDebe = sumarDebe(destino);
        agregarHaber(destino, "791", "Cargas imputables a cuentas de costos y gastos", totalDestinoDebe);
        BigDecimal totalDestinoHaber = sumarHaber(destino);

        List<AsientoContableBloqueDTO> asientos = List.of(
                new AsientoContableBloqueDTO("PLANILLA", "Devengo de la planilla RCC", devengo,
                        totalDevengoDebe, totalDevengoHaber),
                new AsientoContableBloqueDTO("DESTINO", "Destino del gasto por obra / centro de costo", destino,
                        totalDestinoDebe, totalDestinoHaber));
        BigDecimal totalDebe = totalDevengoDebe.add(totalDestinoDebe).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalHaber = totalDevengoHaber.add(totalDestinoHaber).setScale(2, RoundingMode.HALF_UP);

        return new AsientoContableDTO(
                declaracion.getId(),
                declaracion.getAnio(),
                declaracion.getMes(),
                declaracion.getEstado() == EstadoDeclaracion.CERRADO
                        ? EstadoPeriodoNomina.CERRADO
                        : EstadoPeriodoNomina.BORRADOR,
                declaracion.getEstado() != EstadoDeclaracion.CERRADO,
                Instant.now(),
                filas.size(),
                haberes.setScale(2, RoundingMode.HALF_UP),
                descuentos.setScale(2, RoundingMode.HALF_UP),
                aportes.setScale(2, RoundingMode.HALF_UP),
                neto.setScale(2, RoundingMode.HALF_UP),
                asientos,
                totalDebe,
                totalHaber,
                totalDebe.subtract(totalHaber).abs().setScale(2, RoundingMode.HALF_UP));
    }

    public byte[] generarPdf(AsientoContableDTO asiento, NominaDocumentoConfiguracion empresa) {
        return asientoContableService.generarPdf(asiento, empresa);
    }

    private static DestinoContable destino(Proyecto proyecto) {
        if (proyecto == null) {
            return new DestinoContable("92", "Costos de producción / obra", "RCC");
        }
        String auxiliar = proyecto.getCodigo();
        if (proyecto.getCentroCosto() != null && !proyecto.getCentroCosto().isBlank()) {
            auxiliar = proyecto.getCodigo() + " / " + proyecto.getCentroCosto().trim();
        }
        return new DestinoContable("92", "Costos de producción / obra", auxiliar);
    }

    private static String cuentaDescuento(CodigoConceptoCc codigo) {
        return switch (codigo) {
            case ONP -> "4032";
            case COMISION_AFP, PRIMA_AFP, APORTE_AFP -> "407";
            case CONAFOVICER -> "4038";
            case RENTA_5TA -> "40173";
            case CUOTA_SINDICAL -> "4171";
            default -> "4699";
        };
    }

    private static String cuentaGastoAporte(CodigoConceptoCc codigo) {
        return switch (codigo) {
            case ESSALUD -> "6271";
            case SCTR_SALUD, SCTR_PENSION -> "6273";
            default -> "627";
        };
    }

    private static String cuentaPasivoAporte(CodigoConceptoCc codigo) {
        return switch (codigo) {
            case ESSALUD -> "4031";
            case FONDO_CAPACITACION -> "4151";
            default -> "4699";
        };
    }

    private static void ajustarDiferencia(List<AsientoContableLineaDTO> lineas, BigDecimal diferencia) {
        if (diferencia.signum() < 0) {
            agregarDebe(lineas, "6211", "Ajuste de redondeo de planilla", diferencia.abs());
            return;
        }
        if (diferencia.signum() == 0) {
            return;
        }
        for (int i = 0; i < lineas.size(); i++) {
            AsientoContableLineaDTO linea = lineas.get(i);
            if ("4699".equals(linea.getCuenta())) {
                lineas.set(i, new AsientoContableLineaDTO(
                        linea.getCuenta(), linea.getDescripcion(), linea.getDestino(),
                        linea.getDebe(), linea.getHaber().add(diferencia)));
                return;
            }
        }
        agregarHaber(lineas, "4699", "Ajuste de redondeo de planilla", diferencia);
    }

    private record DestinoContable(String cuenta, String descripcion, String auxiliar) {}

    private static <K> void acumular(Map<K, BigDecimal> mapa, K clave, BigDecimal monto) {
        mapa.merge(clave, n(monto), BigDecimal::add);
    }

    private static BigDecimal valor(Map<String, BigDecimal> mapa, String clave) {
        return n(mapa.get(clave));
    }

    private static BigDecimal n(BigDecimal valor) {
        return valor == null ? CERO : valor.setScale(2, RoundingMode.HALF_UP);
    }

    private static void agregarDebe(List<AsientoContableLineaDTO> lineas, String cuenta, String descripcion, BigDecimal monto) {
        agregarDebe(lineas, cuenta, descripcion, null, monto);
    }

    private static void agregarDebe(
            List<AsientoContableLineaDTO> lineas, String cuenta, String descripcion, String destino, BigDecimal monto) {
        monto = n(monto);
        if (monto.signum() > 0) {
            lineas.add(new AsientoContableLineaDTO(cuenta, descripcion, destino, monto, CERO));
        }
    }

    private static void agregarHaber(List<AsientoContableLineaDTO> lineas, String cuenta, String descripcion, BigDecimal monto) {
        monto = n(monto);
        if (monto.signum() > 0) {
            lineas.add(new AsientoContableLineaDTO(cuenta, descripcion, null, CERO, monto));
        }
    }

    private static BigDecimal sumarDebe(List<AsientoContableLineaDTO> lineas) {
        return lineas.stream().map(AsientoContableLineaDTO::getDebe).reduce(CERO, BigDecimal::add);
    }

    private static BigDecimal sumarHaber(List<AsientoContableLineaDTO> lineas) {
        return lineas.stream().map(AsientoContableLineaDTO::getHaber).reduce(CERO, BigDecimal::add);
    }
}
