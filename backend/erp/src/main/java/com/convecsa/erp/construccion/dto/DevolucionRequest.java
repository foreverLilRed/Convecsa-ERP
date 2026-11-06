package com.convecsa.erp.construccion.dto;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DevolucionRequest {

    private LocalDate fecha;

    private String observaciones;

    @Valid
    private List<DevolucionItemRequest> items = new ArrayList<>();
}
