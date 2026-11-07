package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AsientoObraBloqueDTO {

    private final String codigo;
    private final String titulo;
    private final List<AsientoObraLineaDTO> lineas;
    private final BigDecimal totalDebe;
    private final BigDecimal totalHaber;
}
