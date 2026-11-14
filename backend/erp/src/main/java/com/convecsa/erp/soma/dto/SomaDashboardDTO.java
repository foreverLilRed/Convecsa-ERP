package com.convecsa.erp.soma.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaDashboardDTO {

    private final SomaEstadisticaAnioDTO estadistica;
    private final int hallazgosAbiertos;
    private final int hallazgosLevantados;
    private final List<SomaDashboardObraAlertaDTO> obrasConAlertasCriticas;
}
