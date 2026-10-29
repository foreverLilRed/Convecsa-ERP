package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoletaCcDTO {

    private final Long id;
    private final Long periodoId;
    private final Long personaId;
    private final Long contratoId;
    private final String personaNombres;
    private final String personaApellidos;
    private final String numeroDocumento;
    private final String categoria;
    private final BigDecimal totalHaberes;
    private final BigDecimal totalDescuentos;
    private final BigDecimal totalAportesEmpleador;
    private final BigDecimal totalAfecto;
    private final BigDecimal neto;
    private final Instant createdAt;
    private final List<BoletaCcConceptoDTO> conceptos;
}
