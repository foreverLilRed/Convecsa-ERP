package com.convecsa.erp.desempeno.dto;

public record ColaboradorDesempenoDetalleDTO(
        Long personaId,
        String nombres,
        String apellidos,
        String numeroDocumento,
        String fotoUrl,
        String cargo,
        String proyecto,
        EvaluacionDesempenoDTO evaluacion) {}
