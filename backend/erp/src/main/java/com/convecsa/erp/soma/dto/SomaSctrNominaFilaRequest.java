package com.convecsa.erp.soma.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaSctrNominaFilaRequest {

    private Long personaId;
    private Boolean incluidoSalud;
    private Boolean incluidoPension;
}
