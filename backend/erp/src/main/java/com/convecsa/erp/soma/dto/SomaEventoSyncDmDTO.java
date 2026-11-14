package com.convecsa.erp.soma.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaEventoSyncDmDTO {

    private final List<LocalDate> aplicados;
    private final List<SomaEventoDmOmitidoDTO> omitidos;
}
