package com.convecsa.erp.desempeno.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DesempenoListQuery(
        @NotBlank String periodo,
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size,
        String q) {

    public int pageOrDefault() {
        return page == null ? 0 : page;
    }

    public int sizeOrDefault() {
        return size == null ? 20 : size;
    }
}
