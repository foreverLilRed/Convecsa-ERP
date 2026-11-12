package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.AptitudEmo;
import com.convecsa.erp.soma.domain.TipoEmo;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaEmoDTO {

    private final Long id;
    private final Long personaId;
    private final String personaNombre;
    private final Long proyectoId;
    private final TipoEmo tipoEmo;
    private final String clinicaNombre;
    private final String clinicaRuc;
    private final String medicoEvaluador;
    private final String cmpMedico;
    private final LocalDate fechaExamen;
    private final LocalDate fechaVencimiento;
    private final AptitudEmo aptitud;
    private final String restricciones;
    private final String interconsultas;
    private final String observaciones;
    private final Long documentoObraId;
    private final Instant createdAt;
}
