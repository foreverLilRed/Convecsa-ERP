package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.ClasificacionHallazgoSoma;
import com.convecsa.erp.soma.domain.EstadoHallazgoSoma;
import com.convecsa.erp.soma.domain.PotencialPerdidaSoma;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaHallazgoDTO {

    private final Long id;
    private final Long proyectoId;
    private final Long inspeccionId;
    private final String inspeccionAreaFrente;
    private final ClasificacionHallazgoSoma clasificacion;
    private final PotencialPerdidaSoma potencialPerdida;
    private final String descripcion;
    private final String ubicacion;
    private final String accionCorrectiva;
    private final Long responsablePersonaId;
    private final String responsableNombre;
    private final LocalDate fechaDeteccion;
    private final LocalDate fechaLimite;
    private final LocalDate fechaCierre;
    private final boolean paralizacionFrente;
    private final Long evidenciaAntesDocId;
    private final Long evidenciaDespuesDocId;
    private final EstadoHallazgoSoma estado;
    private final boolean vencido;
    private final Instant createdAt;
}
