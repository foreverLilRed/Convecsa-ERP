package com.convecsa.erp.soma.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaPetarChecklistItemDTO {

    private final String clave;
    private final String etiqueta;
}
