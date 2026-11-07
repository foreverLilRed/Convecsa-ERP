package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.EstadoObra;
import com.convecsa.erp.construccion.domain.NivelRiesgoObra;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProyectoDTO {

    private final Long id;
    private final String codigo;
    private final String nombre;
    private final String ubicacion;
    private final String centroCosto;
    private final BigDecimal valorObra;
    private final NivelRiesgoObra nivelRiesgo;
    private final BigDecimal tasaSctrSalud;
    private final BigDecimal tasaSctrPension;
    private final String clienteRazon;
    private final String clienteRuc;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final EstadoObra estadoObra;
    private final BigDecimal porcentajeRetencion;
    private final boolean activo;
    private final boolean alertaUit;
    private final int uitAnio;
}
