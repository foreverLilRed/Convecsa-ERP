package com.convecsa.erp.soma.domain;

public enum NivelRiesgoIperc {
    TRIVIAL,
    TOLERABLE,
    MODERADO,
    IMPORTANTE,
    INTOLERABLE;

    public static NivelRiesgoIperc desdePuntaje(int puntaje) {
        if (puntaje < 1 || puntaje > 25) {
            throw new IllegalArgumentException("El puntaje de riesgo debe estar entre 1 y 25");
        }
        if (puntaje <= 4) {
            return TRIVIAL;
        }
        if (puntaje <= 8) {
            return TOLERABLE;
        }
        if (puntaje <= 12) {
            return MODERADO;
        }
        if (puntaje <= 19) {
            return IMPORTANTE;
        }
        return INTOLERABLE;
    }
}
