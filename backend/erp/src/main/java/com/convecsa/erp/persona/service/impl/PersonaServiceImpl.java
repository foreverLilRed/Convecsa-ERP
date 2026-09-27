package com.convecsa.erp.persona.service.impl;

import com.convecsa.erp.common.exception.DocumentoDuplicadoException;
import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.persona.PersonaSort;
import com.convecsa.erp.persona.dto.PersonaDTO;
import com.convecsa.erp.persona.dto.PersonaListQuery;
import com.convecsa.erp.persona.dto.PersonaRequest;
import com.convecsa.erp.persona.dto.PersonaResumenDTO;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.mapper.PersonaMapper;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.persona.repository.PersonaSpecifications;
import com.convecsa.erp.persona.service.InterfazPersonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PersonaResumenDTO> listar(PersonaListQuery query) {
        Pageable pageable = PageRequest.of(
                query.pageOrDefault(), query.sizeOrDefault(), PersonaSort.parse(query.sort()));
        Page<PersonaResumenDTO> page = personaRepository
                .findAll(PersonaSpecifications.from(query), pageable)
                .map(personaMapper::toResumen);
        return PagedResponse.from(page);
    }
}
