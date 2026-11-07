package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.TipoDocumentoObra;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentoObraDTO {

    private final Long id;
    private final Long proyectoId;
    private final TipoDocumentoObra tipo;
    private final String nombreOriginal;
    private final String mime;
    private final long tamanoBytes;
    private final String observaciones;
    private final Instant createdAt;
}
