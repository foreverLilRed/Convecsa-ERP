package com.convecsa.erp.soma.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaEntregaEppItemRequest {

    private Long materialId;
    private BigDecimal cantidad;
    private String talla;
}
