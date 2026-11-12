package com.convecsa.erp.soma.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaResumenObraDTO {

    private final Long proyectoId;
    private final String codigoObra;
    private final String nombreObra;
    private final String supervisorSsoma;
}
