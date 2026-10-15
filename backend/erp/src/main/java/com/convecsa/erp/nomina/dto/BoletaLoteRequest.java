package com.convecsa.erp.nomina.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BoletaLoteRequest {
    private List<Long> boletaIds = new ArrayList<>();
}
