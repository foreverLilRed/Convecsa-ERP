package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecepcionItemDTO {

    private final Long id;
    private final Long ordenCompraItemId;
    private final Long materialId;
    private final String codigoMaterial;
    private final BigDecimal cantidad;
}
