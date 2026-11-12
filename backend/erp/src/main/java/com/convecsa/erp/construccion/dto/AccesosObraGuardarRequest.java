package com.convecsa.erp.construccion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccesosObraGuardarRequest {

    @NotNull
    @Valid
    private List<AccesoObraRequest> accesos = new ArrayList<>();
}
