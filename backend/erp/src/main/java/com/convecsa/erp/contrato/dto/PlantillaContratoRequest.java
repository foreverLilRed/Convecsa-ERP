package com.convecsa.erp.contrato.dto;

import com.convecsa.erp.contrato.domain.UsoPlantilla;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlantillaContratoRequest {

    @NotBlank
    @Size(max = 40)
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Solo letras, números, guion y guion bajo")
    private String codigo;

    @NotBlank
    @Size(max = 160)
    private String nombre;

    @NotBlank
    private String plantilla;

    private UsoPlantilla uso;
}
