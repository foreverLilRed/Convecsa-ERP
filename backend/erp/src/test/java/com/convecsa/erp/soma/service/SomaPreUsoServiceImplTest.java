package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.entity.AsignacionEquipo;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.AsignacionEquipoRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.ChecklistPreUso;
import com.convecsa.erp.soma.domain.ResultadoPreUsoEquipo;
import com.convecsa.erp.soma.dto.SomaPreUsoBloqueoDTO;
import com.convecsa.erp.soma.dto.SomaPreUsoRequest;
import com.convecsa.erp.soma.entity.SomaInspeccionEquipo;
import com.convecsa.erp.soma.repository.SomaInspeccionEquipoRepository;
import com.convecsa.erp.soma.service.impl.SomaPreUsoServiceImpl;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SomaPreUsoServiceImplTest {

    @Mock
    private SomaInspeccionEquipoRepository somaInspeccionEquipoRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private AsignacionEquipoRepository asignacionEquipoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private SomaPreUsoServiceImpl servicio;

    private Proyecto proyecto;
    private AsignacionEquipo asignacion;

    @BeforeEach
    void stubBasico() {
        proyecto = new Proyecto();
        proyecto.setId(10L);
        asignacion = new AsignacionEquipo();
        asignacion.setId(5L);
        asignacion.setProyecto(proyecto);
        asignacion.setCodigo("EQ-01");
        asignacion.setDescripcion("Retroexcavadora");
        lenient().when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        lenient().when(asignacionEquipoRepository.findByIdAndProyectoId(5L, 10L)).thenReturn(Optional.of(asignacion));
        lenient().when(somaInspeccionEquipoRepository.save(any(SomaInspeccionEquipo.class))).thenAnswer(invocation -> {
            SomaInspeccionEquipo fila = invocation.getArgument(0);
            if (fila.getId() == null) {
                fila.setId(1L);
            }
            return fila;
        });
    }

    @Test
    void operativoConItemFalsoSeRechaza() {
        SomaPreUsoRequest request = requestBase(ResultadoPreUsoEquipo.OPERATIVO);
        request.getChecklist().put("frenos", false);
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> servicio.crear(10L, request));
        assertTrue(ex.getMessage().contains("OPERATIVO"));
    }

    @Test
    void asignacionDeOtraObraSeRechaza() {
        when(asignacionEquipoRepository.findByIdAndProyectoId(5L, 10L)).thenReturn(Optional.empty());
        SomaPreUsoRequest request = requestBase(ResultadoPreUsoEquipo.OPERATIVO);
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> servicio.crear(10L, request));
        assertEquals("La asignación no pertenece a esta obra", ex.getMessage());
    }

    @Test
    void bloqueoSoloElDiaDelInoperativo() {
        LocalDate dia = LocalDate.of(2026, 11, 13);
        when(somaInspeccionEquipoRepository.existsByAsignacionIdAndFechaAndResultado(
                        5L, dia, ResultadoPreUsoEquipo.INOPERATIVO_BLOQUEADO))
                .thenReturn(true);
        when(somaInspeccionEquipoRepository.existsByAsignacionIdAndFechaAndResultado(
                        5L, dia.plusDays(1), ResultadoPreUsoEquipo.INOPERATIVO_BLOQUEADO))
                .thenReturn(false);
        SomaInspeccionEquipo fila = new SomaInspeccionEquipo();
        fila.setAsignacion(asignacion);
        fila.setFecha(dia);
        fila.setResultado(ResultadoPreUsoEquipo.INOPERATIVO_BLOQUEADO);
        when(somaInspeccionEquipoRepository.findByProyectoIdAndFechaAndResultado(
                        10L, dia, ResultadoPreUsoEquipo.INOPERATIVO_BLOQUEADO))
                .thenReturn(List.of(fila));
        when(somaInspeccionEquipoRepository.findByProyectoIdAndFechaAndResultado(
                        10L, dia.plusDays(1), ResultadoPreUsoEquipo.INOPERATIVO_BLOQUEADO))
                .thenReturn(List.of());

        assertTrue(servicio.equipoBloqueadoEn(5L, dia));
        List<SomaPreUsoBloqueoDTO> bloqueos = servicio.listarBloqueos(10L, dia);
        assertEquals(1, bloqueos.size());
        assertEquals(5L, bloqueos.get(0).getAsignacionId());
        assertEquals("EQ-01", bloqueos.get(0).getCodigo());

        assertFalse(servicio.equipoBloqueadoEn(5L, dia.plusDays(1)));
        assertTrue(servicio.listarBloqueos(10L, dia.plusDays(1)).isEmpty());
    }

    private SomaPreUsoRequest requestBase(ResultadoPreUsoEquipo resultado) {
        SomaPreUsoRequest request = new SomaPreUsoRequest();
        request.setAsignacionEquipoId(5L);
        request.setFecha(LocalDate.of(2026, 11, 13));
        request.setResultado(resultado);
        Map<String, Boolean> checklist = new LinkedHashMap<>();
        for (String clave : ChecklistPreUso.items()) {
            checklist.put(clave, true);
        }
        request.setChecklist(checklist);
        return request;
    }
}
