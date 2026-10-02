package com.convecsa.erp.contrato.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CatalogoItemDTO {

    private final Long id;
    private final String codigo;
    private final String label;
}
