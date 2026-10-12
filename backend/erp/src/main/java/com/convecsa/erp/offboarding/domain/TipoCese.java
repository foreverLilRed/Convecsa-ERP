package com.convecsa.erp.offboarding.domain;

public enum TipoCese {
    RENUNCIA,
    VENCIMIENTO_PLAZO,
    MUTUO_ACUERDO,
    DESPIDO;

    public String label() {
        return switch (this) {
            case RENUNCIA -> "Renuncia voluntaria";
            case VENCIMIENTO_PLAZO -> "Vencimiento de plazo";
            case MUTUO_ACUERDO -> "Mutuo acuerdo";
            case DESPIDO -> "Despido";
        };
    }
}
