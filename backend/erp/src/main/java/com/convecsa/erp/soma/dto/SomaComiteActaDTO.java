package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.TipoSesionComiteSoma;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaComiteActaDTO {

    private final Long id;
    private final Long proyectoId;
    private final String numeroActa;
    private final TipoSesionComiteSoma tipoSesion;
    private final LocalDate fecha;
    private final String agenda;
    private final String acuerdos;
    private final Long actaDocId;
    private final Instant createdAt;
}
