package com.convecsa.erp.contrato.dto;

import com.convecsa.erp.contrato.domain.UsoPlantilla;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlantillaContratoDetalleDTO {

    private final Long id;
    private final String codigo;
    private final String nombre;
    private final UsoPlantilla uso;
    private final String plantilla;
    private final List<PlantillaCampoDTO> metadata;
}
