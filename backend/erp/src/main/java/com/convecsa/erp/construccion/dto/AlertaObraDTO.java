package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.SeveridadAlertaObra;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AlertaObraDTO {

    private final String codigo;
    private final SeveridadAlertaObra severidad;
    private final String mensaje;
}
