package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AsientoObraLineaDTO {

    private final String cuenta;
    private final String descripcion;
    private final String auxiliar;
    private final BigDecimal debe;
    private final BigDecimal haber;
}
