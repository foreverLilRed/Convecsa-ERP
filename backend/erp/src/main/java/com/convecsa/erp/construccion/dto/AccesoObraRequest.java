package com.convecsa.erp.construccion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccesoObraRequest {

    @NotNull
    private Long usuarioId;
}
