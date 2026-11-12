package com.convecsa.erp.soma.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaSctrPolizaRequest {

    private Integer anio;
    private Integer mes;
    private String aseguradoraSalud;
    private String numeroPolizaSalud;
    private String aseguradoraPension;
    private String numeroPolizaPension;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal montoPrimaSalud;
    private BigDecimal montoPrimaPension;
    private Long documentoObraId;
}
