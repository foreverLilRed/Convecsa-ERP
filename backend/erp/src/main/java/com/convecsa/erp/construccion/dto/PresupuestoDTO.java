package com.convecsa.erp.construccion.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PresupuestoDTO {

    private final List<PresupuestoVersionResumenDTO> versiones;
    private final PresupuestoVersionDTO version;
}
