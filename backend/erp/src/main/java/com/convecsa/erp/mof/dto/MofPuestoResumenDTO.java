package com.convecsa.erp.mof.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MofPuestoResumenDTO {

    private final Long id;
    private final String codigo;
    private final String cargo;
    private final String grupo;
}
