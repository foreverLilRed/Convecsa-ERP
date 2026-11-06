package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.EstadoOrdenCompra;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrdenCompraDTO {

    private final Long id;
    private final Long proyectoId;
    private final int numero;
    private final EstadoOrdenCompra estado;
    private final String proveedorRazon;
    private final String proveedorRuc;
    private final LocalDate plazoEntrega;
    private final String observaciones;
    private final Instant createdAt;
    private final List<OrdenCompraItemDTO> items;
    private final List<RecepcionDTO> recepciones;
}
