package com.convecsa.erp.construccion.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AsientosObraDTO {

    private final AsientoObraDTO materiales;
    private final AsientoObraDTO factoring;
}
