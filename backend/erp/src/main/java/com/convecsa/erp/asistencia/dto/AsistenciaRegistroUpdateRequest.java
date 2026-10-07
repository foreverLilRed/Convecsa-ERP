package com.convecsa.erp.asistencia.dto;

import com.convecsa.erp.asistencia.domain.EstadoAsistencia;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsistenciaRegistroUpdateRequest {

    @NotNull
    private Long id;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("24.00")
    private BigDecimal horasTrabajadas;

    @NotNull
    private EstadoAsistencia estado;

    private String observacion;
}
