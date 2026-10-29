package com.convecsa.erp.nomina.declaracion.support;

import com.convecsa.erp.asistencia.entity.AsistenciaRegistro;
import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCc;
import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCcConcepto;
import com.convecsa.erp.nomina.declaracion.entity.ReciboHonorario;
import com.convecsa.erp.nomina.entity.BoletaConcepto;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

@Service
public class PlameArchivoService {

    private static final BigDecimal OCHO = new BigDecimal("8");

    public String generarJor(List<ConsolidadoCc> obreros, List<BoletaPago> administrativos) {
        List<String> lineas = new ArrayList<>();
        for (ConsolidadoCc fila : obreros) {
            Persona persona = fila.getPersona();
            BigDecimal extras = PlameFormato.n(fila.getHe60()).add(PlameFormato.n(fila.getHe100()));
            lineas.add(lineaJor(persona, fila.getHorasOrd(), extras));
        }
        for (BoletaPago boleta : administrativos) {
            BigDecimal horas = BigDecimal.valueOf(boleta.getDiasTrabajados()).multiply(OCHO);
            lineas.add(lineaJor(boleta.getPersona(), horas, BigDecimal.ZERO));
        }
        lineas.sort(Comparator.naturalOrder());
        return unir(lineas);
    }

    public String generarRem(
            List<ConsolidadoCc> obreros,
            List<BoletaPago> administrativos,
            List<ReciboHonorario> recibos) {
        Map<String, LineaRem> lineas = new TreeMap<>();
        for (ConsolidadoCc fila : obreros) {
            Persona persona = fila.getPersona();
            for (ConsolidadoCcConcepto concepto : fila.getConceptos()) {
                if (PlameFormato.n(concepto.getMonto()).signum() <= 0) {
                    continue;
                }
                String codigo = PlameCatalogo.codigoRcc(concepto.getCodigo());
                BigDecimal cantidad = cantidadRcc(concepto, fila);
                acumular(lineas, clave(persona, codigo), persona, codigo, cantidad, concepto.getMonto());
            }
        }
        for (BoletaPago boleta : administrativos) {
            Persona persona = boleta.getPersona();
            for (BoletaConcepto concepto : boleta.getConceptos()) {
                if (PlameFormato.n(concepto.getMonto()).signum() <= 0) {
                    continue;
                }
                String codigo = PlameCatalogo.codigoAdmin(concepto.getCodigo());
                acumular(lineas, clave(persona, codigo), persona, codigo, BigDecimal.ONE, concepto.getMonto());
            }
        }
        for (ReciboHonorario recibo : recibos) {
            if (PlameFormato.n(recibo.getMonto()).signum() <= 0) {
                continue;
            }
            String tipoDoc = tipoDocRxh(recibo);
            String numDoc = documentoRxh(recibo);
            acumularDocumento(
                    lineas,
                    tipoDoc + "|" + numDoc + "|" + PlameCatalogo.RXH_HONORARIO,
                    tipoDoc,
                    numDoc,
                    PlameCatalogo.RXH_HONORARIO,
                    BigDecimal.ONE,
                    recibo.getMonto());
            if (recibo.isRetencionCuarta()) {
                BigDecimal retencion = recibo.getMonto().multiply(new BigDecimal("0.08"))
                        .setScale(2, RoundingMode.HALF_UP);
                if (retencion.signum() > 0) {
                    acumularDocumento(
                            lineas,
                            tipoDoc + "|" + numDoc + "|" + PlameCatalogo.RXH_RETENCION_CUARTA,
                            tipoDoc,
                            numDoc,
                            PlameCatalogo.RXH_RETENCION_CUARTA,
                            BigDecimal.ONE,
                            retencion);
                }
            }
        }
        List<String> salida = new ArrayList<>();
        for (LineaRem linea : lineas.values()) {
            salida.add(String.join("|",
                    linea.tipoDoc,
                    linea.numDoc,
                    linea.codigo,
                    PlameFormato.cantidad(linea.cantidad),
                    PlameFormato.monto(linea.devengado),
                    PlameFormato.monto(linea.pagado)));
        }
        return unir(salida);
    }

