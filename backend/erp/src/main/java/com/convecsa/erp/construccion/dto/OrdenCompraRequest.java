package com.convecsa.erp.construccion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrdenCompraRequest {

    private Long requerimientoId;

    @Size(max = 160)
    private String proveedorRazon;

    @Size(max = 11)
    private String proveedorRuc;

    private LocalDate plazoEntrega;

    private String observaciones;

    @Valid
    private List<OrdenCompraItemRequest> items = new ArrayList<>();
}
