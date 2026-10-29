package com.convecsa.erp.contrato.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ModalidadContratoDTO {

    private final Long id;
    private final String codigo;
    private final String label;
    private final Long regimenLaboralId;
    private final boolean requiereFechaFin;
}
