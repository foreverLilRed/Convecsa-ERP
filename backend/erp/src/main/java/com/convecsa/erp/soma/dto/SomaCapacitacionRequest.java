package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.TipoCapacitacionSoma;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaCapacitacionRequest {

    private TipoCapacitacionSoma tipo;
    private String tema;
    private LocalDate fecha;
    private BigDecimal horasDuracion;
    private String expositorNombre;
    private String expositorEntidad;
    private BigDecimal puntajeSimulacro;
    private String observaciones;
    private Long evidenciaDocId;
    private List<SomaCapacitacionAsistenteRequest> asistentes;
}
