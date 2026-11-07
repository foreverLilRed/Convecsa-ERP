package com.convecsa.erp.construccion.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CronogramaPartidaRequest {

    @NotNull
    private Long partidaId;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;
}
