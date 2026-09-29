package com.convecsa.erp.persona.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExperienciaLaboralDTO {

    private final Long id;
    private final String organizacion;
    private final String cargo;
    private final LocalDate fechaInicioLaboral;
    private final LocalDate fechaFin;
}
