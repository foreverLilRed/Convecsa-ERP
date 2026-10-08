package com.convecsa.erp.mof.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MofPuestoDTO {

    private final Long id;
    private final String codigo;
    private final String cargo;
    private final String grupo;
    private final String proposito;
    private final String superior;
    private final String subordinados;
    private final List<String> funciones;
    private final List<String> educacion;
    private final List<String> experiencia;
    private final List<String> competencias;
}
