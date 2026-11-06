package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.RolResponsableObra;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProyectoResponsableRequest {

    @NotNull
    private Long personaId;

    @NotNull
    private RolResponsableObra rol;
}
