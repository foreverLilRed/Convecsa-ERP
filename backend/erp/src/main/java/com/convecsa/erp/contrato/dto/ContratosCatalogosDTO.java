package com.convecsa.erp.contrato.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContratosCatalogosDTO {

    private final List<CatalogoItemDTO> jornadas;
    private final List<CatalogoItemDTO> causasObjetivas;
    private final List<CatalogoItemDTO> tiposPlantilla;
    private final List<CatalogoItemDTO> metodosPago;
}
