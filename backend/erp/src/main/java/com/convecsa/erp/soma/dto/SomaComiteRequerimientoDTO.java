package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.TipoRequerimientoComiteSoma;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaComiteRequerimientoDTO {

    private final TipoRequerimientoComiteSoma tipoRequerido;
    private final int trabajadoresActivos;
    private final int titularesEmpleador;
    private final int titularesTrabajadores;
    private final int supervisoresActivos;
    private final List<String> avisos;
}
