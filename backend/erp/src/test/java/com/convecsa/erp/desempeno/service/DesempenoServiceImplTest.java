package com.convecsa.erp.desempeno.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.desempeno.domain.EstadoEvaluacionDesempeno;
import com.convecsa.erp.desempeno.dto.ColaboradorDesempenoDTO;
import com.convecsa.erp.desempeno.dto.ColaboradorDesempenoDetalleDTO;
import com.convecsa.erp.desempeno.dto.DesempenoListQuery;
import com.convecsa.erp.desempeno.dto.EvaluacionDesempenoRequest;
import com.convecsa.erp.desempeno.entity.EvaluacionDesempeno;
import com.convecsa.erp.desempeno.repository.EvaluacionDesempenoRepository;
import com.convecsa.erp.desempeno.service.impl.DesempenoServiceImpl;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class DesempenoServiceImplTest {

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private EvaluacionDesempenoRepository evaluacionRepository;

    @InjectMocks
    private DesempenoServiceImpl servicio;

    private Persona persona;

    @BeforeEach
    void setUp() {
        persona = new Persona();
        persona.setId(7L);
        persona.setNombres("Pedro");
        persona.setApellidos("Quispe");
        persona.setNumeroDocumento("12345678");
    }

    @Test
    @SuppressWarnings("unchecked")
    void listadoSinEvaluacionDevuelvePromedioNulo() {
        when(personaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(persona)));
        when(evaluacionRepository.findByPersonaIdInAndPeriodo(List.of(7L), "2026-Q3")).thenReturn(List.of());
        when(contratoRepository.findByPersonaIdInAndActivoTrueOrderByIdDesc(List.of(7L)))
                .thenReturn(List.of());

        PagedResponse<ColaboradorDesempenoDTO> page =
                servicio.listarColaboradores(new DesempenoListQuery("2026-Q3", 0, 20, null));

        assertEquals(1, page.getContent().size());
        ColaboradorDesempenoDTO item = page.getContent().get(0);
        assertEquals(7L, item.personaId());
        assertNull(item.promedio());
        assertNull(item.estado());
        assertNull(item.evaluacionId());
    }

    @Test
    void upsertActualizaLaMismaFila() {
        EvaluacionDesempeno existente = new EvaluacionDesempeno();
        existente.setId(21L);
        existente.setPersona(persona);
        existente.setPeriodo("2026-Q3");
        existente.setSeguridadSst(3);
        existente.setProductividad(3);
        existente.setCalidadTecnica(3);
        existente.setPuntualidad(3);
        existente.setTrabajoEquipo(3);
        existente.setMetaCumplida(0);
        existente.setEstado(EstadoEvaluacionDesempeno.PENDIENTE);

        when(personaRepository.findById(7L)).thenReturn(Optional.of(persona));
        when(evaluacionRepository.findByPersonaIdAndPeriodo(7L, "2026-Q3")).thenReturn(Optional.of(existente));
        when(evaluacionRepository.save(existente)).thenReturn(existente);
        when(contratoRepository.findFirstByPersonaIdAndActivoTrueOrderByIdDesc(7L))
                .thenReturn(Optional.empty());

        EvaluacionDesempenoRequest request = new EvaluacionDesempenoRequest();
        request.setPeriodo("2026-Q3");
        request.setSeguridadSst(5);
        request.setProductividad(4);
        request.setCalidadTecnica(4);
        request.setPuntualidad(5);
        request.setTrabajoEquipo(4);
        request.setMetaCumplida(95);
        request.setComentarios("Compromiso SSOMA");
        request.setEstado(EstadoEvaluacionDesempeno.COMPLETADA);

        ColaboradorDesempenoDetalleDTO detalle = servicio.guardarEvaluacion(7L, request);

        ArgumentCaptor<EvaluacionDesempeno> captor = ArgumentCaptor.forClass(EvaluacionDesempeno.class);
        verify(evaluacionRepository).save(captor.capture());
        assertSame(existente, captor.getValue());
        assertEquals(21L, detalle.evaluacion().id());
        assertEquals(new java.math.BigDecimal("4.4"), detalle.evaluacion().promedio());
        assertEquals(EstadoEvaluacionDesempeno.COMPLETADA, detalle.evaluacion().estado());
    }
}
