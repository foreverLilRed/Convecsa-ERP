package com.convecsa.erp.persona.mapper;

import com.convecsa.erp.persona.domain.EstadoDocumentoChecklist;
import com.convecsa.erp.persona.domain.TipoDocumentoChecklist;
import com.convecsa.erp.persona.dto.CelularDTO;
import com.convecsa.erp.persona.dto.CuentaBancariaDTO;
import com.convecsa.erp.persona.dto.CuentaBancariaRequest;
import com.convecsa.erp.persona.dto.DocumentoChecklistDTO;
import com.convecsa.erp.persona.dto.DocumentoChecklistRequest;
import com.convecsa.erp.persona.dto.ExperienciaLaboralDTO;
import com.convecsa.erp.persona.dto.ExperienciaLaboralRequest;
import com.convecsa.erp.persona.dto.PersonaDTO;
import com.convecsa.erp.persona.dto.PersonaRequest;
import com.convecsa.erp.persona.dto.PersonaResumenDTO;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.entity.PersonaCelular;
import com.convecsa.erp.persona.entity.PersonaCuentaBancaria;
import com.convecsa.erp.persona.entity.PersonaDocumentoChecklist;
import com.convecsa.erp.persona.entity.PersonaExperienciaLaboral;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PersonaMapper {

    public Persona toEntity(PersonaRequest request) {
        Persona persona = new Persona();
        updateEntity(persona, request);
        return persona;
    }

    public void updateEntity(Persona persona, PersonaRequest request) {
        persona.setApellidos(request.getApellidos().trim());
        persona.setNombres(request.getNombres().trim());
        persona.setTipoDocumento(request.getTipoDocumento());
        persona.setNumeroDocumento(request.getNumeroDocumento().trim());
        persona.setFechaNacimiento(request.getFechaNacimiento());
        persona.setFechaEmisionDni(request.getFechaEmisionDni());
        persona.setFechaCaducidadDni(request.getFechaCaducidadDni());
        persona.setCorreo(request.getCorreo().trim());
        persona.setCorreoSecundario(blankToNull(request.getCorreoSecundario()));
        persona.setGradoInstruccion(request.getGradoInstruccion());
        persona.setDireccion(request.getDireccion().trim());
        persona.setDepartamento(request.getDepartamento().trim());
        persona.setProvincia(request.getProvincia().trim());
        persona.setDistrito(request.getDistrito().trim());
        persona.setResidencia(request.getResidencia().trim());
        persona.setFotoUrl(blankToNull(request.getFotoUrl()));
        persona.setFechaVigenciaEmo(request.getFechaVigenciaEmo());
        persona.setAptitudMedica(request.getAptitudMedica());
        persona.setRestriccionesMedicas(blankToNull(request.getRestriccionesMedicas()));

        int cuentasAntes = persona.getCuentasBancarias().size();
        long principalesAntes = persona.getCuentasBancarias().stream().filter(PersonaCuentaBancaria::isPrincipal).count();

        persona.getCelulares().clear();
        request.getCelulares().stream()
                .map(String::trim)
                .filter(numero -> !numero.isEmpty())
                .forEach(numero -> {
                    PersonaCelular celular = new PersonaCelular();
                    celular.setNumero(numero);
                    persona.addCelular(celular);
                });

        persona.getCuentasBancarias().clear();
        if (request.getCuentasBancarias() != null) {
            for (CuentaBancariaRequest cuentaRequest : request.getCuentasBancarias()) {
                PersonaCuentaBancaria cuenta = new PersonaCuentaBancaria();
                cuenta.setBanco(cuentaRequest.getBanco());
                cuenta.setNumeroCuenta(cuentaRequest.getNumeroCuenta().trim());
                cuenta.setCci(cuentaRequest.getCci().trim());
                cuenta.setPrincipal(cuentaRequest.isPrincipal());
                persona.addCuentaBancaria(cuenta);
            }
        }
        int cuentasDespues = persona.getCuentasBancarias().size();
        long principalesDespues = persona.getCuentasBancarias().stream().filter(PersonaCuentaBancaria::isPrincipal).count();
        // #region agent log
        try {
            String line = String.format(
                    "{\"sessionId\":\"f22012\",\"hypothesisId\":\"A\",\"location\":\"PersonaMapper.updateEntity\",\"message\":\"cuentas around replace\",\"data\":{\"cuentasAntes\":%d,\"principalesAntes\":%d,\"cuentasDespues\":%d,\"principalesDespues\":%d,\"incomingCuentas\":%d},\"timestamp\":%d}%n",
                    cuentasAntes,
                    principalesAntes,
                    cuentasDespues,
                    principalesDespues,
                    request.getCuentasBancarias() == null ? 0 : request.getCuentasBancarias().size(),
                    System.currentTimeMillis());
            java.nio.file.Files.writeString(
                    java.nio.file.Path.of("D:/Proyectos/Convecsa ERP/debug-f22012.log"),
                    line,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
        // #endregion

        persona.getExperienciasLaborales().clear();
        if (request.getExperienciasLaborales() != null) {
            for (ExperienciaLaboralRequest experienciaRequest : request.getExperienciasLaborales()) {
                PersonaExperienciaLaboral experiencia = new PersonaExperienciaLaboral();
                experiencia.setOrganizacion(experienciaRequest.getOrganizacion().trim());
                experiencia.setCargo(experienciaRequest.getCargo().trim());
                experiencia.setFechaInicioLaboral(experienciaRequest.getFechaInicioLaboral());
                experiencia.setFechaFin(experienciaRequest.getFechaFin());
                persona.addExperienciaLaboral(experiencia);
            }
        }

        applyChecklist(persona, request.getDocumentosChecklist());
    }

    private void applyChecklist(Persona persona, List<DocumentoChecklistRequest> requestItems) {
        Map<TipoDocumentoChecklist, EstadoDocumentoChecklist> estados = new EnumMap<>(TipoDocumentoChecklist.class);
        for (TipoDocumentoChecklist tipo : TipoDocumentoChecklist.values()) {
            estados.put(tipo, EstadoDocumentoChecklist.PENDIENTE);
        }
        if (requestItems != null) {
            for (DocumentoChecklistRequest item : requestItems) {
                if (item.getTipo() != null && item.getEstado() != null) {
                    estados.put(item.getTipo(), item.getEstado());
                }
            }
        }

        Map<TipoDocumentoChecklist, PersonaDocumentoChecklist> actuales = new EnumMap<>(TipoDocumentoChecklist.class);
        for (PersonaDocumentoChecklist existente : persona.getDocumentosChecklist()) {
            actuales.put(existente.getTipoDocumento(), existente);
        }

        for (TipoDocumentoChecklist tipo : TipoDocumentoChecklist.values()) {
            PersonaDocumentoChecklist fila = actuales.get(tipo);
            if (fila == null) {
                fila = new PersonaDocumentoChecklist();
                fila.setTipoDocumento(tipo);
                persona.addDocumentoChecklist(fila);
            }
            fila.setCriticidad(tipo.getCriticidad());
            fila.setEstado(estados.get(tipo));
        }
    }

    public PersonaDTO toDto(Persona persona) {
        return PersonaDTO.builder()
                .id(persona.getId())
                .apellidos(persona.getApellidos())
                .nombres(persona.getNombres())
                .tipoDocumento(persona.getTipoDocumento())
                .numeroDocumento(persona.getNumeroDocumento())
                .fechaNacimiento(persona.getFechaNacimiento())
                .fechaEmisionDni(persona.getFechaEmisionDni())
                .fechaCaducidadDni(persona.getFechaCaducidadDni())
                .celulares(persona.getCelulares().stream()
                        .map(celular -> new CelularDTO(celular.getId(), celular.getNumero()))
                        .toList())
                .correo(persona.getCorreo())
                .correoSecundario(persona.getCorreoSecundario())
                .gradoInstruccion(persona.getGradoInstruccion())
                .direccion(persona.getDireccion())
                .departamento(persona.getDepartamento())
                .provincia(persona.getProvincia())
                .distrito(persona.getDistrito())
                .residencia(persona.getResidencia())
                .fotoUrl(persona.getFotoUrl())
                .cuentasBancarias(persona.getCuentasBancarias().stream()
                        .map(cuenta -> new CuentaBancariaDTO(
                                cuenta.getId(),
                                cuenta.getBanco(),
                                cuenta.getNumeroCuenta(),
                                cuenta.getCci(),
                                cuenta.isPrincipal()))
                        .toList())
                .experienciasLaborales(persona.getExperienciasLaborales().stream()
                        .map(experiencia -> new ExperienciaLaboralDTO(
                                experiencia.getId(),
                                experiencia.getOrganizacion(),
                                experiencia.getCargo(),
                                experiencia.getFechaInicioLaboral(),
                                experiencia.getFechaFin()))
                        .toList())
                .documentosChecklist(persona.getDocumentosChecklist().stream()
                        .sorted(Comparator.comparing(item -> item.getTipoDocumento().ordinal()))
                        .map(item -> new DocumentoChecklistDTO(
                                item.getId(),
                                item.getTipoDocumento(),
                                item.getCriticidad(),
                                item.getEstado()))
                        .toList())
                .fechaVigenciaEmo(persona.getFechaVigenciaEmo())
                .aptitudMedica(persona.getAptitudMedica())
                .restriccionesMedicas(persona.getRestriccionesMedicas())
                .createdAt(persona.getCreatedAt())
                .build();
    }

    public PersonaResumenDTO toResumen(Persona persona) {
        return PersonaResumenDTO.builder()
                .id(persona.getId())
                .apellidos(persona.getApellidos())
                .nombres(persona.getNombres())
                .tipoDocumento(persona.getTipoDocumento())
                .numeroDocumento(persona.getNumeroDocumento())
                .correo(persona.getCorreo())
                .gradoInstruccion(persona.getGradoInstruccion())
                .departamento(persona.getDepartamento())
                .distrito(persona.getDistrito())
                .createdAt(persona.getCreatedAt())
                .build();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
