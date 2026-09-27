package com.convecsa.erp.persona.dto;

import com.convecsa.erp.persona.domain.GradoInstruccion;
import com.convecsa.erp.persona.domain.TipoDocumento;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PersonaResumenDTO {

    private final Long id;
    private final String apellidos;
    private final String nombres;
    private final TipoDocumento tipoDocumento;
    private final String numeroDocumento;
    private final String correo;
    private final GradoInstruccion gradoInstruccion;
    private final String departamento;
    private final String distrito;
    private final Instant createdAt;
}
