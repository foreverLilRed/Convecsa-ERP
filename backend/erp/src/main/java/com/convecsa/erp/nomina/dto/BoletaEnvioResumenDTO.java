package com.convecsa.erp.nomina.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class BoletaEnvioResumenDTO {
    private final int enviados;
    private final int errores;
    private final List<String> detalles;
}
