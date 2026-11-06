package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.TipoMovimientoAlmacen;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MovimientoAlmacenDTO {

    private final Long id;
    private final Long materialId;
    private final String codigoMaterial;
    private final String descripcionMaterial;
    private final TipoMovimientoAlmacen tipo;
    private final BigDecimal cantidad;
    private final BigDecimal costoUnitario;
    private final BigDecimal costoTotal;
    private final BigDecimal saldoCantidad;
    private final BigDecimal costoPromedio;
    private final Long partidaId;
    private final String codigoPartida;
    private final String centroCosto;
    private final Long recepcionId;
    private final Long valeId;
    private final Long ordenCompraId;
    private final LocalDate fecha;
    private final String observaciones;
    private final Instant createdAt;
}
