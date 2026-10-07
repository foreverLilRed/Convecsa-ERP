package com.convecsa.erp.asistencia.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsistenciaConfigRequest {

    @NotEmpty
    private List<Integer> diasSemana;
}
