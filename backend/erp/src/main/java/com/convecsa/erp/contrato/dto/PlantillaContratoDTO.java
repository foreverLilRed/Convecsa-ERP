package com.convecsa.erp.contrato.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlantillaContratoDTO {

    private final Long id;
    private final String codigo;
    private final String nombre;
    private final List<PlantillaCampoDTO> metadata;
}
