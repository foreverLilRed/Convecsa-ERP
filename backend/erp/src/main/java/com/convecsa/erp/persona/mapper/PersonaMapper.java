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
        persona.setGenero(request.getGenero());
        persona.setContactoEmergenciaNombre(request.getContactoEmergenciaNombre().trim());
        persona.setContactoEmergenciaTelefono(request.getContactoEmergenciaTelefono().trim());
        persona.setContactoEmergenciaParentesco(blankToNull(request.getContactoEmergenciaParentesco()));
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

        persona.getCelulares().clear();
        request.getCelulares().stream()
                .map(String::trim)
                .filter(numero -> !numero.isEmpty())
                .forEach(numero -> {
                    PersonaCelular celular = new PersonaCelular();
                    celular.setNumero(numero);
                    persona.addCelular(celular);
                });

        syncCuentasBancarias(persona, request.getCuentasBancarias());

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

    private void syncCuentasBancarias(Persona persona, List<CuentaBancariaRequest> requests) {
        List<CuentaBancariaRequest> incoming = requests == null ? List.of() : requests;
        List<PersonaCuentaBancaria> actuales = persona.getCuentasBancarias();
        int keep = Math.min(actuales.size(), incoming.size());

        for (int i = 0; i < keep; i++) {
            applyCuenta(actuales.get(i), incoming.get(i));
        }
        if (actuales.size() > incoming.size()) {
            actuales.subList(incoming.size(), actuales.size()).clear();
        }
        for (int i = keep; i < incoming.size(); i++) {
            PersonaCuentaBancaria cuenta = new PersonaCuentaBancaria();
            applyCuenta(cuenta, incoming.get(i));
            persona.addCuentaBancaria(cuenta);
        }
    }

    private static void applyCuenta(PersonaCuentaBancaria cuenta, CuentaBancariaRequest request) {
        cuenta.setBanco(request.getBanco());
        cuenta.setNumeroCuenta(request.getNumeroCuenta().trim());
        cuenta.setCci(request.getCci().trim());
        cuenta.setPrincipal(request.isPrincipal());
    }

    private void applyChecklist(Persona persona, List<DocumentoChecklistRequest> requestItems) {
        Map<TipoDocumentoChecklist, EstadoDocumentoChecklist> estados = new EnumMap<>(TipoDocumentoChecklist.class);
        if (requestItems != null) {
            for (DocumentoChecklistRequest item : requestItems) {
                if (item.getTipo() != null && item.getEstado() != null) {
                    estados.put(item.getTipo(), item.getEstado());
                }
            }
        }

        persona.getDocumentosChecklist()
                .removeIf(existente -> !estados.containsKey(existente.getTipoDocumento()));

        Map<TipoDocumentoChecklist, PersonaDocumentoChecklist> actuales = new EnumMap<>(TipoDocumentoChecklist.class);
        for (PersonaDocumentoChecklist existente : persona.getDocumentosChecklist()) {
            actuales.put(existente.getTipoDocumento(), existente);
        }

        for (Map.Entry<TipoDocumentoChecklist, EstadoDocumentoChecklist> entry : estados.entrySet()) {
            TipoDocumentoChecklist tipo = entry.getKey();
            PersonaDocumentoChecklist fila = actuales.get(tipo);
            if (fila == null) {
                fila = new PersonaDocumentoChecklist();
                fila.setTipoDocumento(tipo);
                persona.addDocumentoChecklist(fila);
            }
            fila.setCriticidad(tipo.getCriticidad());
            fila.setEstado(entry.getValue());
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
                .celulares(toCelularDtos(persona))
                .correo(persona.getCorreo())
                .genero(persona.getGenero())
                .contactoEmergenciaNombre(persona.getContactoEmergenciaNombre())
                .contactoEmergenciaTelefono(persona.getContactoEmergenciaTelefono())
                .contactoEmergenciaParentesco(persona.getContactoEmergenciaParentesco())
                .gradoInstruccion(persona.getGradoInstruccion())
                .direccion(persona.getDireccion())
                .departamento(persona.getDepartamento())
                .provincia(persona.getProvincia())
                .distrito(persona.getDistrito())
                .residencia(persona.getResidencia())
                .fotoUrl(persona.getFotoUrl())
                .cuentasBancarias(toCuentaDtos(persona))
                .experienciasLaborales(toExperienciaDtos(persona))
                .documentosChecklist(toDocumentoDtos(persona))
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
                .fechaNacimiento(persona.getFechaNacimiento())
                .fechaEmisionDni(persona.getFechaEmisionDni())
                .fechaCaducidadDni(persona.getFechaCaducidadDni())
                .celulares(toCelularDtos(persona))
                .correo(persona.getCorreo())
                .gradoInstruccion(persona.getGradoInstruccion())
                .direccion(persona.getDireccion())
                .departamento(persona.getDepartamento())
                .provincia(persona.getProvincia())
                .distrito(persona.getDistrito())
                .residencia(persona.getResidencia())
                .cuentasBancarias(toCuentaDtos(persona))
                .experienciasLaborales(toExperienciaDtos(persona))
                .fechaVigenciaEmo(persona.getFechaVigenciaEmo())
                .aptitudMedica(persona.getAptitudMedica())
                .documentosChecklist(toDocumentoDtos(persona))
                .createdAt(persona.getCreatedAt())
                .build();
    }

    private static List<CelularDTO> toCelularDtos(Persona persona) {
        return persona.getCelulares().stream()
                .map(celular -> new CelularDTO(celular.getId(), celular.getNumero()))
                .toList();
    }

    private static List<CuentaBancariaDTO> toCuentaDtos(Persona persona) {
        return persona.getCuentasBancarias().stream()
                .map(cuenta -> new CuentaBancariaDTO(
                        cuenta.getId(),
                        cuenta.getBanco(),
                        cuenta.getNumeroCuenta(),
                        cuenta.getCci(),
                        cuenta.isPrincipal()))
                .toList();
    }

    private static List<ExperienciaLaboralDTO> toExperienciaDtos(Persona persona) {
        return persona.getExperienciasLaborales().stream()
                .map(experiencia -> new ExperienciaLaboralDTO(
                        experiencia.getId(),
                        experiencia.getOrganizacion(),
                        experiencia.getCargo(),
                        experiencia.getFechaInicioLaboral(),
                        experiencia.getFechaFin()))
                .toList();
    }

    private static List<DocumentoChecklistDTO> toDocumentoDtos(Persona persona) {
        return persona.getDocumentosChecklist().stream()
                .sorted(Comparator.comparing(item -> item.getTipoDocumento().ordinal()))
                .map(item -> new DocumentoChecklistDTO(
                        item.getId(),
                        item.getTipoDocumento(),
                        item.getCriticidad(),
                        item.getEstado()))
                .toList();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
