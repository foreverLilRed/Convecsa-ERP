package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.MotivoEntregaEpp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaEntregaEppDTO {

    private final Long id;
    private final Long proyectoId;
    private final Long personaId;
    private final String personaNombre;
    private final String numeroDocumento;
    private final Long valeSalidaId;
    private final LocalDate fechaEntrega;
    private final MotivoEntregaEpp motivo;
    private final boolean firmadoTrabajador;
    private final String entregadoPor;
    private final String observaciones;
    private final Instant createdAt;
    private final List<SomaEntregaEppItemDTO> items;
}
