package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.TipoTrabajoPetar;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaPetarFaltanteTareoDTO {

    private final LocalDate fecha;
    private final TipoTrabajoPetar tipoTrabajo;
}
