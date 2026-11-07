package com.convecsa.erp.construccion.dto;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CuadrillaObraDTO {

    private final Long id;
    private final Long proyectoId;
    private final String codigo;
    private final String nombre;
    private final String centroCosto;
    private final String observaciones;
    private final Instant createdAt;
    private final List<CuadrillaMiembroDTO> miembros;
}
