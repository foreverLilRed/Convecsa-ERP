package com.convecsa.erp.nomina.declaracion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RxhImportacionDTO {

    private final int importados;
    private final int conMatch;
    private final int huerfanos;
}
