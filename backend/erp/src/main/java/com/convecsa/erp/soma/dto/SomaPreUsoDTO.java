package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.ResultadoPreUsoEquipo;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaPreUsoDTO {

    private final Long id;
    private final Long proyectoId;
    private final Long asignacionEquipoId;
    private final String equipoCodigo;
    private final String equipoDescripcion;
    private final Long operadorPersonaId;
    private final String operadorNombre;
    private final String operadorDocumento;
    private final LocalDate fecha;
    private final BigDecimal horometroLectura;
    private final Map<String, Boolean> checklist;
    private final ResultadoPreUsoEquipo resultado;
    private final String observaciones;
    private final Instant createdAt;
}
