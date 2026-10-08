package com.convecsa.erp.contrato.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdendaRequest {

    @NotNull
    private Long contratoId;

    @NotNull
    private Long plantillaContratoId;

    @NotNull
    private LocalDate fechaFinNueva;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal sueldo;

    @NotBlank
    @Size(max = 2000)
    private String motivo;

    @Size(max = 80)
    private String ciudadEmision;

    @NotNull
    private LocalDate fechaEmision;

    private Map<String, String> metadata = new LinkedHashMap<>();
}
