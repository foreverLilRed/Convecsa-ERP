package com.convecsa.erp.nomina.declaracion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReciboHonorarioDTO {

    private final Long id;
    private final Long personaId;
    private final String personaNombres;
    private final String tipoDocumento;
    private final String numeroDocumento;
    private final String apellidoPaterno;
    private final String apellidoMaterno;
    private final String nombres;
    private final boolean domiciliado;
    private final String tipoComprobante;
    private final String serie;
    private final String numero;
    private final BigDecimal monto;
    private final LocalDate fechaEmision;
    private final LocalDate fechaPago;
    private final boolean retencionCuarta;
    private final boolean retencionPension;
    private final BigDecimal importeAportePension;
    private final boolean huerfano;
}
