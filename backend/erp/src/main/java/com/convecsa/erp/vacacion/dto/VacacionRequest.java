package com.convecsa.erp.vacacion.dto;

import com.convecsa.erp.vacacion.domain.TipoVacacion;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VacacionRequest {

    @NotNull(message = "La persona es obligatoria")
    private Long personaId;

    @NotNull(message = "El tipo de vacaciones es obligatorio")
    private TipoVacacion tipo;

    @NotNull(message = "El inicio del periodo vacacional es obligatorio")
    private LocalDate periodoInicio;

    @NotNull(message = "El fin del periodo vacacional es obligatorio")
    private LocalDate periodoFin;

    @Min(value = 1, message = "Los días de derecho deben ser al menos 1")
    @Max(value = 60, message = "Los días de derecho no pueden superar 60")
    private int diasDerechoPeriodo = 30;

    @Min(value = 1, message = "Debe solicitar al menos un día")
    @Max(value = 30, message = "No puede solicitar más de 30 días")
    private int diasSolicitados;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @Size(max = 2000, message = "La observación no puede superar 2000 caracteres")
    private String observacion;
}
