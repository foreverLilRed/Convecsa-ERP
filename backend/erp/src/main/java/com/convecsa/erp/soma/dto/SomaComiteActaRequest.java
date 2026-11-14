package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.TipoSesionComiteSoma;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaComiteActaRequest {

    private String numeroActa;
    private TipoSesionComiteSoma tipoSesion;
    private LocalDate fecha;
    private String agenda;
    private String acuerdos;
    private Long actaDocId;
}
