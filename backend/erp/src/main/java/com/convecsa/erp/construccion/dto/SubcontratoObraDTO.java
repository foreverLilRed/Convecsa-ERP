package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubcontratoObraDTO {

    private final Long id;
    private final Long proyectoId;
    private final String especialidad;
    private final String subcontratistaRazon;
    private final String subcontratistaRuc;
    private final BigDecimal montoContratado;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final Long partidaId;
    private final String codigoPartida;
    private final String descripcionPartida;
    private final String centroCosto;
    private final String observaciones;
    private final Instant createdAt;
    private final BigDecimal montoAvanzado;
    private final BigDecimal saldo;
}
