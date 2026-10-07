package com.convecsa.erp.asistencia.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsistenciaPlanillaUpdateRequest {

    @NotEmpty
    @Valid
    private List<AsistenciaRegistroUpdateRequest> items;
}
