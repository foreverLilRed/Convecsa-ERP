package com.convecsa.erp.desempeno.dto;

import com.convecsa.erp.desempeno.domain.EstadoEvaluacionDesempeno;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvaluacionDesempenoRequest {

    @NotBlank
    @Pattern(regexp = "\\d{4}-Q[1-4]", message = "Periodo inválido. Use el formato AAAA-Qn.")
    private String periodo;

    @Min(1)
    @Max(5)
    private int seguridadSst;

    @Min(1)
    @Max(5)
    private int productividad;

    @Min(1)
    @Max(5)
    private int calidadTecnica;

    @Min(1)
    @Max(5)
    private int puntualidad;

    @Min(1)
    @Max(5)
    private int trabajoEquipo;

    @Min(0)
    @Max(100)
    private int metaCumplida;

    @Size(max = 4000)
    private String comentarios;

    @NotNull
    private EstadoEvaluacionDesempeno estado;
}
