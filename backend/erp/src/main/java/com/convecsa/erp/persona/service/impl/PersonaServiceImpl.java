package com.convecsa.erp.persona.service.impl;

import com.convecsa.erp.common.exception.DocumentoDuplicadoException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
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
    public PersonaDTO obtener(Long id) {
        return personaMapper.toDto(findOrThrow(id));
    }

    @Override
    @Transactional
    public PersonaDTO actualizar(Long id, PersonaRequest request) {
        Persona persona = findOrThrow(id);
        if (personaRepository.existsByTipoDocumentoAndNumeroDocumentoAndIdNot(
                request.getTipoDocumento(), request.getNumeroDocumento().trim(), id)) {
            throw new DocumentoDuplicadoException(
                    "Ya existe una persona con ese tipo y número de documento");
        }

        personaMapper.updateEntity(persona, request);
        // #region agent log
        try {
            String line = String.format(
                    "{\"sessionId\":\"f22012\",\"hypothesisId\":\"B\",\"location\":\"PersonaServiceImpl.actualizar\",\"message\":\"before save salud fields\",\"data\":{\"id\":%d,\"fechaVigenciaEmo\":%s,\"aptitudMedica\":%s},\"timestamp\":%d}%n",
                    id,
                    request.getFechaVigenciaEmo() == null ? "null" : "\"" + request.getFechaVigenciaEmo() + "\"",
                    request.getAptitudMedica() == null ? "null" : "\"" + request.getAptitudMedica() + "\"",
                    System.currentTimeMillis());
            java.nio.file.Files.writeString(
                    java.nio.file.Path.of("D:/Proyectos/Convecsa ERP/debug-f22012.log"),
                    line,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
        // #endregion
        try {
            Persona saved = personaRepository.save(persona);
            return personaMapper.toDto(saved);
        } catch (RuntimeException ex) {
            // #region agent log
            try {
                String cause = ex.getClass().getSimpleName();
                Throwable root = ex;
                while (root.getCause() != null) {
                    root = root.getCause();
                }
                String line = String.format(
                        "{\"sessionId\":\"f22012\",\"hypothesisId\":\"A\",\"location\":\"PersonaServiceImpl.actualizar\",\"message\":\"save failed\",\"data\":{\"exception\":\"%s\",\"root\":\"%s\",\"rootMessage\":%s},\"timestamp\":%d}%n",
                        cause,
                        root.getClass().getSimpleName(),
                        root.getMessage() == null ? "null" : "\"" + root.getMessage().replace("\"", "'").replace("\n", " ") + "\"",
                        System.currentTimeMillis());
                java.nio.file.Files.writeString(
                        java.nio.file.Path.of("D:/Proyectos/Convecsa ERP/debug-f22012.log"),
                        line,
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.APPEND);
            } catch (Exception ignored) {
            }
            // #endregion
            throw ex;
        }
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

    private Persona findOrThrow(Long id) {
        return personaRepository
                .findById(id)
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
    }
}
