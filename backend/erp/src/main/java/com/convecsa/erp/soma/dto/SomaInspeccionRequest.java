package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.TipoInspeccionSoma;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaInspeccionRequest {

    private TipoInspeccionSoma tipo;
    private LocalDate fecha;
    private String areaFrente;
    private Long subcontratoId;
    private Long inspectorPersonaId;
    private BigDecimal puntajeCumplimiento;
    private String observaciones;
}
