package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.TipoInspeccionSoma;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaInspeccionDTO {

    private final Long id;
    private final Long proyectoId;
    private final TipoInspeccionSoma tipo;
    private final LocalDate fecha;
    private final String areaFrente;
    private final Long subcontratoId;
    private final String subcontratoNombre;
    private final Long inspectorPersonaId;
    private final String inspectorNombre;
    private final BigDecimal puntajeCumplimiento;
    private final String observaciones;
    private final Instant createdAt;
    private final List<SomaHallazgoDTO> hallazgos;
}
