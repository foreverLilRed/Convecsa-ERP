package com.convecsa.erp.nomina.declaracion.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeclaracionDetalleDTO {

    private final DeclaracionMensualDTO declaracion;
    private final List<ConsolidadoCcDTO> consolidado;
    private final List<ReciboHonorarioDTO> recibos;
}
