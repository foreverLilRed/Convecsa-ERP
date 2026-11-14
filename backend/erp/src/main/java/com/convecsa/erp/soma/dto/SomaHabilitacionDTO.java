package com.convecsa.erp.soma.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaHabilitacionDTO {

    private final LocalDate fecha;
    private final int habilitados;
    private final int bloqueados;
    private final int emosPorVencer30Dias;
    private final List<SomaHabilitacionFilaDTO> filas;
}
