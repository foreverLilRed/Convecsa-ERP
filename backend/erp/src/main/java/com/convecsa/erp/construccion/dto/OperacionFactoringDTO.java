package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.EstadoFactoring;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OperacionFactoringDTO {

    private final Long id;
    private final Long proyectoId;
    private final Long facturaId;
    private final int facturaNumero;
    private final String entidadRazon;
    private final String entidadRuc;
    private final LocalDate fechaCesion;
    private final BigDecimal tasaDescuento;
    private final BigDecimal comision;
    private final BigDecimal igvComision;
    private final BigDecimal importeNominal;
    private final BigDecimal descuento;
    private final BigDecimal importeNeto;
    private final LocalDate fechaPagoEsperada;
    private final LocalDate fechaAdelanto;
    private final EstadoFactoring estado;
    private final Instant createdAt;
}
