package com.convecsa.erp.persona.dto;

import com.convecsa.erp.persona.domain.GradoInstruccion;
import com.convecsa.erp.persona.domain.TipoDocumento;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PersonaListQuery(
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size,
        String sort,
        String q,
        TipoDocumento tipoDocumento,
        GradoInstruccion gradoInstruccion,
        String departamento,
        String provincia,
        String distrito,
        Boolean conContratoActivo) {

    public int pageOrDefault() {
        return page == null ? 0 : page;
    }

    public int sizeOrDefault() {
        return size == null ? 20 : size;
    }
}
