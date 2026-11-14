package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.CategoriaResiduoSoma;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SomaResiduoDTO {

    private final Long id;
    private final Long proyectoId;
    private final LocalDate fecha;
    private final CategoriaResiduoSoma categoriaResiduo;
    private final String descripcion;
    private final BigDecimal cantidadKg;
    private final BigDecimal volumenM3;
    private final String eorsRazonSocial;
    private final String eorsRuc;
    private final String numeroManifiesto;
    private final String destinoFinal;
    private final Long manifiestoDocId;
    private final Instant createdAt;
}
