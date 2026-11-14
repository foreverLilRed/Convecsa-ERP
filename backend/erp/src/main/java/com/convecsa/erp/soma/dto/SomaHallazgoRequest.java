package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.ClasificacionHallazgoSoma;
import com.convecsa.erp.soma.domain.PotencialPerdidaSoma;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaHallazgoRequest {

    private Long inspeccionId;
    private ClasificacionHallazgoSoma clasificacion;
    private PotencialPerdidaSoma potencialPerdida;
    private String descripcion;
    private String ubicacion;
    private String accionCorrectiva;
    private Long responsablePersonaId;
    private LocalDate fechaDeteccion;
    private LocalDate fechaLimite;
    private Boolean paralizacionFrente;
    private Long evidenciaAntesDocId;
}
