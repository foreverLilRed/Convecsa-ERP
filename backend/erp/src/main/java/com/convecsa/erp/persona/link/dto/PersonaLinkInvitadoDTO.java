package com.convecsa.erp.persona.link.dto;

import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.persona.link.domain.TipoLinkInvitado;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PersonaLinkInvitadoDTO {

    private final Long id;
    private final TipoLinkInvitado tipo;
    private final Long personaId;
    private final String apellidos;
    private final String nombres;
    private final TipoDocumento tipoDocumento;
    private final String numeroDocumento;
    private final String token;
    private final Instant creadoEn;
    private final Instant expiraEn;
}
