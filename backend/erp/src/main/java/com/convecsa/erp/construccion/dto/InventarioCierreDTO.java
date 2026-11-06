package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventarioCierreDTO {

    private final int anio;
    private final int mes;
    private final Instant createdAt;
    private final BigDecimal valorTotal;
    private final List<InventarioCierreItemDTO> items;
}
