package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvancePartidaDTO {

    private final Long partidaId;
    private final String codigo;
    private final String descripcion;
    private final String unidad;
    private final String centroCosto;
    private final BigDecimal metradoMeta;
    private final BigDecimal metradoAnterior;
    private final BigDecimal metradoPeriodo;
    private final BigDecimal metradoAcumulado;
    private final BigDecimal desvio;
}
