package com.convecsa.erp.desempeno.dto;

import com.convecsa.erp.desempeno.domain.EstadoEvaluacionDesempeno;
import java.math.BigDecimal;

public record EvaluacionDesempenoDTO(
        Long id,
        Long personaId,
        String periodo,
        int seguridadSst,
        int productividad,
        int calidadTecnica,
        int puntualidad,
        int trabajoEquipo,
        int metaCumplida,
        String comentarios,
        EstadoEvaluacionDesempeno estado,
        BigDecimal promedio) {}
