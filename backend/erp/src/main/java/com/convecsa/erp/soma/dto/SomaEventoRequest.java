package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.CausasBasicasEvento;
import com.convecsa.erp.soma.domain.CausasInmediatasEvento;
import com.convecsa.erp.soma.domain.GravedadIncapacidadSst;
import com.convecsa.erp.soma.domain.TipoEventoSst;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaEventoRequest {

    private TipoEventoSst tipoEvento;
    private GravedadIncapacidadSst gravedadIncapacidad;
    private Instant fechaHora;
    private String lugarExacto;
    private Long partidaId;
    private Long personaAfectadaId;
    private Long subcontratoId;
    private String descripcionEvento;
    private String parteCuerpoLesionada;
    private String naturalezaLesion;
    private String agenteCausante;
    private String numeroCertificadoDm;
    private Integer diasCargoEstadistico;
    private LocalDate fechaInicioDm;
    private LocalDate fechaFinDm;
    private CausasInmediatasEvento causasInmediatas;
    private CausasBasicasEvento causasBasicas;
    private Boolean notificadoMtpe;
    private Long informeDocId;
}
