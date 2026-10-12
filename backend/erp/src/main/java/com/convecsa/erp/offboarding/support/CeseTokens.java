package com.convecsa.erp.offboarding.support;

import com.convecsa.erp.contrato.support.SueldoEnLetras;
import com.convecsa.erp.offboarding.domain.LiquidacionSnapshot;
import com.convecsa.erp.offboarding.entity.Cese;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class CeseTokens {

    private static final DateTimeFormatter FECHA_LARGA =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-PE"));

    private CeseTokens() {
    }

    public static Map<String, String> de(Cese cese) {
        LiquidacionSnapshot liq = cese.getLiquidacion() == null ? new LiquidacionSnapshot() : cese.getLiquidacion();
        Map<String, String> extras = new LinkedHashMap<>();
        extras.put("fecha_cese", fecha(cese.getFechaCese()));
        extras.put("fecha_fin", fecha(cese.getFechaCese()));
        extras.put("fecha_firma", fecha(cese.getFechaCese()));
        extras.put("tipo_cese", cese.getTipoCese() == null ? "" : cese.getTipoCese().label());
        extras.put("motivo_cese", cese.getMotivo() == null || cese.getMotivo().isBlank() ? "—" : cese.getMotivo().trim());
        extras.put("tiempo_servicio", liq.getTiempoServicio() == null ? "" : liq.getTiempoServicio());
        extras.put("liq_cts", monto(liq.getCts()));
        extras.put("liq_grati", monto(liq.getGratificacion()));
        extras.put("liq_vacaciones", monto(liq.getVacaciones()));
        extras.put("liq_total", monto(liq.getTotal()));
        extras.put("liq_total_letras", SueldoEnLetras.de(liq.getTotal()));
        return extras;
    }

    private static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FECHA_LARGA);
    }

    private static String monto(BigDecimal valor) {
        if (valor == null) {
            return "0.00";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("es-PE"));
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("#,##0.00", symbols).format(valor);
    }
}
