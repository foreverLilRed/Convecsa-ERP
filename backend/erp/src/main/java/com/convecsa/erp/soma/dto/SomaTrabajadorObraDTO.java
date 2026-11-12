package com.convecsa.erp.soma.dto;

import com.convecsa.erp.construccion.domain.CategoriaObrera;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaTrabajadorObraDTO {

    private final Long personaId;
    private final String nombres;
    private final String apellidos;
    private final String numeroDocumento;
    private final CategoriaObrera categoria;
    private final LocalDate fechaIngreso;
    private final LocalDate fechaCese;
}
