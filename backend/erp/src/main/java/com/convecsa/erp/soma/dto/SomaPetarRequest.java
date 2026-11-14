package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.TipoTrabajoPetar;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaPetarRequest {

    private TipoTrabajoPetar tipoTrabajo;
    private Long partidaId;
    private Long cuadrillaId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String ubicacionFrente;
    private String descripcionTrabajo;
    private Map<String, Boolean> checklist;
    private Map<String, BigDecimal> mediciones;
    private Long residentePersonaId;
    private Long supervisorSsomaId;
    private Long documentoObraId;
    private List<SomaPetarTrabajadorRequest> trabajadores;
}
