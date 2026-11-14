package com.convecsa.erp.soma.domain;

public enum TipoRequerimientoComiteSoma {
    SUBCOMITE,
    SUPERVISOR_SST;

    public static final int UMBRAL_SUBCOMITE = 20;

    public static TipoRequerimientoComiteSoma desdeActivos(int trabajadoresActivos) {
        return trabajadoresActivos >= UMBRAL_SUBCOMITE ? SUBCOMITE : SUPERVISOR_SST;
    }
}
