package com.convecsa.erp.soma.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaEstadisticaMesDTO {

    private final Integer mes;
    private final BigDecimal hht;
    private final int accidentes;
    private final int diasPerdidos;
    private final BigDecimal indiceFrecuencia;
    private final BigDecimal indiceSeveridad;
    private final BigDecimal indiceAccidentabilidad;
}
