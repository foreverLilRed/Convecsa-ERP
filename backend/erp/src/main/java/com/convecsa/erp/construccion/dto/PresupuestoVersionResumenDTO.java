package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.EstadoVersionPresupuesto;
import com.convecsa.erp.construccion.domain.TipoVersionPresupuesto;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PresupuestoVersionResumenDTO {

    private final Long id;
    private final TipoVersionPresupuesto tipo;
    private final EstadoVersionPresupuesto estado;
    private final int numero;
    private final Instant createdAt;
    private final int cantidadPartidas;
}
