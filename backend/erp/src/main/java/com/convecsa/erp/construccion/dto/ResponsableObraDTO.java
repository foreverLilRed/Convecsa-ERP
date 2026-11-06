package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.RolResponsableObra;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponsableObraDTO {

    private final Long id;
    private final Long personaId;
    private final String nombres;
    private final String apellidos;
    private final String numeroDocumento;
    private final RolResponsableObra rol;
}
