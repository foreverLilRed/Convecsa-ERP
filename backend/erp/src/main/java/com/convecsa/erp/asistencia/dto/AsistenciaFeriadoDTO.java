package com.convecsa.erp.asistencia.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AsistenciaFeriadoDTO {

    private final Long id;
    private final LocalDate fecha;
    private final String nombre;
    private final boolean laborable;
}
