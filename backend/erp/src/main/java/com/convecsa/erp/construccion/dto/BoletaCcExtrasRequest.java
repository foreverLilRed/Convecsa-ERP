package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.nomina.dto.BoletaConceptoExtraRequest;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoletaCcExtrasRequest {

    @Valid
    private List<BoletaConceptoExtraRequest> extras = new ArrayList<>();
}
