package com.convecsa.erp.construccion.dto;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuditoriaObraDTO {

    private final Long id;
    private final Long proyectoId;
    private final String accion;
    private final String entidad;
    private final Long entidadId;
    private final String detalle;
    private final String usuario;
    private final Instant createdAt;
}
