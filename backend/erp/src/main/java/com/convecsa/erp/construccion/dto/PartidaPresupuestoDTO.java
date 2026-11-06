package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PartidaPresupuestoDTO {

    private final Long id;
    private final String codigo;
    private final String capitulo;
    private final String descripcion;
    private final String unidad;
    private final BigDecimal metrado;
    private final BigDecimal precioUnitario;
    private final BigDecimal total;
    private final String centroCosto;
    private final int orden;
    private final List<ApuPartidaDTO> apu;
}
