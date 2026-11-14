package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.CargoComiteSoma;
import com.convecsa.erp.soma.domain.RepresentacionComiteSoma;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaComiteMiembroRequest {

    private Long personaId;
    private RepresentacionComiteSoma representacion;
    private CargoComiteSoma cargoComite;
    private Boolean titular;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}
