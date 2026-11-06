package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.CategoriaObrera;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PersonalObraDTO {

    private final Long personaId;
    private final Long contratoId;
    private final String nombres;
    private final String apellidos;
    private final String numeroDocumento;
    private final CategoriaObrera categoria;
    private final String especialidadBae;
    private final int hijosEscolaridad;
    private final boolean afiliadoSindical;
    private final String centroCosto;
    private final LocalDate fechaIngreso;
    private final LocalDate fechaCese;
}
