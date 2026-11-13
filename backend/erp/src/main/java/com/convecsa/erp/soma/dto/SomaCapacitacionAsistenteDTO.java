package com.convecsa.erp.soma.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaCapacitacionAsistenteDTO {

    private final Long id;
    private final Long personaId;
    private final String personaNombre;
    private final String numeroDocumento;
    private final BigDecimal nota;
    private final boolean aprobado;
}
