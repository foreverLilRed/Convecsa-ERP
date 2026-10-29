package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProyectoDTO {

    private final Long id;
    private final String codigo;
    private final String nombre;
    private final String ubicacion;
    private final String centroCosto;
    private final BigDecimal valorObra;
    private final boolean activo;
    private final boolean alertaUit;
    private final int uitAnio;
}
