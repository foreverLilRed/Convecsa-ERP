package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubcontratoObraRequest {

    private String especialidad;
    private String subcontratistaRazon;
    private String subcontratistaRuc;
    private BigDecimal montoContratado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long partidaId;
    private String observaciones;
}
