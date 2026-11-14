package com.convecsa.erp.soma.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaCharlaAsistenteDTO {

    private final Long id;
    private final Long personaId;
    private final String personaNombre;
    private final String numeroDocumento;
    private final boolean habilitadoSoma;
    private final String observacion;
}
