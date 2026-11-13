package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.TipoCapacitacionSoma;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaCapacitacionDTO {

    private final Long id;
    private final Long proyectoId;
    private final TipoCapacitacionSoma tipo;
    private final String tema;
    private final LocalDate fecha;
    private final BigDecimal horasDuracion;
    private final String expositorNombre;
    private final String expositorEntidad;
    private final BigDecimal puntajeSimulacro;
    private final String observaciones;
    private final Long evidenciaDocId;
    private final Instant createdAt;
    private final List<SomaCapacitacionAsistenteDTO> asistentes;
}
