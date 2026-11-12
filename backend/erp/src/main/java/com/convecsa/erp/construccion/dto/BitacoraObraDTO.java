package com.convecsa.erp.construccion.dto;

import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BitacoraObraDTO {

    private final Long id;
    private final Long proyectoId;
    private final LocalDate fecha;
    private final String texto;
    private final String usuario;
    private final Instant createdAt;
}
