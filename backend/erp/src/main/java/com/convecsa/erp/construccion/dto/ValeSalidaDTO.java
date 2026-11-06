package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ValeSalidaDTO {

    private final Long id;
    private final Long almacenId;
    private final int numero;
    private final LocalDate fecha;
    private final Long partidaId;
    private final String codigoPartida;
    private final String descripcionPartida;
    private final String centroCosto;
    private final String observaciones;
    private final Instant createdAt;
    private final BigDecimal costoTotal;
    private final List<ValeSalidaItemDTO> items;
}
