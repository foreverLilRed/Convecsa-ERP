package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.NivelRiesgoIperc;
import com.convecsa.erp.soma.domain.TipoPeligroIperc;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaIpercFilaDTO {

    private final Long id;
    private final Long proyectoId;
    private final Long partidaId;
    private final String partidaCodigo;
    private final String partidaDescripcion;
    private final String actividadTarea;
    private final String puestoTrabajo;
    private final boolean rutinario;
    private final TipoPeligroIperc tipoPeligro;
    private final String peligro;
    private final String riesgo;
    private final String consecuencia;
    private final String requisitoLegal;
    private final int probabilidadInicial;
    private final int severidadInicial;
    private final NivelRiesgoIperc nivelRiesgoInicial;
    private final String controlEliminacion;
    private final String controlSustitucion;
    private final String controlIngenieria;
    private final String controlAdministrativo;
    private final String controlEpp;
    private final int probabilidadResidual;
    private final int severidadResidual;
    private final NivelRiesgoIperc nivelRiesgoResidual;
    private final boolean activo;
    private final Instant createdAt;
}
