package com.convecsa.erp.contrato.dto;

import com.convecsa.erp.contrato.domain.UsoPlantilla;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlantillaContratoUpdateRequest {

    @NotBlank
    @Size(max = 160)
    private String nombre;

    @NotBlank
    private String plantilla;

    private UsoPlantilla uso;

    @NotBlank(message = "Ingrese el PIN de superusuario")
    private String pin;
}
