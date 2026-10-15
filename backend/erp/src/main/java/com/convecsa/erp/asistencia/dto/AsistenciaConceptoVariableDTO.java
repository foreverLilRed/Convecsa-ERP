package com.convecsa.erp.asistencia.dto;

import com.convecsa.erp.asistencia.domain.TipoConceptoVariableTareo;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AsistenciaConceptoVariableDTO {

    private final Long id;
    private final Long personaId;
    private final Long contratoId;
    private final TipoConceptoVariableTareo tipo;
    private final BigDecimal monto;
    private final String detalle;
}
