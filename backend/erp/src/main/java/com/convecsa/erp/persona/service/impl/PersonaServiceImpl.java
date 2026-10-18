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
import com.convecsa.erp.persona.support.PersonaExcelExporter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonaServiceImpl implements InterfazPersonaService {

    private final PersonaRepository personaRepository;
    private final PersonaMapper personaMapper;
    private final PersonaExcelExporter personaExcelExporter;

    @PersistenceContext
    private EntityManager entityManager;

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

        persona.getCuentasBancarias().forEach(cuenta -> cuenta.setPrincipal(false));
        entityManager.flush();
        personaMapper.updateEntity(persona, request);
        Persona saved = personaRepository.save(persona);
        return personaMapper.toDto(saved);
    }

    @Override
    @Transactional
    public PersonaDTO habilitar(Long id) {
        return actualizarEstado(id, true);
    }

    @Override
    @Transactional
    public PersonaDTO deshabilitar(Long id) {
        return actualizarEstado(id, false);
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

    @Override
    @Transactional(readOnly = true)
    public byte[] exportar(PersonaListQuery query) {
        List<Persona> personas = personaRepository.findAll(
                PersonaSpecifications.from(query), PersonaSort.parse(query.sort()));
        return personaExcelExporter.export(personas);
    }

    private Persona findOrThrow(Long id) {
        return personaRepository
                .findById(id)
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
    }

    private PersonaDTO actualizarEstado(Long id, boolean activo) {
        Persona persona = findOrThrow(id);
        persona.setActivo(activo);
        return personaMapper.toDto(personaRepository.save(persona));
    }
}
