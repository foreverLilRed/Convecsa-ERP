package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.PasoAts;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaCharlaAtsDTO {

    private final Long id;
    private final Long proyectoId;
    private final Long cuadrillaId;
    private final String cuadrillaNombre;
    private final Long partidaId;
    private final String partidaCodigo;
    private final String partidaDescripcion;
    private final LocalDate fecha;
    private final LocalTime horaInicio;
    private final int duracionMinutos;
    private final String temaCharla;
    private final Long expositorPersonaId;
    private final String expositorNombre;
    private final Long supervisorSsomaId;
    private final String supervisorSsomaNombre;
    private final List<PasoAts> pasos;
    private final boolean eppVerificado;
    private final boolean herramientasVerif;
    private final String observaciones;
    private final Instant createdAt;
    private final List<SomaCharlaAsistenteDTO> asistentes;
    private final String advertencia;
}
