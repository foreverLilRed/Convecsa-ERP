package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.TipoPeligroIperc;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaIpercFilaRequest {

    private Long partidaId;
    private String actividadTarea;
    private String puestoTrabajo;
    private Boolean rutinario;
    private TipoPeligroIperc tipoPeligro;
    private String peligro;
    private String riesgo;
    private String consecuencia;
    private String requisitoLegal;
    private Integer probabilidadInicial;
    private Integer severidadInicial;
    private String controlEliminacion;
    private String controlSustitucion;
    private String controlIngenieria;
    private String controlAdministrativo;
    private String controlEpp;
    private Integer probabilidadResidual;
    private Integer severidadResidual;
}
