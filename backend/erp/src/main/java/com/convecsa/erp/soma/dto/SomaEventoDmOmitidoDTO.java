package com.convecsa.erp.soma.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaEventoDmOmitidoDTO {

    private final LocalDate fecha;
    private final String motivo;
}
