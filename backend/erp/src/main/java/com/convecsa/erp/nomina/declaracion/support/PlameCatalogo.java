package com.convecsa.erp.nomina.declaracion.support;

import com.convecsa.erp.asistencia.domain.CodigoTareo;
import com.convecsa.erp.construccion.domain.CodigoConceptoCc;
import com.convecsa.erp.nomina.domain.CodigoConcepto;
import com.convecsa.erp.persona.domain.TipoDocumento;
import java.util.EnumMap;
import java.util.Map;

public final class PlameCatalogo {

    public static final String RXH_HONORARIO = "0121";
    public static final String RXH_RETENCION_CUARTA = "0604";

    private static final Map<CodigoConceptoCc, String> RCC = new EnumMap<>(CodigoConceptoCc.class);
    private static final Map<CodigoConcepto, String> ADMIN = new EnumMap<>(CodigoConcepto.class);

    static {
        RCC.put(CodigoConceptoCc.JORNAL_BASICO, "0121");
        RCC.put(CodigoConceptoCc.DOMINICAL, "0115");
        RCC.put(CodigoConceptoCc.BUC, "0311");
        RCC.put(CodigoConceptoCc.MOVILIDAD, "0909");
        RCC.put(CodigoConceptoCc.VACACIONES_10, "0114");
        RCC.put(CodigoConceptoCc.GRATIFICACION, "0407");
        RCC.put(CodigoConceptoCc.BONIF_LEY_29351, "0313");
        RCC.put(CodigoConceptoCc.CTS_15, "0904");
        RCC.put(CodigoConceptoCc.ESCOLARIDAD, "0211");
        RCC.put(CodigoConceptoCc.HE_60, "1001");
        RCC.put(CodigoConceptoCc.HE_100, "1002");
        RCC.put(CodigoConceptoCc.INDEMNIZACION_HE, "0507");
        RCC.put(CodigoConceptoCc.BAE, "1003");
        RCC.put(CodigoConceptoCc.ALTURA, "1004");
        RCC.put(CodigoConceptoCc.ALTITUD, "0303");
        RCC.put(CodigoConceptoCc.COTA_CERO, "1005");
        RCC.put(CodigoConceptoCc.ALTAS_TEMP, "1006");
        RCC.put(CodigoConceptoCc.CONTACTO_AGUA, "0310");
        RCC.put(CodigoConceptoCc.AGUAS_SERVIDAS, "1007");
        RCC.put(CodigoConceptoCc.NOCTURNO, "0309");
        RCC.put(CodigoConceptoCc.FERIA_TRABAJADO, "0107");
        RCC.put(CodigoConceptoCc.DESCANSO_TRABAJADO, "0107");
        RCC.put(CodigoConceptoCc.REM_FERIADO, "0115");
        RCC.put(CodigoConceptoCc.ONP, "0607");
        RCC.put(CodigoConceptoCc.COMISION_AFP, "0601");
        RCC.put(CodigoConceptoCc.PRIMA_AFP, "0606");
        RCC.put(CodigoConceptoCc.APORTE_AFP, "0608");
        RCC.put(CodigoConceptoCc.CONAFOVICER, "0602");
        RCC.put(CodigoConceptoCc.RENTA_5TA, "0605");
        RCC.put(CodigoConceptoCc.CUOTA_SINDICAL, "0702");
        RCC.put(CodigoConceptoCc.ESSALUD, "0804");
        RCC.put(CodigoConceptoCc.FONDO_CAPACITACION, "0809");
        RCC.put(CodigoConceptoCc.SCTR_SALUD, "0806");
        RCC.put(CodigoConceptoCc.SCTR_PENSION, "0813");

        ADMIN.put(CodigoConcepto.SUELDO_BASICO, "0121");
        ADMIN.put(CodigoConcepto.ASIG_FAMILIAR, "0201");
        ADMIN.put(CodigoConcepto.APORTE_OBLIG, "0608");
        ADMIN.put(CodigoConcepto.COMISION_AFP, "0601");
        ADMIN.put(CodigoConcepto.PRIMA_SEGURO, "0606");
        ADMIN.put(CodigoConcepto.APORTE_ONP, "0607");
        ADMIN.put(CodigoConcepto.ESSALUD, "0804");
        ADMIN.put(CodigoConcepto.SCTR_SALUD, "0806");
        ADMIN.put(CodigoConcepto.SCTR_PENSION, "0813");
        ADMIN.put(CodigoConcepto.VIDA_LEY, "0803");
        ADMIN.put(CodigoConcepto.ESSALUD_VIDA, "0815");
        ADMIN.put(CodigoConcepto.EXTRA, "0904");
    }

    private PlameCatalogo() {}

    public static String codigoRcc(CodigoConceptoCc codigo) {
        return RCC.getOrDefault(codigo, "0904");
    }

    public static String codigoAdmin(CodigoConcepto codigo) {
        return ADMIN.getOrDefault(codigo, "0904");
    }

    public static String tipoDocumentoPlame(TipoDocumento tipo) {
        if (tipo == null) {
            return "01";
        }
        return switch (tipo) {
            case DNI -> "01";
            case CE -> "04";
            case PASAPORTE -> "07";
        };
    }

    public static String tipoDocumentoAfpnet(TipoDocumento tipo) {
        if (tipo == TipoDocumento.CE) {
            return "1";
        }
        return "0";
    }

    public static String tipoSuspensionAdmin(CodigoTareo codigo) {
        if (codigo == null) {
            return null;
        }
        return switch (codigo) {
            case F, TF -> "07";
            case DM -> "20";
            case SIT -> "21";
            case MAT_PRE, MAT_POST -> "22";
            case LCG -> "26";
            case PAT -> "28";
            case FI -> "32";
            default -> null;
        };
    }
}
