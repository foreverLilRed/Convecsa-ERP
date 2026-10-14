package com.convecsa.erp.persona.dto;

import com.convecsa.erp.persona.domain.AptitudMedica;
import com.convecsa.erp.persona.domain.Genero;
import com.convecsa.erp.persona.domain.GradoInstruccion;
import com.convecsa.erp.persona.domain.TipoDocumento;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PersonaDTO {

    private final Long id;
    private final String apellidos;
    private final String nombres;
    private final TipoDocumento tipoDocumento;
    private final String numeroDocumento;
    private final LocalDate fechaNacimiento;
    private final LocalDate fechaEmisionDni;
    private final LocalDate fechaCaducidadDni;
    private final List<CelularDTO> celulares;
    private final String correo;
    private final String pension;
    private final String cuspp;
    private final Genero genero;
    private final String contactoEmergenciaNombre;
    private final String contactoEmergenciaTelefono;
    private final String contactoEmergenciaParentesco;
    private final GradoInstruccion gradoInstruccion;
    private final String direccion;
    private final String departamento;
    private final String provincia;
    private final String distrito;
    private final String residencia;
    private final String fotoUrl;
    private final List<CuentaBancariaDTO> cuentasBancarias;
    private final List<ExperienciaLaboralDTO> experienciasLaborales;
    private final List<DocumentoChecklistDTO> documentosChecklist;
    private final LocalDate fechaVigenciaEmo;
    private final AptitudMedica aptitudMedica;
    private final String restriccionesMedicas;
    private final Instant createdAt;
}
