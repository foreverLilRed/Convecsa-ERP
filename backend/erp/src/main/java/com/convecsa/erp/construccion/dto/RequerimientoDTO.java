package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.EstadoRequerimientoObra;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequerimientoDTO {

    private final Long id;
    private final Long proyectoId;
    private final int numero;
    private final EstadoRequerimientoObra estado;
    private final LocalDate fecha;
    private final String observaciones;
    private final Instant createdAt;
    private final List<RequerimientoItemDTO> items;
}
