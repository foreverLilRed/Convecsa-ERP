package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.EstadoValorizacion;
import com.convecsa.erp.construccion.domain.TipoValorizacion;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ValorizacionDTO {

    private final Long id;
    private final Long proyectoId;
    private final int numero;
    private final TipoValorizacion tipo;
    private final EstadoValorizacion estado;
    private final int anio;
    private final int mes;
    private final BigDecimal porcentajeRetencion;
    private final BigDecimal montoBruto;
    private final BigDecimal montoRetencion;
    private final BigDecimal montoNeto;
    private final String observaciones;
    private final Instant createdAt;
    private final List<ValorizacionItemDTO> items;
    private final FacturaObraDTO factura;
}
