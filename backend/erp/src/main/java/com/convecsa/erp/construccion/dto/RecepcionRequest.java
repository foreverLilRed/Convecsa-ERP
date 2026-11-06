package com.convecsa.erp.construccion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecepcionRequest {

    private String numeroGuia;

    private LocalDate fecha;

    private String observaciones;

    @Valid
    @NotEmpty
    private List<RecepcionItemRequest> items = new ArrayList<>();
}
