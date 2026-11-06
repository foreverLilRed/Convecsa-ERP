package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SugeridoApuDTO {

    private final Long materialId;
    private final String codigo;
    private final String descripcion;
    private final String unidad;
    private final Long partidaId;
    private final String codigoPartida;
    private final String descripcionPartida;
    private final String centroCosto;
    private final BigDecimal cantidad;
}
