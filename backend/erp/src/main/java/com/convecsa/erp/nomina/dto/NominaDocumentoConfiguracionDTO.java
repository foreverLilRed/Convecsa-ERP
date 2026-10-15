package com.convecsa.erp.nomina.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class NominaDocumentoConfiguracionDTO {
    private final String razonSocial;
    private final String ruc;
    private final String domicilio;
    private final String representante;
    private final String logoDataUrl;
    private final String firmaDataUrl;
}
