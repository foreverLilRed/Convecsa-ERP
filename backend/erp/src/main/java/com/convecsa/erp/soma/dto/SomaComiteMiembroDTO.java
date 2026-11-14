package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.CargoComiteSoma;
import com.convecsa.erp.soma.domain.RepresentacionComiteSoma;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaComiteMiembroDTO {

    private final Long id;
    private final Long proyectoId;
    private final Long personaId;
    private final String personaNombre;
    private final RepresentacionComiteSoma representacion;
    private final CargoComiteSoma cargoComite;
    private final boolean titular;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final boolean activo;
}
