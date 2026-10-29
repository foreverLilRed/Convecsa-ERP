package com.convecsa.erp.nomina.declaracion.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReciboHonorarioAsignarRequest {

    private Long personaId;
    private String numeroDocumento;
}
