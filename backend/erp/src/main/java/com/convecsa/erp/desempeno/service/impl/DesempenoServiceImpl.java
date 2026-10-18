package com.convecsa.erp.desempeno.service.impl;

import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.desempeno.domain.EstadoEvaluacionDesempeno;
import com.convecsa.erp.desempeno.dto.ColaboradorDesempenoDTO;
import com.convecsa.erp.desempeno.dto.ColaboradorDesempenoDetalleDTO;
import com.convecsa.erp.desempeno.dto.DesempenoListQuery;
import com.convecsa.erp.desempeno.dto.EvaluacionDesempenoDTO;
import com.convecsa.erp.desempeno.dto.EvaluacionDesempenoRequest;
import com.convecsa.erp.desempeno.entity.EvaluacionDesempeno;
import com.convecsa.erp.desempeno.repository.EvaluacionDesempenoRepository;
import com.convecsa.erp.desempeno.service.InterfazDesempenoService;
import com.convecsa.erp.desempeno.support.PeriodoDesempeno;
import com.convecsa.erp.desempeno.support.PromedioCompetencias;
import com.convecsa.erp.persona.dto.PersonaListQuery;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.persona.repository.PersonaSpecifications;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesempenoServiceImpl implements InterfazDesempenoService {

    private final PersonaRepository personaRepository;
    private final ContratoRepository contratoRepository;
    private final EvaluacionDesempenoRepository evaluacionRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ColaboradorDesempenoDTO> listarColaboradores(DesempenoListQuery query) {
        String periodo = PeriodoDesempeno.validar(query.periodo());
        PageRequest pageable = PageRequest.of(
                query.pageOrDefault(),
                query.sizeOrDefault(),
                Sort.by("apellidos").ascending().and(Sort.by("nombres").ascending()));
        PersonaListQuery personaQuery = new PersonaListQuery(
                query.pageOrDefault(),
                query.sizeOrDefault(),
                null,
                query.q(),
                null,
                null,
                null,
                null,
                null,
                null);
        Page<Persona> page = personaRepository.findAll(PersonaSpecifications.from(personaQuery), pageable);
        List<Long> ids = page.getContent().stream().map(Persona::getId).toList();
        Map<Long, EvaluacionDesempeno> evaluaciones = ids.isEmpty()
                ? Map.of()
                : evaluacionRepository.findByPersonaIdInAndPeriodo(ids, periodo).stream()
                        .collect(Collectors.toMap(eval -> eval.getPersona().getId(), Function.identity()));
        Map<Long, Contrato> contratos = contratosActivosPorPersona(ids);

        return PagedResponse.from(page.map(persona -> {
            EvaluacionDesempeno evaluacion = evaluaciones.get(persona.getId());
            Contrato contrato = contratos.get(persona.getId());
            return new ColaboradorDesempenoDTO(
                    persona.getId(),
                    persona.getNombres(),
                    persona.getApellidos(),
                    persona.getNumeroDocumento(),
                    persona.getFotoUrl(),
                    contrato == null ? null : contrato.metadata("cargo"),
                    contrato == null ? null : contrato.metadata("proyecto"),
                    evaluacion == null ? null : evaluacion.getId(),
                    evaluacion == null ? null : promedioDe(evaluacion),
                    evaluacion == null ? null : evaluacion.getEstado(),
                    evaluacion == null ? null : evaluacion.getMetaCumplida());
        }));
    }

    @Override
    @Transactional(readOnly = true)
    public ColaboradorDesempenoDetalleDTO obtenerColaborador(Long personaId, String periodo) {
        String periodoValido = PeriodoDesempeno.validar(periodo);
        Persona persona = findPersona(personaId);
        EvaluacionDesempeno evaluacion =
                evaluacionRepository.findByPersonaIdAndPeriodo(personaId, periodoValido).orElse(null);
        return toDetalle(persona, periodoValido, evaluacion);
    }

    @Override
    @Transactional
    public ColaboradorDesempenoDetalleDTO guardarEvaluacion(Long personaId, EvaluacionDesempenoRequest request) {
        String periodo = PeriodoDesempeno.validar(request.getPeriodo());
        Persona persona = findPersona(personaId);
        EvaluacionDesempeno evaluacion = evaluacionRepository
                .findByPersonaIdAndPeriodo(personaId, periodo)
                .orElseGet(() -> {
                    EvaluacionDesempeno nueva = new EvaluacionDesempeno();
                    nueva.setPersona(persona);
                    nueva.setPeriodo(periodo);
                    return nueva;
                });
        evaluacion.setSeguridadSst(request.getSeguridadSst());
        evaluacion.setProductividad(request.getProductividad());
        evaluacion.setCalidadTecnica(request.getCalidadTecnica());
        evaluacion.setPuntualidad(request.getPuntualidad());
        evaluacion.setTrabajoEquipo(request.getTrabajoEquipo());
        evaluacion.setMetaCumplida(request.getMetaCumplida());
        evaluacion.setComentarios(blankToNull(request.getComentarios()));
        evaluacion.setEstado(request.getEstado());
        EvaluacionDesempeno saved = evaluacionRepository.save(evaluacion);
        return toDetalle(persona, periodo, saved);
    }

    private ColaboradorDesempenoDetalleDTO toDetalle(
            Persona persona, String periodo, EvaluacionDesempeno evaluacion) {
        Contrato contrato = contratoRepository
                .findFirstByPersonaIdAndActivoTrueOrderByIdDesc(persona.getId())
                .orElse(null);
        EvaluacionDesempenoDTO dto = evaluacion == null
                ? vacia(persona.getId(), periodo)
                : toDto(evaluacion);
        return new ColaboradorDesempenoDetalleDTO(
                persona.getId(),
                persona.getNombres(),
                persona.getApellidos(),
                persona.getNumeroDocumento(),
                persona.getFotoUrl(),
                contrato == null ? null : contrato.metadata("cargo"),
                contrato == null ? null : contrato.metadata("proyecto"),
                dto);
    }

    private EvaluacionDesempenoDTO toDto(EvaluacionDesempeno evaluacion) {
        return new EvaluacionDesempenoDTO(
                evaluacion.getId(),
                evaluacion.getPersona().getId(),
                evaluacion.getPeriodo(),
                evaluacion.getSeguridadSst(),
                evaluacion.getProductividad(),
                evaluacion.getCalidadTecnica(),
                evaluacion.getPuntualidad(),
                evaluacion.getTrabajoEquipo(),
                evaluacion.getMetaCumplida(),
                evaluacion.getComentarios() == null ? "" : evaluacion.getComentarios(),
                evaluacion.getEstado(),
                promedioDe(evaluacion));
    }

    private static EvaluacionDesempenoDTO vacia(Long personaId, String periodo) {
        return new EvaluacionDesempenoDTO(
                null,
                personaId,
                periodo,
                3,
                3,
                3,
                3,
                3,
                0,
                "",
                EstadoEvaluacionDesempeno.PENDIENTE,
                PromedioCompetencias.calcular(3, 3, 3, 3, 3));
    }

    private Map<Long, Contrato> contratosActivosPorPersona(List<Long> personaIds) {
        if (personaIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Contrato> porPersona = new LinkedHashMap<>();
        for (Contrato contrato : contratoRepository.findByPersonaIdInAndActivoTrueOrderByIdDesc(personaIds)) {
            porPersona.putIfAbsent(contrato.getPersona().getId(), contrato);
        }
        return porPersona;
    }

    private Persona findPersona(Long id) {
        return personaRepository
                .findById(id)
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
    }

    private static BigDecimal promedioDe(EvaluacionDesempeno evaluacion) {
        return PromedioCompetencias.calcular(
                evaluacion.getSeguridadSst(),
                evaluacion.getProductividad(),
                evaluacion.getCalidadTecnica(),
                evaluacion.getPuntualidad(),
                evaluacion.getTrabajoEquipo());
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
