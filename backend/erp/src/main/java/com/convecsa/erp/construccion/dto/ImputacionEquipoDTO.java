package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImputacionEquipoDTO {

    private final Long id;
    private final Long asignacionId;
    private final Long proyectoId;
    private final LocalDate fecha;
    private final BigDecimal cantidad;
    private final BigDecimal tarifa;
    private final BigDecimal costoTotal;
    private final Long partidaId;
    private final String codigoPartida;
    private final String descripcionPartida;
    private final String centroCosto;
    private final String observaciones;
    private final Instant createdAt;
}
