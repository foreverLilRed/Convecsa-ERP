package com.convecsa.erp.desempeno.dto;

import com.convecsa.erp.desempeno.domain.EstadoEvaluacionDesempeno;
import java.math.BigDecimal;

public record ColaboradorDesempenoDTO(
        Long personaId,
        String nombres,
        String apellidos,
        String numeroDocumento,
        String fotoUrl,
        String cargo,
        String proyecto,
        Long evaluacionId,
        BigDecimal promedio,
        EstadoEvaluacionDesempeno estado,
        Integer metaCumplida) {}
