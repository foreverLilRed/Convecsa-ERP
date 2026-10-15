package com.convecsa.erp.asistencia.dto;

import com.convecsa.erp.asistencia.domain.EstadoAsistencia;
import com.convecsa.erp.asistencia.domain.CodigoTareo;
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

    @NotNull
    private CodigoTareo codigo;

    @DecimalMin("0")
    private Integer minutosExtras25 = 0;

    @DecimalMin("0")
    private Integer minutosExtras35 = 0;

    @DecimalMin("0")
    private Integer minutosTardanza = 0;

    private String observacion;
}
