package com.convecsa.erp.soma.dto;

import com.convecsa.erp.construccion.domain.TipoEpp;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaEppMaterialDTO {

    private final Long materialId;
    private final String codigo;
    private final String descripcion;
    private final String unidad;
    private final TipoEpp tipoEpp;
    private final Integer vidaUtilDias;
    private final BigDecimal stock;
}
