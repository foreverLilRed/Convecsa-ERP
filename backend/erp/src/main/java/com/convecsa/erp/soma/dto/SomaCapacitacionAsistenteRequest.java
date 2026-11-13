package com.convecsa.erp.soma.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaCapacitacionAsistenteRequest {

    private Long personaId;
    private BigDecimal nota;
    private Boolean aprobado;
}
