package com.convecsa.erp.contrato.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlantillaCampoDTO {

    private final String clave;
    private final String label;
    private final String tipo;
    private final String catalogo;
    private final boolean requerido;
}
