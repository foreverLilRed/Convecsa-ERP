package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.PasoAts;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaCharlaAtsRequest {

    private Long cuadrillaId;
    private Long partidaId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private Integer duracionMinutos;
    private String temaCharla;
    private Long expositorPersonaId;
    private Long supervisorSsomaId;
    private List<PasoAts> pasos;
    private Boolean eppVerificado;
    private Boolean herramientasVerif;
    private String observaciones;
    private List<SomaCharlaAsistenteRequest> asistentes;
}
