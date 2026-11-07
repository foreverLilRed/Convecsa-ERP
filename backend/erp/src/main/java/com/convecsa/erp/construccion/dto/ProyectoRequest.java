package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.EstadoObra;
import com.convecsa.erp.construccion.domain.NivelRiesgoObra;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProyectoRequest {

    @NotBlank
    @Size(max = 20)
    private String codigo;

    @NotBlank
    @Size(max = 160)
    private String nombre;

    @Size(max = 120)
    private String ubicacion;

    @Size(max = 40)
    private String centroCosto;

    @DecimalMin(value = "0.00")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal valorObra;

    private NivelRiesgoObra nivelRiesgo;

    @DecimalMin(value = "0.00")
    @Digits(integer = 1, fraction = 6)
    private BigDecimal tasaSctrSalud;

    @DecimalMin(value = "0.00")
    @Digits(integer = 1, fraction = 6)
    private BigDecimal tasaSctrPension;

    @Size(max = 160)
    private String clienteRazon;

    @Size(max = 11)
    private String clienteRuc;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private EstadoObra estadoObra;

    @DecimalMin(value = "0.00")
    @Digits(integer = 3, fraction = 2)
    @DecimalMax("100.00")
    private BigDecimal porcentajeRetencion;

    private Boolean activo;

    @AssertTrue(message = "La fecha de fin no puede ser menor que la de inicio")
    public boolean isVigenciaValida() {
        return fechaInicio == null || fechaFin == null || !fechaFin.isBefore(fechaInicio);
    }
}
