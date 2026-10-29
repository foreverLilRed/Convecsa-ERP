package com.convecsa.erp.construccion.support;

import com.convecsa.erp.construccion.domain.CodigoConceptoCc;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import java.math.BigDecimal;

public record CcConceptoCalculado(
        CategoriaConcepto categoria,
        CodigoConceptoCc codigo,
        String label,
        BigDecimal monto) {
}
