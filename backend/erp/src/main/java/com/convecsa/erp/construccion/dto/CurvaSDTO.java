package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurvaSDTO {

    private final BigDecimal metradoMeta;
    private final BigDecimal montoMeta;
    private final BigDecimal desvioPlazoPct;
    private final List<CurvaSPuntoDTO> puntos;
}
