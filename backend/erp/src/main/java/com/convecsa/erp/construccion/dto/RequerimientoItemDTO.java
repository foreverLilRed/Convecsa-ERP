package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequerimientoItemDTO {

    private final Long id;
    private final Long materialId;
    private final String codigoMaterial;
    private final String descripcionMaterial;
    private final String unidad;
    private final Long partidaId;
    private final String codigoPartida;
    private final String descripcionPartida;
    private final String centroCosto;
    private final BigDecimal cantidad;
    private final BigDecimal cantidadAtendida;
}
