package com.convecsa.erp.soma.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaEstadisticaAnioDTO {

    private final int anio;
    private final List<SomaEstadisticaMesDTO> meses;
    private final SomaEstadisticaMesDTO acumulado;
    private final Integer diasSinAccidente;
}
