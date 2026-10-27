package com.convecsa.erp.obra.dto;

import com.convecsa.erp.obra.domain.EstadoObra;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObraEstadoRequest {

    @NotNull(message = "El estado destino es obligatorio")
    private EstadoObra estado;

    @Size(max = 2000, message = "La justificación no puede superar 2000 caracteres")
    private String justificacion;
}
