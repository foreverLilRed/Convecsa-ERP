package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.EstadoSctrPoliza;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaSctrPolizaDTO {

    private final Long id;
    private final Long proyectoId;
    private final int anio;
    private final int mes;
    private final String aseguradoraSalud;
    private final String numeroPolizaSalud;
    private final String aseguradoraPension;
    private final String numeroPolizaPension;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final BigDecimal montoPrimaSalud;
    private final BigDecimal montoPrimaPension;
    private final Long documentoObraId;
    private final EstadoSctrPoliza estado;
    private final Instant createdAt;
    private final List<SomaSctrNominaDTO> nomina;
}
