package com.convecsa.erp.contrato.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdendaDTO {

    private final Long id;
    private final int numero;
    private final Long contratoId;
    private final Long personaId;
    private final String personaNombres;
    private final String personaApellidos;
    private final String numeroDocumento;
    private final LocalDate contratoFechaInicio;
    private final LocalDate fechaFinAnterior;
    private final LocalDate fechaFinNueva;
    private final BigDecimal sueldo;
    private final String motivo;
    private final String ciudadEmision;
    private final LocalDate fechaEmision;
    private final String plantillaCodigo;
    private final String plantillaNombre;
    private final Instant createdAt;
}
