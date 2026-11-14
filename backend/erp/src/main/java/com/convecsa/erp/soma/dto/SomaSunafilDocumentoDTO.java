package com.convecsa.erp.soma.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaSunafilDocumentoDTO {

    private final int formato;
    private final String titulo;
    private final String razonSocial;
    private final String ruc;
    private final String domicilio;
    private final String logoDataUrl;
    private final String alcance;
    private final String periodo;
    private final List<String> columnas;
    private final List<List<String>> filas;
}
