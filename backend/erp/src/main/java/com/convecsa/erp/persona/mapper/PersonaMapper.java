package com.convecsa.erp.persona.mapper;

import com.convecsa.erp.persona.dto.CelularDTO;
import com.convecsa.erp.persona.dto.CuentaBancariaDTO;
import com.convecsa.erp.persona.dto.CuentaBancariaRequest;
import com.convecsa.erp.persona.dto.PersonaDTO;
import com.convecsa.erp.persona.dto.PersonaRequest;
import com.convecsa.erp.persona.dto.PersonaResumenDTO;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.entity.PersonaCelular;
import com.convecsa.erp.persona.entity.PersonaCuentaBancaria;
import org.springframework.stereotype.Component;

@Component
public class PersonaMapper {

    public Persona toEntity(PersonaRequest request) {
        Persona persona = new Persona();
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

        request.getCelulares().stream()
                .map(String::trim)
                .filter(numero -> !numero.isEmpty())
                .forEach(numero -> {
                    PersonaCelular celular = new PersonaCelular();
                    celular.setNumero(numero);
                    persona.addCelular(celular);
                });

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

        return persona;
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
