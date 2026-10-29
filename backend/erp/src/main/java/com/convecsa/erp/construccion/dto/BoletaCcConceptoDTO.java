package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.CodigoConceptoCc;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoletaCcConceptoDTO {

    private final CategoriaConcepto categoria;
    private final CodigoConceptoCc codigo;
    private final String label;
    private final BigDecimal monto;
    private final int orden;
}
