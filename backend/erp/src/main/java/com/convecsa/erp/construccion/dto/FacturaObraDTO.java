package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.EstadoFacturaObra;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FacturaObraDTO {

    private final Long id;
    private final Long valorizacionId;
    private final int numero;
    private final LocalDate fecha;
    private final LocalDate fechaVencimiento;
    private final String clienteRazon;
    private final String clienteRuc;
    private final BigDecimal subtotal;
    private final BigDecimal igv;
    private final BigDecimal total;
    private final BigDecimal saldo;
    private final EstadoFacturaObra estado;
    private final Instant createdAt;
    private final java.util.List<CobranzaObraDTO> cobranzas;
    private final OperacionFactoringDTO factoring;
}
