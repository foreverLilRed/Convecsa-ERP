package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.EstadoAdicionalObra;
import com.convecsa.erp.construccion.domain.TipoAdicionalObra;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdicionalObraDTO {

    private final Long id;
    private final Long proyectoId;
    private final TipoAdicionalObra tipo;
    private final EstadoAdicionalObra estado;
    private final Long partidaId;
    private final String codigo;
    private final String descripcion;
    private final String unidad;
    private final BigDecimal metrado;
    private final BigDecimal precioUnitario;
    private final BigDecimal total;
    private final Instant createdAt;
}
