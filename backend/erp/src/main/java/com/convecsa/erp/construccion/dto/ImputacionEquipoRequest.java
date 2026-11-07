package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImputacionEquipoRequest {

    private LocalDate fecha;
    private BigDecimal cantidad;
    private Long partidaId;
    private String observaciones;
}
