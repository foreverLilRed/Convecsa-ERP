package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.contrato.dto.CatalogoItemDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConstruccionCatalogosDTO {

    private final List<CatalogoItemDTO> categorias;
    private final List<CatalogoItemDTO> especialidadesBae;
    private final List<ProyectoDTO> proyectos;
    private final int anioParametros;
}
