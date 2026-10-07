package com.convecsa.erp.asistencia.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AsistenciaFilaDTO {

    private final Long personaId;
    private final String personaNombres;
    private final String personaApellidos;
    private final String numeroDocumento;
    private final Long contratoId;
    private final String jornadaCodigo;
    private final String jornadaLabel;
    private final BigDecimal horasEsperadasDia;
    private final List<AsistenciaRegistroDTO> registros;
}
