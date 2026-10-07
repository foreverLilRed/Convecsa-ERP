package com.convecsa.erp.contrato.dto;

import com.convecsa.erp.contrato.domain.EstadoContrato;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContratoDTO {

    private final Long id;
    private final Long personaId;
    private final String personaNombres;
    private final String personaApellidos;
    private final String numeroDocumento;
    private final EstadoContrato estado;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final String plantillaCodigo;
    private final String plantillaNombre;
    private final Instant createdAt;
    private final BigDecimal sueldo;
    private final Long afpId;
    private final String afpCodigo;
    private final String afpNombre;
    private final boolean tieneAsignacionFamiliar;
}
