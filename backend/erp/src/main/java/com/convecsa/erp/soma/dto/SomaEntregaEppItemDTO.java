package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.EstadoItemEpp;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaEntregaEppItemDTO {

    private final Long id;
    private final Long materialId;
    private final String materialCodigo;
    private final String materialDescripcion;
    private final BigDecimal cantidad;
    private final String talla;
    private final LocalDate fechaVencimientoEstimada;
    private final EstadoItemEpp estadoItem;
}
