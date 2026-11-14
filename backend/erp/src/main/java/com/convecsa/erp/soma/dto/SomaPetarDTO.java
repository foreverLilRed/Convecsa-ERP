package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.EstadoPetar;
import com.convecsa.erp.soma.domain.TipoTrabajoPetar;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaPetarDTO {

    private final Long id;
    private final Long proyectoId;
    private final int numero;
    private final TipoTrabajoPetar tipoTrabajo;
    private final Long partidaId;
    private final String partidaCodigo;
    private final String partidaDescripcion;
    private final Long cuadrillaId;
    private final String cuadrillaNombre;
    private final LocalDate fecha;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;
    private final String ubicacionFrente;
    private final String descripcionTrabajo;
    private final Map<String, Boolean> checklist;
    private final Map<String, BigDecimal> mediciones;
    private final Long residentePersonaId;
    private final String residenteNombre;
    private final Long supervisorSsomaId;
    private final String supervisorSsomaNombre;
    private final EstadoPetar estado;
    private final String autorizadoPor;
    private final Instant autorizadoAt;
    private final Long documentoObraId;
    private final Instant createdAt;
    private final List<SomaPetarTrabajadorDTO> trabajadores;
}
