package com.convecsa.erp.construccion.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AlmacenDTO {

    private final Long id;
    private final Long proyectoId;
    private final String nombre;
    private final int quiebres;
    private final int criticosSinStock;
    private final List<StockItemDTO> stock;
}
