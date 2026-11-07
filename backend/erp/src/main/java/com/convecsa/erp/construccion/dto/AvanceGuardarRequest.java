package com.convecsa.erp.construccion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvanceGuardarRequest {

    @NotNull
    private Integer anio;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer mes;

    @Valid
    private List<AvancePartidaRequest> items = new ArrayList<>();
}
