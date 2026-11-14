package com.convecsa.erp.soma.domain;

import java.math.BigDecimal;

public enum TipoAgenteMonitoreoSoma {
    FISICO_RUIDO,
    FISICO_ILUMINACION,
    FISICO_VIBRACION,
    FISICO_ESTRES_TERMICO,
    QUIMICO_POLVO_GASES,
    BIOLOGICO,
    ERGONOMICO,
    PSICOSOCIAL,
    AMBIENTAL_AGUA_AIRE;

    public static boolean cumpleLmp(TipoAgenteMonitoreoSoma tipo, BigDecimal valorMedido, BigDecimal limitePermisible) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de agente es obligatorio");
        }
        if (valorMedido == null) {
            throw new IllegalArgumentException("El valor medido es obligatorio");
        }
        if (limitePermisible == null) {
            throw new IllegalArgumentException("El límite permisible es obligatorio");
        }
        if (tipo == FISICO_ILUMINACION) {
            return valorMedido.compareTo(limitePermisible) >= 0;
        }
        return valorMedido.compareTo(limitePermisible) <= 0;
    }
}
