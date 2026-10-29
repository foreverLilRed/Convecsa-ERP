package com.convecsa.erp.contrato.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContratosCatalogosDTO {

    private final List<CatalogoItemDTO> regimenesLaborales;
    private final List<ModalidadContratoDTO> modalidadesContrato;
    private final List<CatalogoItemDTO> jornadas;
    private final List<CatalogoItemDTO> causasObjetivas;
    private final List<CatalogoItemDTO> metodosPago;
    private final List<PlantillaContratoDTO> plantillas;
    private final List<CatalogoItemDTO> afps;
}
