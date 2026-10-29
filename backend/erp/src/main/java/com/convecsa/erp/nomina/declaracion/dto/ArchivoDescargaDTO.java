package com.convecsa.erp.nomina.declaracion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArchivoDescargaDTO {

    private final String filename;
    private final String contentType;
    private final byte[] contenido;
}
