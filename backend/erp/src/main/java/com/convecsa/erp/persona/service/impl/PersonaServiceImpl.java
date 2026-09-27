package com.convecsa.erp.persona.service.impl;

import com.convecsa.erp.common.exception.DocumentoDuplicadoException;
import com.convecsa.erp.persona.dto.PersonaDTO;
import com.convecsa.erp.persona.dto.PersonaRequest;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.mapper.PersonaMapper;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.persona.service.InterfazPersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonaServiceImpl implements InterfazPersonaService {

    private final PersonaRepository personaRepository;
    private final PersonaMapper personaMapper;

    @Override
    @Transactional
    public PersonaDTO crear(PersonaRequest request) {
        if (personaRepository.existsByTipoDocumentoAndNumeroDocumento(
                request.getTipoDocumento(), request.getNumeroDocumento().trim())) {
            throw new DocumentoDuplicadoException(
                    "Ya existe una persona con ese tipo y número de documento");
        }

        Persona persona = personaMapper.toEntity(request);
        Persona saved = personaRepository.save(persona);
        return personaMapper.toDto(saved);
    }
}
