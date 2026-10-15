package com.convecsa.erp.asistencia.dto;

import com.convecsa.erp.asistencia.domain.TipoConceptoVariableTareo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsistenciaConceptoVariableRequest {

    @NotNull
    private Long personaId;

    @NotNull
    private Long contratoId;

    @NotNull
    private TipoConceptoVariableTareo tipo;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal monto;

    @Size(max = 300)
    private String detalle;
}
