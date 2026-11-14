package com.convecsa.erp.soma.domain;

public enum TipoEventoSst {
    INCIDENTE,
    INCIDENTE_PELIGROSO,
    ACCIDENTE_LEVE,
    ACCIDENTE_INCAPACITANTE,
    ACCIDENTE_MORTAL,
    ENFERMEDAD_OCUPACIONAL;

    public boolean esAccidenteOEnfermedad() {
        return name().startsWith("ACCIDENTE_") || this == ENFERMEDAD_OCUPACIONAL;
    }
}
