package com.convecsa.erp.mof.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MofPuestoRequest {

    @NotBlank
    @Size(max = 8000)
    private String proposito;

    @NotNull
    private List<@NotBlank @Size(max = 2000) String> funciones = new ArrayList<>();
}
