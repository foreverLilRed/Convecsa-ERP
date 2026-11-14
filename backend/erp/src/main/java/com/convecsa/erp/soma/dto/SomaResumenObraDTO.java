package com.convecsa.erp.soma.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaResumenObraDTO {

    private final Long proyectoId;
    private final String codigoObra;
    private final String nombreObra;
    private final String supervisorSsoma;
    private final int habilitados;
    private final int bloqueados;
    private final int emosPorVencer30Dias;
    private final BigDecimal hhtMes;
    private final int accidentesMes;
    private final int diasPerdidosMes;
    private final BigDecimal indiceFrecuencia;
    private final BigDecimal indiceSeveridad;
    private final BigDecimal indiceAccidentabilidad;
    private final Integer diasSinAccidente;
}
