package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.CausasBasicasEvento;
import com.convecsa.erp.soma.domain.CausasInmediatasEvento;
import com.convecsa.erp.soma.domain.EstadoInvestigacionSst;
import com.convecsa.erp.soma.domain.GravedadIncapacidadSst;
import com.convecsa.erp.soma.domain.TipoEventoSst;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaEventoDTO {

    private final Long id;
    private final Long proyectoId;
    private final int numero;
    private final TipoEventoSst tipoEvento;
    private final GravedadIncapacidadSst gravedadIncapacidad;
    private final Instant fechaHora;
    private final String lugarExacto;
    private final Long partidaId;
    private final String partidaCodigo;
    private final String partidaDescripcion;
    private final Long personaAfectadaId;
    private final String personaAfectadaNombre;
    private final Long subcontratoId;
    private final String subcontratoNombre;
    private final String descripcionEvento;
    private final String parteCuerpoLesionada;
    private final String naturalezaLesion;
    private final String agenteCausante;
    private final String numeroCertificadoDm;
    private final int diasDescansoMedico;
    private final int diasCargoEstadistico;
    private final LocalDate fechaInicioDm;
    private final LocalDate fechaFinDm;
    private final CausasInmediatasEvento causasInmediatas;
    private final CausasBasicasEvento causasBasicas;
    private final boolean notificadoMtpe;
    private final EstadoInvestigacionSst estadoInvestigacion;
    private final Long informeDocId;
    private final Instant createdAt;
    private final List<SomaHallazgoDTO> hallazgos;
}
