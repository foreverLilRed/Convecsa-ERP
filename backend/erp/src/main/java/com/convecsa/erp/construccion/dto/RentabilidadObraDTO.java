package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RentabilidadObraDTO {

    private final BigDecimal ingreso;
    private final BigDecimal costoMo;
    private final BigDecimal costoMateriales;
    private final BigDecimal costoFactoring;
    private final BigDecimal margenBruto;
    private final BigDecimal margenNeto;
    private final List<RentabilidadPuntoDTO> puntos;
}
