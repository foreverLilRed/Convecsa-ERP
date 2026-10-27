package com.convecsa.erp.obra.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObraRequest {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 40, message = "El código no puede superar 40 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    private String nombre;

    @NotBlank(message = "El cliente es obligatorio")
    @Size(max = 200, message = "El cliente no puede superar 200 caracteres")
    private String clienteNombre;

    @Size(max = 200, message = "El contratista principal no puede superar 200 caracteres")
    private String contratistaPrincipal;

    @Size(max = 300, message = "La ubicación no puede superar 300 caracteres")
    private String ubicacion;

    private LocalDate fechaInicioPlanificada;

    private LocalDate fechaFinPlanificada;

    private LocalDate fechaInicioReal;

    private LocalDate fechaFinReal;

    @DecimalMin(value = "0", inclusive = true, message = "El monto contratado no puede ser negativo")
    private BigDecimal montoContratado;

    @Size(max = 3, message = "La moneda no puede superar 3 caracteres")
    private String moneda;

    @Size(max = 200, message = "El responsable no puede superar 200 caracteres")
    private String responsableNombre;
}
