package com.convecsa.erp.nomina.dto;

import com.convecsa.erp.nomina.domain.EstadoBoletaPago;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoletaPagoDTO {

    private final Long id;
    private final Long periodoId;
    private final int periodoAnio;
    private final int periodoMes;
    private final Long personaId;
    private final String personaNombres;
    private final String personaApellidos;
    private final String numeroDocumento;
    private final String cargo;
    private final String banco;
    private final String cci;
    private final Long afpId;
    private final String afpCodigo;
    private final String afpNombre;
    private final int diasTrabajados;
    private final boolean aplicaAsigFamiliar;
    private final BigDecimal totalHaberes;
    private final BigDecimal totalDescuentos;
    private final BigDecimal totalAportesEmpleador;
    private final BigDecimal neto;
    private final EstadoBoletaPago estado;
    private final List<BoletaConceptoDTO> conceptos;
    private final Instant createdAt;
    private final Instant updatedAt;
}
