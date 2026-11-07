package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.ModoTarifaEquipo;
import com.convecsa.erp.construccion.domain.OrigenEquipo;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsignacionEquipoRequest {

    private String codigo;
    private String descripcion;
    private OrigenEquipo origen;
    private String proveedorRazon;
    private String proveedorRuc;
    private LocalDate fechaIngreso;
    private LocalDate fechaSalida;
    private ModoTarifaEquipo modoTarifa;
    private BigDecimal tarifa;
    private Long partidaId;
    private String observaciones;
}
