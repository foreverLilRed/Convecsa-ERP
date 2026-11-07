package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.ModoTarifaEquipo;
import com.convecsa.erp.construccion.domain.OrigenEquipo;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AsignacionEquipoDTO {

    private final Long id;
    private final Long proyectoId;
    private final String codigo;
    private final String descripcion;
    private final OrigenEquipo origen;
    private final String proveedorRazon;
    private final String proveedorRuc;
    private final LocalDate fechaIngreso;
    private final LocalDate fechaSalida;
    private final ModoTarifaEquipo modoTarifa;
    private final BigDecimal tarifa;
    private final Long partidaId;
    private final String codigoPartida;
    private final String descripcionPartida;
    private final String centroCosto;
    private final String observaciones;
    private final Instant createdAt;
    private final BigDecimal costoImputado;
    private final BigDecimal cantidadImputada;
}
