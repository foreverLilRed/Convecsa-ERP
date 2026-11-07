package com.convecsa.erp.construccion.dto;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CronogramaGuardarRequest {

    @Valid
    private List<CronogramaPartidaRequest> partidas = new ArrayList<>();
}
