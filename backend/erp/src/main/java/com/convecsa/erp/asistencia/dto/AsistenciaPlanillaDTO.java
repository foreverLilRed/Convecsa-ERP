package com.convecsa.erp.asistencia.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AsistenciaPlanillaDTO {

    private final int anio;
    private final int mes;
    private final AsistenciaConfigDTO config;
    private final List<AsistenciaFeriadoDTO> feriados;
    private final List<LocalDate> diasLaborables;
    private final boolean generado;
    private final List<AsistenciaFilaDTO> filas;
}
