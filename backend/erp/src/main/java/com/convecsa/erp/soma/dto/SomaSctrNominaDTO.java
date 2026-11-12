package com.convecsa.erp.soma.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaSctrNominaDTO {

    private final Long personaId;
    private final String personaNombre;
    private final String numeroDocumento;
    private final boolean incluidoSalud;
    private final boolean incluidoPension;
}
