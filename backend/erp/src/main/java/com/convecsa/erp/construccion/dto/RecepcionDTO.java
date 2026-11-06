package com.convecsa.erp.construccion.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecepcionDTO {

    private final Long id;
    private final String numeroGuia;
    private final LocalDate fecha;
    private final String observaciones;
    private final Instant createdAt;
    private final List<RecepcionItemDTO> items;
}
