package com.convecsa.erp.nomina.dto;

import com.convecsa.erp.nomina.domain.EstadoBoletaPago;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoletaPagoResumenDTO {

    private final Long id;
    private final Long personaId;
    private final String personaNombres;
    private final String personaApellidos;
    private final String numeroDocumento;
    private final String cargo;
    private final String afpNombre;
    private final int diasTrabajados;
    private final BigDecimal totalHaberes;
    private final BigDecimal totalDescuentos;
    private final BigDecimal neto;
    private final EstadoBoletaPago estado;
}
