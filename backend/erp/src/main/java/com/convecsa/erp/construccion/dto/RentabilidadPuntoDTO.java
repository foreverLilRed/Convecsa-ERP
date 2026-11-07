package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RentabilidadPuntoDTO {

    private final int anio;
    private final int mes;
    private final BigDecimal ingreso;
    private final BigDecimal costoMo;
    private final BigDecimal costoMateriales;
    private final BigDecimal costoFactoring;
    private final BigDecimal margenBruto;
    private final BigDecimal margenNeto;
    private final BigDecimal margenBrutoAcumulado;
    private final BigDecimal margenNetoAcumulado;
}
