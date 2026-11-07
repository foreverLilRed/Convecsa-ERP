package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.EstadoAvanceObra;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvanceObraDTO {

    private final Long id;
    private final Long proyectoId;
    private final int anio;
    private final int mes;
    private final EstadoAvanceObra estado;
    private final Instant createdAt;
    private final Instant closedAt;
    private final List<AvancePartidaDTO> items;
}