    public String generarSnl(List<ConsolidadoCc> obreros, List<AsistenciaRegistro> asistencias) {
        Map<String, Integer> dias = new TreeMap<>();
        Map<String, PersonaRef> personas = new LinkedHashMap<>();
        for (ConsolidadoCc fila : obreros) {
            Persona persona = fila.getPersona();
            agregarSnl(dias, personas, persona, "07", fila.getDiasFalta());
            agregarSnl(dias, personas, persona, "20", fila.getDiasDm());
            agregarSnl(dias, personas, persona, "26", fila.getDiasLic());
        }
        for (AsistenciaRegistro registro : asistencias) {
            String tipo = PlameCatalogo.tipoSuspensionAdmin(registro.getCodigo());
            if (tipo != null) {
                agregarSnl(dias, personas, registro.getPersona(), tipo, 1);
            }
        }
        List<String> lineas = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dias.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            PersonaRef ref = personas.get(entry.getKey());
            String tipo = entry.getKey().substring(entry.getKey().lastIndexOf('|') + 1);
            lineas.add(String.join("|", ref.tipoDoc, ref.numDoc, tipo, String.valueOf(entry.getValue())));
        }
        return unir(lineas);
    }

    private static String lineaJor(Persona persona, BigDecimal horasOrd, BigDecimal horasExtra) {
        PlameFormato.HorasMinutos ord = PlameFormato.horasMinutos(horasOrd);
        PlameFormato.HorasMinutos extra = PlameFormato.horasMinutos(horasExtra);
        return String.join("|",
                PlameCatalogo.tipoDocumentoPlame(persona.getTipoDocumento()),
                PlameFormato.limpio(persona.getNumeroDocumento()),
                String.valueOf(ord.horas()),
                String.valueOf(ord.minutos()),
                String.valueOf(extra.horas()),
                String.valueOf(extra.minutos()));
    }

    private static BigDecimal cantidadRcc(ConsolidadoCcConcepto concepto, ConsolidadoCc fila) {
        return switch (concepto.getCodigo()) {
            case HE_60 -> PlameFormato.n(fila.getHe60());
            case HE_100 -> PlameFormato.n(fila.getHe100());
            default -> BigDecimal.ONE;
        };
    }

    private static void acumular(
            Map<String, LineaRem> lineas,
            String clave,
            Persona persona,
            String codigo,
            BigDecimal cantidad,
            BigDecimal monto) {
        acumularDocumento(
                lineas,
                clave,
                PlameCatalogo.tipoDocumentoPlame(persona.getTipoDocumento()),
                PlameFormato.limpio(persona.getNumeroDocumento()),
                codigo,
                cantidad,
                monto);
    }

    private static void acumularDocumento(
            Map<String, LineaRem> lineas,
            String clave,
            String tipoDoc,
            String numDoc,
            String codigo,
            BigDecimal cantidad,
            BigDecimal monto) {
        LineaRem actual = lineas.get(clave);
        if (actual == null) {
            lineas.put(clave, new LineaRem(tipoDoc, numDoc, codigo, cantidad, monto, monto));
            return;
        }
        actual.cantidad = actual.cantidad.add(cantidad);
        actual.devengado = actual.devengado.add(monto);
        actual.pagado = actual.pagado.add(monto);
    }

    private static void agregarSnl(
            Map<String, Integer> dias,
            Map<String, PersonaRef> personas,
            Persona persona,
            String tipo,
            int nroDias) {
        if (nroDias <= 0) {
            return;
        }
        String clave = clave(persona, tipo);
        personas.putIfAbsent(clave, new PersonaRef(
                PlameCatalogo.tipoDocumentoPlame(persona.getTipoDocumento()),
                PlameFormato.limpio(persona.getNumeroDocumento())));
        dias.merge(clave, nroDias, Integer::sum);
    }

    private static String clave(Persona persona, String codigo) {
        return PlameCatalogo.tipoDocumentoPlame(persona.getTipoDocumento())
                + "|" + PlameFormato.limpio(persona.getNumeroDocumento())
                + "|" + codigo;
    }

    private static String tipoDocRxh(ReciboHonorario recibo) {
        if (recibo.getPersona() != null) {
            return PlameCatalogo.tipoDocumentoPlame(recibo.getPersona().getTipoDocumento());
        }
        String tipo = recibo.getTipoDocumento();
        if ("06".equals(tipo) && documentoRxh(recibo).length() == 8) {
            return "01";
        }
        return tipo == null || tipo.isBlank() ? "01" : tipo;
    }

    private static String documentoRxh(ReciboHonorario recibo) {
        if (recibo.getPersona() != null) {
            return PlameFormato.limpio(recibo.getPersona().getNumeroDocumento());
        }
        String numero = PlameFormato.limpio(recibo.getNumeroDocumento());
        if ("06".equals(recibo.getTipoDocumento()) && numero.startsWith("10") && numero.length() >= 11) {
            return numero.substring(2, 10);
        }
        return numero;
    }

    private static String unir(List<String> lineas) {
        if (lineas.isEmpty()) {
            return "";
        }
        return String.join("\r\n", lineas) + "\r\n";
    }

    private static class LineaRem {
        private final String tipoDoc;
        private final String numDoc;
        private final String codigo;
        private BigDecimal cantidad;
        private BigDecimal devengado;
        private BigDecimal pagado;

        private LineaRem(
                String tipoDoc,
                String numDoc,
                String codigo,
                BigDecimal cantidad,
                BigDecimal devengado,
                BigDecimal pagado) {
            this.tipoDoc = tipoDoc;
            this.numDoc = numDoc;
            this.codigo = codigo;
            this.cantidad = cantidad;
            this.devengado = devengado;
            this.pagado = pagado;
        }
    }

    private record PersonaRef(String tipoDoc, String numDoc) {}
}
