package com.convecsa.erp.asistencia.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AsistenciaConfigDTO {

    private final List<Integer> diasSemana;
}
