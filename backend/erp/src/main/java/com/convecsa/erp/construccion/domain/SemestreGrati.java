package com.convecsa.erp.construccion.domain;

import java.time.LocalDate;

public enum SemestreGrati {
    FIESTAS_PATRIAS,
    NAVIDAD;

    public static SemestreGrati de(LocalDate fecha) {
        if (fecha == null || fecha.getMonthValue() <= 7) {
            return FIESTAS_PATRIAS;
        }
        return NAVIDAD;
    }
}
