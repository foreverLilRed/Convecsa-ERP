package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.ResultadoPreUsoEquipo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaPreUsoRequest {

    private Long asignacionEquipoId;
    private Long operadorPersonaId;
    private LocalDate fecha;
    private BigDecimal horometroLectura;
    private Map<String, Boolean> checklist;
    private ResultadoPreUsoEquipo resultado;
    private String observaciones;
}
