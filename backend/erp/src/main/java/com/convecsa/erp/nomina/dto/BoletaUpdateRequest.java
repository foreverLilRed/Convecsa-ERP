package com.convecsa.erp.nomina.dto;

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
public class BoletaUpdateRequest {

    @NotNull
    @Min(0)
    @Max(30)
    private Integer diasTrabajados;

    @NotNull
    private Boolean aplicaAsigFamiliar;

    @NotNull
    private Long afpId;

    @Valid
    private List<BoletaConceptoExtraRequest> extras = new ArrayList<>();
}
