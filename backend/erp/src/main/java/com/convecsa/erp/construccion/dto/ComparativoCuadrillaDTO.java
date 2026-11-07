package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ComparativoCuadrillaDTO {

    private final Long cuadrillaId;
    private final String codigo;
    private final String nombre;
    private final String centroCosto;
    private final BigDecimal realMo;
    private final int miembros;
}
