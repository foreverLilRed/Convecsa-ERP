package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.TipoEpp;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MaterialDTO {

    private final Long id;
    private final String codigo;
    private final String descripcion;
    private final String unidad;
    private final String familia;
    private final BigDecimal stockMinimo;
    private final boolean critico;
    private final boolean activo;
    private final boolean esEpp;
    private final TipoEpp tipoEpp;
    private final Integer vidaUtilDias;
    private final String normaCertificacion;
}
