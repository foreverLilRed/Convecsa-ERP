package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.EstadoHabilitacionSoma;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaHabilitacionFilaDTO {

    private final Long personaId;
    private final String nombres;
    private final String apellidos;
    private final String numeroDocumento;
    private final EstadoHabilitacionSoma estado;
    private final List<String> motivos;
    private final String restricciones;
}
