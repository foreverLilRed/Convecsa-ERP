package com.convecsa.erp.nomina.support;

import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.domain.CodigoConcepto;
import com.convecsa.erp.nomina.domain.EstadoPeriodoNomina;
import com.convecsa.erp.nomina.dto.AsientoContableBloqueDTO;
import com.convecsa.erp.nomina.dto.AsientoContableDTO;
import com.convecsa.erp.nomina.dto.AsientoContableLineaDTO;
import com.convecsa.erp.nomina.entity.BoletaConcepto;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.nomina.entity.NominaDocumentoConfiguracion;
import com.convecsa.erp.nomina.entity.PeriodoNomina;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.Instant;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AsientoContableService {

    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final Locale ES_PE = Locale.forLanguageTag("es-PE");
    private final BoletaPdfService pdfService;

    public AsientoContableService(BoletaPdfService pdfService) {
        this.pdfService = pdfService;
    }

    public AsientoContableDTO construir(PeriodoNomina periodo, List<BoletaPago> boletas) {
        if (boletas.isEmpty()) {
            throw new IllegalArgumentException("No hay boletas para generar el asiento contable");
        }

        BigDecimal haberes = suma(boletas, BoletaPago::getTotalHaberes);
        BigDecimal descuentos = suma(boletas, BoletaPago::getTotalDescuentos);
        BigDecimal aportes = suma(boletas, BoletaPago::getTotalAportesEmpleador);
        BigDecimal neto = suma(boletas, BoletaPago::getNeto);

        Map<String, BigDecimal> descuentosPorCuenta = new LinkedHashMap<>();
        Map<String, BigDecimal> aportesGasto = new LinkedHashMap<>();
        Map<String, BigDecimal> aportesPasivo = new LinkedHashMap<>();
        Map<DestinoContable, BigDecimal> destinos = new LinkedHashMap<>();

        for (BoletaPago boleta : boletas) {
            acumular(destinos, destino(boleta.getContrato()),
                    normalizar(boleta.getTotalHaberes()).add(normalizar(boleta.getTotalAportesEmpleador())));
            for (BoletaConcepto concepto : boleta.getConceptos()) {
                if (concepto.getCategoria() == CategoriaConcepto.DESCUENTO) {
                    acumular(descuentosPorCuenta, cuentaDescuento(concepto), normalizar(concepto.getMonto()));
                } else if (concepto.getCategoria() == CategoriaConcepto.APORTE_EMPLEADOR) {
                    String gasto = cuentaGastoAporte(concepto.getCodigo());
                    String pasivo = cuentaPasivoAporte(concepto.getCodigo());
                    acumular(aportesGasto, gasto, normalizar(concepto.getMonto()));
                    acumular(aportesPasivo, pasivo, normalizar(concepto.getMonto()));
                }
            }
        }

        List<AsientoContableLineaDTO> devengo = new ArrayList<>();
        agregarDebe(devengo, "6211", "Sueldos y salarios", haberes);
        agregarDebe(devengo, "6271", "Régimen de prestaciones de salud", aportesGasto.get("6271"));
        agregarDebe(devengo, "6273", "SCTR, accidentes y enfermedades profesionales", aportesGasto.get("6273"));
        agregarDebe(devengo, "6274", "Seguro de vida", aportesGasto.get("6274"));
        agregarDebe(devengo, "6275", "Seguros particulares de salud", aportesGasto.get("6275"));
        agregarDebe(devengo, "627", "Otras cargas de seguridad y previsión social", aportesGasto.get("627"));

        agregarHaber(devengo, "40173", "Renta de quinta categoría por pagar", descuentosPorCuenta.get("40173"));
        agregarHaber(devengo, "4031", "EsSalud por pagar", aportesPasivo.get("4031"));
        agregarHaber(devengo, "4032", "ONP por pagar", descuentosPorCuenta.get("4032"));
        agregarHaber(devengo, "407", "Administradoras de fondos de pensiones", descuentosPorCuenta.get("407"));
        agregarHaber(devengo, "4111", "Sueldos y salarios por pagar", neto);
        BigDecimal otros = valor(descuentosPorCuenta, "4699").add(valor(aportesPasivo, "4699"));
        agregarHaber(devengo, "4699", "Otras deducciones y seguros por pagar", otros);

        BigDecimal totalDevengoDebe = sumarDebe(devengo);
        BigDecimal totalDevengoHaber = sumarHaber(devengo);
        ajustarDiferenciaRedondeo(devengo, totalDevengoDebe.subtract(totalDevengoHaber));
        totalDevengoDebe = sumarDebe(devengo);
        totalDevengoHaber = sumarHaber(devengo);

        List<AsientoContableLineaDTO> destino = new ArrayList<>();
        destinos.forEach((cuenta, monto) -> agregarDebe(
                destino, cuenta.cuenta(), cuenta.descripcion(), cuenta.auxiliar(), monto));
        BigDecimal totalDestinoDebe = sumarDebe(destino);
        agregarHaber(destino, "791", "Cargas imputables a cuentas de costos y gastos", totalDestinoDebe);
        BigDecimal totalDestinoHaber = sumarHaber(destino);

        List<AsientoContableBloqueDTO> asientos = List.of(
                new AsientoContableBloqueDTO("PLANILLA", "Devengo de la planilla mensual", devengo,
                        totalDevengoDebe, totalDevengoHaber),
                new AsientoContableBloqueDTO("DESTINO", "Destino del gasto de personal", destino,
                        totalDestinoDebe, totalDestinoHaber));
        BigDecimal totalDebe = totalDevengoDebe.add(totalDestinoDebe).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalHaber = totalDevengoHaber.add(totalDestinoHaber).setScale(2, RoundingMode.HALF_UP);

        return new AsientoContableDTO(
                periodo.getId(), periodo.getAnio(), periodo.getMes(), periodo.getEstado(),
                periodo.getEstado() != EstadoPeriodoNomina.CERRADO, Instant.now(), boletas.size(),
                haberes, descuentos, aportes, neto, asientos, totalDebe, totalHaber,
                totalDebe.subtract(totalHaber).abs().setScale(2, RoundingMode.HALF_UP));
    }

    public byte[] generarPdf(AsientoContableDTO asiento, NominaDocumentoConfiguracion empresa) {
        return pdfService.convertir(renderizar(asiento, empresa));
    }

    String renderizar(AsientoContableDTO asiento, NominaDocumentoConfiguracion empresa) {
        StringBuilder bloques = new StringBuilder();
        for (AsientoContableBloqueDTO bloque : asiento.getAsientos()) {
            bloques.append("<section class='asiento'><h2>")
                    .append(escape(bloque.getCodigo())).append(" · ").append(escape(bloque.getTitulo()))
                    .append("</h2><table><thead><tr><th class='cuenta'>Cuenta</th><th>Descripción</th>")
                    .append("<th>Destino / auxiliar</th><th class='monto'>Debe</th><th class='monto'>Haber</th>")
                    .append("</tr></thead><tbody>");
            for (AsientoContableLineaDTO linea : bloque.getLineas()) {
                bloques.append("<tr><td class='cuenta'>").append(escape(linea.getCuenta()))
                        .append("</td><td>").append(escape(linea.getDescripcion()))
                        .append("</td><td>").append(escape(linea.getDestino() == null ? "—" : linea.getDestino()))
                        .append("</td><td class='monto'>").append(monto(linea.getDebe()))
                        .append("</td><td class='monto'>").append(monto(linea.getHaber())).append("</td></tr>");
            }
            bloques.append("</tbody><tfoot><tr><td colspan='3'>Totales del asiento</td><td class='monto'>")
                    .append(monto(bloque.getTotalDebe())).append("</td><td class='monto'>")
                    .append(monto(bloque.getTotalHaber())).append("</td></tr></tfoot></table></section>");
        }

        String logo = empresa.getLogoDataUrl() == null
                ? ""
                : "<img class='logo' src='" + escape(empresa.getLogoDataUrl()) + "' alt='Logo' />";
        String periodo = Month.of(asiento.getMes()).getDisplayName(TextStyle.FULL, ES_PE) + " " + asiento.getAnio();
        String estado = asiento.isPreliminar() ? "PRELIMINAR · PERIODO EN BORRADOR" : "DEFINITIVO · PERIODO CERRADO";
        return """
                <!DOCTYPE html>
                <html xmlns='http://www.w3.org/1999/xhtml' lang='es'>
                <head><meta charset='UTF-8' /><style>
                @page { size: A4; margin: 18mm 14mm 17mm; }
                * { box-sizing: border-box; }
                body { font-family: Arial, sans-serif; color: #14213d; font-size: 9.5px; margin: 0; }
                .header { width: 100%%; border-bottom: 2px solid #143b5d; padding-bottom: 10px; margin-bottom: 13px; }
                .logo { width: 105px; max-height: 48px; object-fit: contain; margin-bottom: 6px; }
                h1 { font-size: 18px; margin: 0 0 4px; letter-spacing: .3px; }
                .company { font-size: 11px; font-weight: bold; }
                .muted { color: #5f6b7a; line-height: 1.45; }
                .badge { display: inline-block; margin-top: 7px; padding: 4px 8px; border: 1px solid #2e75a7; color: #143b5d; font-weight: bold; }
                .summary { width: 100%%; border-collapse: separate; border-spacing: 6px 0; margin: 0 -6px 14px; }
                .summary td { background: #eef6fa; border: 1px solid #cce4ef; padding: 8px; width: 25%%; }
                .summary span { display: block; color: #637381; font-size: 8px; text-transform: uppercase; }
                .summary strong { display: block; margin-top: 3px; font-size: 12px; }
                .asiento { margin: 0 0 15px; page-break-inside: avoid; }
                h2 { font-size: 11px; margin: 0; padding: 7px 8px; background: #143b5d; color: white; }
                table { width: 100%%; border-collapse: collapse; }
                th, td { border: 1px solid #cdd6df; padding: 5px 6px; vertical-align: top; }
                th { background: #eaf2f7; text-align: left; color: #23384d; font-size: 8px; text-transform: uppercase; }
                .cuenta { width: 58px; font-family: 'Courier New', monospace; font-weight: bold; }
                .monto { width: 78px; text-align: right; white-space: nowrap; }
                tfoot td { background: #f4f7f9; font-weight: bold; }
                .grand { margin-top: 3px; padding: 9px; border: 1px solid #143b5d; text-align: right; font-size: 11px; }
                .ok { color: #17643a; }
                .note { margin-top: 12px; padding-top: 8px; border-top: 1px solid #cdd6df; font-size: 8px; color: #66788a; line-height: 1.45; }
                </style></head><body>
                <div class='header'>%s<div class='company'>%s · RUC %s</div><div class='muted'>%s</div>
                <h1>Asiento contable de planilla</h1><div class='muted'>Periodo: %s · %d trabajadores</div><div class='badge'>%s</div></div>
                <table class='summary'><tr><td><span>Haberes</span><strong>S/ %s</strong></td>
                <td><span>Descuentos</span><strong>S/ %s</strong></td><td><span>Aportes empleador</span><strong>S/ %s</strong></td>
                <td><span>Neto por pagar</span><strong>S/ %s</strong></td></tr></table>
                %s
                <div class='grand'>Total general: Debe S/ %s &#160;·&#160; Haber S/ %s &#160;·&#160; <span class='ok'>Diferencia S/ %s</span></div>
                <div class='note'>Base PCGE: cuentas 62, 40, 41, 46 y 79. Las cuentas analíticas 92, 94 y 95 usadas para destino deben adecuarse al plan de cuentas interno de la empresa. Los descuentos manuales no identificados y seguros pendientes se agrupan en la 4699 para revisión contable.</div>
                </body></html>
                """.formatted(logo, escape(empresa.getRazonSocial()), escape(empresa.getRuc()),
                escape(empresa.getDomicilio()), escape(periodo), asiento.getCantidadTrabajadores(), estado,
                monto(asiento.getTotalHaberes()), monto(asiento.getTotalDescuentos()),
                monto(asiento.getTotalAportesEmpleador()), monto(asiento.getTotalNeto()), bloques,
                monto(asiento.getTotalDebe()), monto(asiento.getTotalHaber()), monto(asiento.getDiferencia()));
    }

    private static String cuentaDescuento(BoletaConcepto concepto) {
        if (concepto.getCodigo() == CodigoConcepto.APORTE_ONP) return "4032";
        if (concepto.getCodigo() == CodigoConcepto.APORTE_OBLIG
                || concepto.getCodigo() == CodigoConcepto.COMISION_AFP
                || concepto.getCodigo() == CodigoConcepto.PRIMA_SEGURO) return "407";
        String etiqueta = Normalizer.normalize(concepto.getLabel(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(ES_PE);
        return etiqueta.contains("renta") && etiqueta.contains("quinta") ? "40173" : "4699";
    }

    private static String cuentaGastoAporte(CodigoConcepto codigo) {
        return switch (codigo) {
            case ESSALUD -> "6271";
            case SCTR_SALUD, SCTR_PENSION -> "6273";
            case VIDA_LEY -> "6274";
            case ESSALUD_VIDA -> "6275";
            default -> "627";
        };
    }

    private static String cuentaPasivoAporte(CodigoConcepto codigo) {
        return codigo == CodigoConcepto.ESSALUD ? "4031" : "4699";
    }

    private static DestinoContable destino(Contrato contrato) {
        if (contrato.getProyecto() != null && !contrato.getProyecto().isBlank()) {
            return new DestinoContable("92", "Costos de producción / obra", contrato.getProyecto().trim());
        }
        if (contrato.getArea() != null && !contrato.getArea().isBlank()) {
            String area = contrato.getArea().trim();
            String normalizada = Normalizer.normalize(area, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "").toLowerCase(ES_PE);
            if (normalizada.contains("venta") || normalizada.contains("comercial")) {
                return new DestinoContable("95", "Gastos de ventas", area);
            }
            return new DestinoContable("94", "Gastos administrativos", area);
        }
        return new DestinoContable("94", "Gastos administrativos", "Administración");
    }

    private static void ajustarDiferenciaRedondeo(List<AsientoContableLineaDTO> lineas, BigDecimal diferencia) {
        if (diferencia.signum() < 0) {
            agregarDebe(lineas, "6211", "Ajuste de redondeo de planilla", diferencia.abs());
            return;
        }
        if (diferencia.signum() == 0) return;
        for (int i = 0; i < lineas.size(); i++) {
            AsientoContableLineaDTO linea = lineas.get(i);
            if ("4699".equals(linea.getCuenta())) {
                lineas.set(i, new AsientoContableLineaDTO(linea.getCuenta(), linea.getDescripcion(), linea.getDestino(),
                        linea.getDebe(), linea.getHaber().add(diferencia)));
                return;
            }
        }
        agregarHaber(lineas, "4699", "Ajuste de redondeo de planilla", diferencia);
    }

    private interface Importe { BigDecimal obtener(BoletaPago boleta); }
    private record DestinoContable(String cuenta, String descripcion, String auxiliar) {}
    private static BigDecimal suma(List<BoletaPago> boletas, Importe importe) {
        return boletas.stream().map(importe::obtener).map(AsientoContableService::normalizar)
                .reduce(CERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }
    private static <K> void acumular(Map<K, BigDecimal> mapa, K clave, BigDecimal monto) {
        mapa.merge(clave, normalizar(monto), BigDecimal::add);
    }
    private static BigDecimal valor(Map<String, BigDecimal> mapa, String clave) { return normalizar(mapa.get(clave)); }
    private static BigDecimal normalizar(BigDecimal valor) { return valor == null ? CERO : valor.setScale(2, RoundingMode.HALF_UP); }
    private static void agregarDebe(List<AsientoContableLineaDTO> lineas, String cuenta, String descripcion, BigDecimal monto) {
        agregarDebe(lineas, cuenta, descripcion, null, monto);
    }
    private static void agregarDebe(List<AsientoContableLineaDTO> lineas, String cuenta, String descripcion, String destino, BigDecimal monto) {
        monto = normalizar(monto); if (monto.signum() > 0) lineas.add(new AsientoContableLineaDTO(cuenta, descripcion, destino, monto, CERO));
    }
    private static void agregarHaber(List<AsientoContableLineaDTO> lineas, String cuenta, String descripcion, BigDecimal monto) {
        monto = normalizar(monto); if (monto.signum() > 0) lineas.add(new AsientoContableLineaDTO(cuenta, descripcion, null, CERO, monto));
    }
    private static BigDecimal sumarDebe(List<AsientoContableLineaDTO> lineas) { return lineas.stream().map(AsientoContableLineaDTO::getDebe).reduce(CERO, BigDecimal::add); }
    private static BigDecimal sumarHaber(List<AsientoContableLineaDTO> lineas) { return lineas.stream().map(AsientoContableLineaDTO::getHaber).reduce(CERO, BigDecimal::add); }
    private static String monto(BigDecimal valor) { return String.format(ES_PE, "%,.2f", normalizar(valor)); }
    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
