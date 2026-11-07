package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AsientoObraDTO {

    private final LocalDate desde;
    private final LocalDate hasta;
    private final Instant generadoAt;
    private final List<AsientoObraBloqueDTO> bloques;
    private final BigDecimal totalDebe;
    private final BigDecimal totalHaber;
    private final BigDecimal diferencia;
}
