package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoletaCcResumenDTO {

    private final Long id;
    private final Long personaId;
    private final String personaNombreCompleto;
    private final String numeroDocumento;
    private final String categoria;
    private final BigDecimal totalHaberes;
    private final BigDecimal totalDescuentos;
    private final BigDecimal neto;
}
