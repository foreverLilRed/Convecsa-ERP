package com.convecsa.erp.soma.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaDashboardObraAlertaDTO {

    private final Long proyectoId;
    private final String codigo;
    private final String nombre;
}
