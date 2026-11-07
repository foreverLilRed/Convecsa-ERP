package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CronogramaPartidaDTO {

    private final Long partidaId;
    private final String codigo;
    private final String descripcion;
    private final BigDecimal metradoMeta;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final BigDecimal desvioPlazoPct;
}
