package com.convecsa.erp.persona.link.service.impl;

import com.convecsa.erp.common.exception.DocumentoDuplicadoException;
import com.convecsa.erp.common.exception.LinkInvitadoExpiradoException;
import com.convecsa.erp.common.exception.LinkInvitadoNoEncontradoException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.persona.domain.TipoDocumento;
import com.convecsa.erp.persona.dto.PersonaDTO;
import com.convecsa.erp.persona.dto.PersonaRequest;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.link.domain.TipoLinkInvitado;
import com.convecsa.erp.persona.link.dto.PersonaLinkAltaRequest;
import com.convecsa.erp.persona.link.dto.PersonaLinkInvitadoDTO;
import com.convecsa.erp.persona.link.entity.PersonaLinkInvitado;
import com.convecsa.erp.persona.link.repository.PersonaLinkInvitadoRepository;
import com.convecsa.erp.persona.link.service.InterfazPersonaLinkInvitadoService;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.persona.service.InterfazPersonaService;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonaLinkInvitadoServiceImpl implements InterfazPersonaLinkInvitadoService {

    private static final int TOKEN_BYTES = 32;
    private static final int DURACION_DIAS = 3;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PersonaRepository personaRepository;
    private final PersonaLinkInvitadoRepository linkRepository;
    private final InterfazPersonaService personaService;

    @Override
    @Transactional
    public PersonaLinkInvitadoDTO crear(Long personaId) {
        Persona persona = personaRepository
                .findById(personaId)
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));

        PersonaLinkInvitado link = nuevoLink();
        link.setTipo(TipoLinkInvitado.EDICION);
        link.setPersona(persona);
        return toDto(linkRepository.save(link));
    }

    @Override
    @Transactional
    public PersonaLinkInvitadoDTO crearAlta(PersonaLinkAltaRequest request) {
        String apellidos = request.getApellidos().trim();
        String nombres = request.getNombres().trim();
        TipoDocumento tipoDocumento = request.getTipoDocumento();
        String numeroDocumento = request.getNumeroDocumento().trim();

        if (personaRepository.existsByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento)) {
            throw new DocumentoDuplicadoException(
                    "Ya existe una persona con ese tipo y número de documento");
        }

        PersonaLinkInvitado link = nuevoLink();
        link.setTipo(TipoLinkInvitado.ALTA);
        link.setApellidos(apellidos);
        link.setNombres(nombres);
        link.setTipoDocumento(tipoDocumento);
        link.setNumeroDocumento(numeroDocumento);
        return toDto(linkRepository.save(link));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaDTO obtenerPorToken(String token) {
        PersonaLinkInvitado link = linkVigente(token);
        if (link.getPersona() != null) {
            return personaService.obtener(link.getPersona().getId());
        }
        return fichaAltaPendiente(link);
    }

    @Override
    @Transactional
    public PersonaDTO actualizarPorToken(String token, PersonaRequest request) {
        PersonaLinkInvitado link = linkVigente(token);
        aplicarIdentidad(link, request);

        if (link.getPersona() == null) {
            PersonaDTO creada = personaService.crear(request);
            Persona persona = personaRepository
                    .findById(creada.getId())
                    .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
            link.setPersona(persona);
            linkRepository.save(link);
            return creada;
        }
        return personaService.actualizar(link.getPersona().getId(), request);
    }

    private void aplicarIdentidad(PersonaLinkInvitado link, PersonaRequest request) {
        if (link.getTipo() == TipoLinkInvitado.ALTA) {
            request.setApellidos(link.getApellidos());
            request.setNombres(link.getNombres());
            request.setTipoDocumento(link.getTipoDocumento());
            request.setNumeroDocumento(link.getNumeroDocumento());
            return;
        }
        Persona persona = link.getPersona();
        if (persona != null) {
            request.setApellidos(persona.getApellidos());
            request.setNombres(persona.getNombres());
            request.setTipoDocumento(persona.getTipoDocumento());
            request.setNumeroDocumento(persona.getNumeroDocumento());
        }
    }

    private static PersonaDTO fichaAltaPendiente(PersonaLinkInvitado link) {
        return PersonaDTO.builder()
                .apellidos(link.getApellidos())
                .nombres(link.getNombres())
                .tipoDocumento(link.getTipoDocumento())
                .numeroDocumento(link.getNumeroDocumento())
                .celulares(List.of())
                .cuentasBancarias(List.of())
                .experienciasLaborales(List.of())
                .documentosChecklist(List.of())
                .build();
    }

    private PersonaLinkInvitado nuevoLink() {
        Instant ahora = Instant.now();
        PersonaLinkInvitado link = new PersonaLinkInvitado();
        link.setToken(nuevoToken());
        link.setCreadoEn(ahora);
        link.setExpiraEn(ahora.plus(DURACION_DIAS, ChronoUnit.DAYS));
        return link;
    }

    private PersonaLinkInvitado linkVigente(String token) {
        PersonaLinkInvitado link = linkRepository
                .findByToken(token)
                .orElseThrow(() -> new LinkInvitadoNoEncontradoException("El enlace no es válido"));
        if (!Instant.now().isBefore(link.getExpiraEn())) {
            throw new LinkInvitadoExpiradoException("El enlace temporal venció");
        }
        return link;
    }

    private static PersonaLinkInvitadoDTO toDto(PersonaLinkInvitado link) {
        return PersonaLinkInvitadoDTO.builder()
                .id(link.getId())
                .tipo(link.getTipo())
                .personaId(link.getPersona() == null ? null : link.getPersona().getId())
                .apellidos(link.getApellidos())
                .nombres(link.getNombres())
                .tipoDocumento(link.getTipoDocumento())
                .numeroDocumento(link.getNumeroDocumento())
                .token(link.getToken())
                .creadoEn(link.getCreadoEn())
                .expiraEn(link.getExpiraEn())
                .build();
    }

    private static String nuevoToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
