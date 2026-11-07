package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.TipoLiberacionRetencion;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LiberacionRetencionDTO {

    private final Long id;
    private final Long proyectoId;
    private final TipoLiberacionRetencion tipo;
    private final LocalDate fecha;
    private final BigDecimal importe;
    private final String entidad;
    private final String numeroDocumento;
    private final String observaciones;
    private final Instant createdAt;
}
