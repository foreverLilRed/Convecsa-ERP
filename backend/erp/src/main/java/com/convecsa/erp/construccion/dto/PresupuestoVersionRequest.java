package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.TipoVersionPresupuesto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PresupuestoVersionRequest {

    private TipoVersionPresupuesto tipo;
}
