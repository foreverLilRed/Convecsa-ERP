package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.TipoAgenteMonitoreoSoma;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaMonitoreoDTO {

    private final Long id;
    private final Long proyectoId;
    private final LocalDate fecha;
    private final TipoAgenteMonitoreoSoma tipoAgente;
    private final String parametroMedido;
    private final String puntoMedicion;
    private final BigDecimal valorMedido;
    private final BigDecimal limitePermisible;
    private final String unidad;
    private final boolean cumpleLmp;
    private final String laboratorioNombre;
    private final Long informeDocId;
    private final Instant createdAt;
}
