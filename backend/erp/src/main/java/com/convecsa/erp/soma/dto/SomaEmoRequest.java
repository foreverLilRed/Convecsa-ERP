package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.AptitudEmo;
import com.convecsa.erp.soma.domain.TipoEmo;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaEmoRequest {

    private Long personaId;
    private Long proyectoId;
    private TipoEmo tipoEmo;
    private String clinicaNombre;
    private String clinicaRuc;
    private String medicoEvaluador;
    private String cmpMedico;
    private LocalDate fechaExamen;
    private LocalDate fechaVencimiento;
    private AptitudEmo aptitud;
    private String restricciones;
    private String interconsultas;
    private String observaciones;
    private Long documentoObraId;
}
