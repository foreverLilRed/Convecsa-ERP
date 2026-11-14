package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.TipoAgenteMonitoreoSoma;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaMonitoreoRequest {

    private LocalDate fecha;
    private TipoAgenteMonitoreoSoma tipoAgente;
    private String parametroMedido;
    private String puntoMedicion;
    private BigDecimal valorMedido;
    private BigDecimal limitePermisible;
    private String unidad;
    private String laboratorioNombre;
    private Long informeDocId;
}
