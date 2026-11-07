package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.CategoriaObrera;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CuadrillaMiembroDTO {

    private final Long personaId;
    private final String nombres;
    private final String apellidos;
    private final String numeroDocumento;
    private final CategoriaObrera categoria;
}
