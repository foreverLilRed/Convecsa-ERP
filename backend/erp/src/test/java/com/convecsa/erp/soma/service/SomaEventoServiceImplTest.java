package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.domain.CodigoTareoCc;
import com.convecsa.erp.construccion.domain.EstadoPeriodoCc;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.PartidaPresupuestoRepository;
import com.convecsa.erp.construccion.repository.PeriodoCcRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.SubcontratoObraRepository;
import com.convecsa.erp.construccion.repository.TareoCcDiaRepository;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.EstadoHallazgoSoma;
import com.convecsa.erp.soma.domain.EstadoInvestigacionSst;
import com.convecsa.erp.soma.domain.GravedadIncapacidadSst;
import com.convecsa.erp.soma.domain.TipoEventoSst;
import com.convecsa.erp.soma.dto.SomaEventoDTO;
import com.convecsa.erp.soma.dto.SomaEventoRequest;
import com.convecsa.erp.soma.dto.SomaEventoSyncDmDTO;
import com.convecsa.erp.soma.entity.SomaEventoSst;
import com.convecsa.erp.soma.entity.SomaHallazgo;
import com.convecsa.erp.soma.repository.SomaEventoSstRepository;
import com.convecsa.erp.soma.repository.SomaHallazgoRepository;
import com.convecsa.erp.soma.service.impl.SomaEventoServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SomaEventoServiceImplTest {

    @Mock
    private SomaEventoSstRepository somaEventoSstRepository;

    @Mock
    private SomaHallazgoRepository somaHallazgoRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private PartidaPresupuestoRepository partidaPresupuestoRepository;

    @Mock
    private SubcontratoObraRepository subcontratoObraRepository;

    @Mock
    private DocumentoObraRepository documentoObraRepository;

    @Mock
    private PeriodoCcRepository periodoCcRepository;

    @Mock
    private TareoCcDiaRepository tareoCcDiaRepository;

    @Mock
    private InterfazSomaInspeccionService somaInspeccionService;

    @Mock
    private AuditoriaObraRegistrar auditoriaObraRegistrar;

    @InjectMocks
    private SomaEventoServiceImpl servicio;

    private Proyecto proyecto;
    private Persona persona;

    @BeforeEach
    void stubBasico() {
        proyecto = new Proyecto();
        proyecto.setId(10L);
        persona = new Persona();
        persona.setId(20L);
        persona.setApellidos("Perez");
        persona.setNombres("Ana");
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
    }

    @Test
    void incapacitanteSinGravedadSeRechazaYIncidenteConGravedadTambien() {
        SomaEventoRequest incapacitante = requestBase(TipoEventoSst.ACCIDENTE_INCAPACITANTE);
        incapacitante.setPersonaAfectadaId(20L);
        incapacitante.setGravedadIncapacidad(null);
        IllegalArgumentException sinGravedad =
                assertThrows(IllegalArgumentException.class, () -> servicio.crear(10L, incapacitante));
        assertEquals("La gravedad es obligatoria en accidente incapacitante", sinGravedad.getMessage());

        SomaEventoRequest incidente = requestBase(TipoEventoSst.INCIDENTE);
        incidente.setGravedadIncapacidad(GravedadIncapacidadSst.TOTAL_TEMPORAL);
        IllegalArgumentException conGravedad =
                assertThrows(IllegalArgumentException.class, () -> servicio.crear(10L, incidente));
        assertEquals("La gravedad solo aplica a accidente incapacitante", conGravedad.getMessage());
    }

    @Test
    void accidenteLeveExigePersonaOSubcontratoEIncidenteNo() {
        SomaEventoRequest leve = requestBase(TipoEventoSst.ACCIDENTE_LEVE);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> servicio.crear(10L, leve));
        assertEquals("Indique la persona afectada o el subcontrato", ex.getMessage());

        when(somaEventoSstRepository.findMaxNumero(10L)).thenReturn(0);
        when(somaEventoSstRepository.save(any(SomaEventoSst.class))).thenAnswer(invocation -> {
            SomaEventoSst fila = invocation.getArgument(0);
            fila.setId(1L);
            return fila;
        });
        SomaEventoRequest incidente = requestBase(TipoEventoSst.INCIDENTE);
        SomaEventoDTO dto = servicio.crear(10L, incidente);
        assertEquals(TipoEventoSst.INCIDENTE, dto.getTipoEvento());
        assertEquals(1, dto.getNumero());
        assertEquals(EstadoInvestigacionSst.EN_INVESTIGACION, dto.getEstadoInvestigacion());
    }

    @Test
    void rangoDmCalculaTresDiasCalendario() {
        when(somaEventoSstRepository.findMaxNumero(10L)).thenReturn(2);
        when(somaEventoSstRepository.save(any(SomaEventoSst.class))).thenAnswer(invocation -> {
            SomaEventoSst fila = invocation.getArgument(0);
            fila.setId(3L);
            return fila;
        });
        SomaEventoRequest request = requestBase(TipoEventoSst.INCIDENTE);
        request.setFechaInicioDm(LocalDate.of(2026, 11, 1));
        request.setFechaFinDm(LocalDate.of(2026, 11, 3));
        SomaEventoDTO dto = servicio.crear(10L, request);
        assertEquals(3, dto.getDiasDescansoMedico());
    }

    @Test
    void cerrarConHallazgoAbiertoFallaYConLevantadosCierra() {
        SomaHallazgo abierto = new SomaHallazgo();
        abierto.setId(8L);
        abierto.setEstado(EstadoHallazgoSoma.ABIERTO);
        SomaEventoSst conAbierto = eventoBase();
        conAbierto.getHallazgos().add(abierto);
        when(somaEventoSstRepository.findByIdAndProyectoId(4L, 10L)).thenReturn(Optional.of(conAbierto));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> servicio.cerrar(10L, 4L));
        assertEquals("No se puede cerrar: hay hallazgos sin levantar", ex.getMessage());

        SomaHallazgo levantado = new SomaHallazgo();
        levantado.setId(9L);
        levantado.setEstado(EstadoHallazgoSoma.LEVANTADO);
        SomaEventoSst listo = eventoBase();
        listo.getHallazgos().add(levantado);
        when(somaEventoSstRepository.findByIdAndProyectoId(5L, 10L)).thenReturn(Optional.of(listo));
        SomaEventoDTO dto = servicio.cerrar(10L, 5L);
        assertEquals(EstadoInvestigacionSst.CERRADO, dto.getEstadoInvestigacion());
    }

    @Test
    void sincronizarDmSoloEscribeEnPeriodoBorrador() {
        SomaEventoSst evento = eventoBase();
        evento.setPersonaAfectada(persona);
        evento.setFechaInicioDm(LocalDate.of(2026, 11, 1));
        evento.setFechaFinDm(LocalDate.of(2026, 11, 3));
        when(somaEventoSstRepository.findByIdAndProyectoId(7L, 10L)).thenReturn(Optional.of(evento));

        PeriodoCc borrador = new PeriodoCc();
        borrador.setId(100L);
        borrador.setFechaDesde(LocalDate.of(2026, 11, 1));
        borrador.setFechaHasta(LocalDate.of(2026, 11, 1));
        borrador.setEstado(EstadoPeriodoCc.BORRADOR);

        PeriodoCc cerrado = new PeriodoCc();
        cerrado.setId(101L);
        cerrado.setFechaDesde(LocalDate.of(2026, 11, 2));
        cerrado.setFechaHasta(LocalDate.of(2026, 11, 2));
        cerrado.setEstado(EstadoPeriodoCc.CERRADO);

        when(periodoCcRepository.findByProyectoIdOrderByFechaDesdeDesc(10L)).thenReturn(List.of(borrador, cerrado));

        TareoCcDia existente = new TareoCcDia();
        existente.setId(50L);
        existente.setPeriodo(borrador);
        existente.setPersona(persona);
        existente.setFecha(LocalDate.of(2026, 11, 1));
        existente.setCodigo(CodigoTareoCc.LAB);
        existente.setHorasOrd(new BigDecimal("8"));
        existente.setAltura(true);
        when(tareoCcDiaRepository.findByPeriodoIdAndPersonaIdAndFecha(100L, 20L, LocalDate.of(2026, 11, 1)))
                .thenReturn(Optional.of(existente));
        when(tareoCcDiaRepository.save(any(TareoCcDia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SomaEventoSyncDmDTO dto = servicio.sincronizarDm(10L, 7L);

        assertEquals(List.of(LocalDate.of(2026, 11, 1)), dto.getAplicados());
        assertEquals(2, dto.getOmitidos().size());
        assertEquals(LocalDate.of(2026, 11, 2), dto.getOmitidos().get(0).getFecha());
        assertEquals("periodo cerrado", dto.getOmitidos().get(0).getMotivo());
        assertEquals(LocalDate.of(2026, 11, 3), dto.getOmitidos().get(1).getFecha());
        assertEquals("sin periodo", dto.getOmitidos().get(1).getMotivo());

        ArgumentCaptor<TareoCcDia> captor = ArgumentCaptor.forClass(TareoCcDia.class);
        verify(tareoCcDiaRepository).save(captor.capture());
        TareoCcDia guardado = captor.getValue();
        assertEquals(CodigoTareoCc.DM, guardado.getCodigo());
        assertEquals(0, BigDecimal.ZERO.compareTo(guardado.getHorasOrd()));
        assertEquals(0, BigDecimal.ZERO.compareTo(guardado.getHe60()));
        assertEquals(0, BigDecimal.ZERO.compareTo(guardado.getHe100()));
        assertTrue(guardado.isAltura());
        verify(tareoCcDiaRepository, never())
                .findByPeriodoIdAndPersonaIdAndFecha(anyLong(), anyLong(), org.mockito.ArgumentMatchers.eq(LocalDate.of(2026, 11, 2)));
        verify(tareoCcDiaRepository, never())
                .findByPeriodoIdAndPersonaIdAndFecha(anyLong(), anyLong(), org.mockito.ArgumentMatchers.eq(LocalDate.of(2026, 11, 3)));
    }

    private SomaEventoSst eventoBase() {
        SomaEventoSst evento = new SomaEventoSst();
        evento.setId(4L);
        evento.setProyecto(proyecto);
        evento.setNumero(1);
        evento.setTipoEvento(TipoEventoSst.INCIDENTE);
        evento.setFechaHora(Instant.parse("2026-11-01T10:00:00Z"));
        evento.setLugarExacto("Frente A");
        evento.setDescripcionEvento("Caida de material");
        evento.setEstadoInvestigacion(EstadoInvestigacionSst.EN_INVESTIGACION);
        evento.setHallazgos(new ArrayList<>());
        return evento;
    }

    private SomaEventoRequest requestBase(TipoEventoSst tipo) {
        SomaEventoRequest request = new SomaEventoRequest();
        request.setTipoEvento(tipo);
        request.setFechaHora(Instant.parse("2026-11-01T10:00:00Z"));
        request.setLugarExacto("Frente A");
        request.setDescripcionEvento("Caida de material");
        return request;
    }
}
