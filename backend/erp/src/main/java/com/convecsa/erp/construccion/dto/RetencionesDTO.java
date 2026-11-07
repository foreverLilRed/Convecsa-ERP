package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RetencionesDTO {

    private final BigDecimal retenidoAcumulado;
    private final BigDecimal liberado;
    private final BigDecimal cartaFianza;
    private final BigDecimal saldo;
    private final List<LiberacionRetencionDTO> movimientos;
}
