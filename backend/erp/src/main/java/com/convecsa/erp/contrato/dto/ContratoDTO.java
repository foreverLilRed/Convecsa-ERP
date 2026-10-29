package com.convecsa.erp.contrato.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContratoDTO {

    private final Long id;
    private final String titulo;
    private final Long categoriaContratoId;
    private final String categoriaContratoCodigo;
    private final String categoriaContratoNombre;
    private final Long regimenLaboralId;
    private final String regimenLaboralCodigo;
    private final String regimenLaboralNombre;
    private final Long personaId;
    private final String personaNombreCompleto;
    private final String personaNumeroDocumento;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFin;
    private final BigDecimal sueldo;
    private final boolean activo;
    private final LocalDate fechaCorte;
    private final Long tipoPlantillaId;
    private final String tipoPlantillaCodigo;
    private final String tipoPlantillaNombre;
    private final String observaciones;
    private final Map<String, String> metadata;
    private final String categoriaObrera;
    private final String especialidadBae;
    private final Boolean afiliadoSindical;
    private final BigDecimal cuotaSindical;
    private final Integer hijosEscolaridad;
    private final Long proyectoId;
    private final String proyectoCodigo;
    private final String proyectoNombre;
}
