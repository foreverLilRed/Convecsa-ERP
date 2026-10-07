package com.convecsa.erp.nomina.dto;

import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.domain.CodigoConcepto;
import com.convecsa.erp.nomina.domain.OrigenConcepto;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoletaConceptoDTO {

    private final Long id;
    private final CategoriaConcepto categoria;
    private final CodigoConcepto codigo;
    private final String label;
    private final BigDecimal monto;
    private final int orden;
    private final OrigenConcepto origen;
}
