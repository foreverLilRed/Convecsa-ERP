package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.TipoRecursoApu;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApuPartidaDTO {

    private final Long id;
    private final TipoRecursoApu tipo;
    private final String codigo;
    private final String descripcion;
    private final String unidad;
    private final BigDecimal cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal parcial;
    private final int orden;
}
