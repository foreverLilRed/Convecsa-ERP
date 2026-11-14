package com.convecsa.erp.soma.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaPreUsoBloqueoDTO {

    private final Long asignacionId;
    private final String codigo;
    private final String descripcion;
}
