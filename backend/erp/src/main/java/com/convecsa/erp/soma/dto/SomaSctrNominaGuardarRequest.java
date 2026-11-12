package com.convecsa.erp.soma.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaSctrNominaGuardarRequest {

    private List<SomaSctrNominaFilaRequest> filas;
}
