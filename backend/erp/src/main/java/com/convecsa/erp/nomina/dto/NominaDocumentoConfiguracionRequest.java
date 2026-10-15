package com.convecsa.erp.nomina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NominaDocumentoConfiguracionRequest {
    @NotBlank @Size(max = 200) private String razonSocial;
    @NotBlank @Size(max = 20) private String ruc;
    @NotBlank @Size(max = 300) private String domicilio;
    @Size(max = 200) private String representante;
    private String logoDataUrl;
    private String firmaDataUrl;
}
