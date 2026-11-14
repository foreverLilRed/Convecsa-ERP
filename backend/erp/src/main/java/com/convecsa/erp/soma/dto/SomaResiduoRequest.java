package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.CategoriaResiduoSoma;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaResiduoRequest {

    private LocalDate fecha;
    private CategoriaResiduoSoma categoriaResiduo;
    private String descripcion;
    private BigDecimal cantidadKg;
    private BigDecimal volumenM3;
    private String eorsRazonSocial;
    private String eorsRuc;
    private String numeroManifiesto;
    private String destinoFinal;
    private Long manifiestoDocId;
}
