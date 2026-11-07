package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubcontratoAvanceRequest {

    private LocalDate fecha;
    private BigDecimal monto;
    private Long partidaId;
    private String observaciones;
}
