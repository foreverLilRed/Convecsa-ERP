package com.convecsa.erp.construccion.dto;

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
    private final boolean activo;
}
