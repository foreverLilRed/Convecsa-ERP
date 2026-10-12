package com.convecsa.erp.persona.link.dto;

import com.convecsa.erp.persona.domain.TipoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonaLinkAltaRequest {

    @NotBlank
    @Size(max = 120)
    private String apellidos;

    @NotBlank
    @Size(max = 120)
    private String nombres;

    @NotNull
    private TipoDocumento tipoDocumento;

    @NotBlank
    @Size(max = 30)
    private String numeroDocumento;
}
