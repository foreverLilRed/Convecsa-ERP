package com.convecsa.erp.nomina.declaracion.dto;

import com.convecsa.erp.nomina.declaracion.domain.EstadoDeclaracion;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeclaracionMensualDTO {

    private final Long id;
    private final int anio;
    private final int mes;
    private final EstadoDeclaracion estado;
    private final Instant createdAt;
    private final Instant closedAt;
    private final long cantidadAdmin;
    private final long cantidadObreros;
    private final long cantidadRxh;
    private final long cantidadRxhSinMatch;
    private final BigDecimal totalNetoRcc;
}
