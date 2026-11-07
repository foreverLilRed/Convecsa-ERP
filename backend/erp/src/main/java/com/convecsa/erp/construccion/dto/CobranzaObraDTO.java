package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CobranzaObraDTO {

    private final Long id;
    private final Long proyectoId;
    private final Long facturaId;
    private final int facturaNumero;
    private final LocalDate fecha;
    private final String banco;
    private final String numeroOperacion;
    private final BigDecimal importe;
    private final BigDecimal detraccion;
    private final BigDecimal aplicado;
    private final String observaciones;
    private final Instant createdAt;
}
