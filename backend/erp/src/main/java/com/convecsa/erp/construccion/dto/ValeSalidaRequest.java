package com.convecsa.erp.construccion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValeSalidaRequest {

    private LocalDate fecha;

    @NotNull
    private Long partidaId;

    private String observaciones;

    @Valid
    @NotEmpty
    private List<ValeSalidaItemRequest> items = new ArrayList<>();
}
