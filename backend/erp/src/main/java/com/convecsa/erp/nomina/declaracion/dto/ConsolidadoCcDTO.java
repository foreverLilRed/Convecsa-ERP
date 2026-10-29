package com.convecsa.erp.nomina.declaracion.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConsolidadoCcDTO {

    private final Long id;
    private final Long personaId;
    private final String personaNombres;
    private final String personaApellidos;
    private final String numeroDocumento;
    private final Long proyectoId;
    private final String proyectoCodigo;
    private final String proyectoNombre;
    private final BigDecimal totalHaberes;
    private final BigDecimal totalDescuentos;
    private final BigDecimal totalAportesEmpleador;
    private final BigDecimal totalAfecto;
    private final BigDecimal neto;
    private final BigDecimal horasOrd;
    private final BigDecimal he60;
    private final BigDecimal he100;
    private final int diasLab;
    private final int diasFalta;
    private final int diasDm;
    private final int diasFe;
    private final int diasLic;
    private final List<ConsolidadoCcConceptoDTO> conceptos;
}
