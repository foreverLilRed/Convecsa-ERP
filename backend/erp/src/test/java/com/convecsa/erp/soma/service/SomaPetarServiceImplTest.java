package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.domain.AccionAuditoriaObra;
import com.convecsa.erp.construccion.domain.EstadoPeriodoCc;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import com.convecsa.erp.construccion.repository.CuadrillaObraRepository;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.PartidaPresupuestoRepository;
import com.convecsa.erp.construccion.repository.PeriodoCcRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ProyectoResponsableRepository;
import com.convecsa.erp.construccion.repository.TareoCcDiaRepository;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.AptitudEmo;
import com.convecsa.erp.soma.domain.ChecklistPetar;
import com.convecsa.erp.soma.domain.EstadoHabilitacionSoma;
import com.convecsa.erp.soma.domain.EstadoPetar;
import com.convecsa.erp.soma.domain.TipoTrabajoPetar;
import com.convecsa.erp.soma.dto.SomaHabilitacionDTO;
import com.convecsa.erp.soma.dto.SomaHabilitacionFilaDTO;
import com.convecsa.erp.soma.dto.SomaPetarDTO;
import com.convecsa.erp.soma.dto.SomaPetarFaltanteTareoDTO;
import com.convecsa.erp.soma.entity.SomaEmoExamen;
import com.convecsa.erp.soma.entity.SomaPetar;
import com.convecsa.erp.soma.entity.SomaPetarTrabajador;
import com.convecsa.erp.soma.repository.SomaEmoExamenRepository;
import com.convecsa.erp.soma.repository.SomaPetarRepository;
import com.convecsa.erp.soma.service.impl.SomaPetarServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
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
class SomaPetarServiceImplTest {

    @Mock
    private SomaPetarRepository somaPetarRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private CuadrillaObraRepository cuadrillaObraRepository;

    @Mock
    private PartidaPresupuestoRepository partidaPresupuestoRepository;

    @Mock
    private ProyectoResponsableRepository proyectoResponsableRepository;

    @Mock
    private DocumentoObraRepository documentoObraRepository;

    @Mock
    private PeriodoCcRepository periodoCcRepository;

    @Mock
    private TareoCcDiaRepository tareoCcDiaRepository;

    @Mock
    private SomaEmoExamenRepository somaEmoExamenRepository;

    @Mock
    private InterfazSomaHabilitacionService somaHabilitacionService;

    @Mock
    private AuditoriaObraRegistrar auditoriaObraRegistrar;

    @InjectMocks
    private SomaPetarServiceImpl servicio;

    private Proyecto proyecto;
    private Persona trabajador;
    private Persona residente;
    private Persona supervisor;

    @BeforeEach
    void stubBasico() {
        proyecto = new Proyecto();
        proyecto.setId(10L);
        trabajador = persona(20L, "Ana", "Perez");
        residente = persona(30L, "Luis", "Residente");
        supervisor = persona(40L, "Sofia", "Supervisor");
        lenient().when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
    }

    @Test
    void transicionesValidasEInvalidas() {
        SomaPetar petar = petarListoParaAutorizar();
        stubPetar(petar);
        stubHabilitado();
        lenient().when(somaEmoExamenRepository.findFirstByPersonaIdOrderByFechaExamenDescIdDesc(20L))
                .thenReturn(Optional.empty());

        IllegalArgumentException invalida =
                assertThrows(IllegalArgumentException.class, () -> servicio.cerrar(10L, 1L));
        assertTrue(invalida.getMessage().contains("Transición no permitida"));

        SomaPetarDTO autorizado = servicio.autorizar(10L, 1L);
        assertEquals(EstadoPetar.AUTORIZADO, autorizado.getEstado());
        verify(auditoriaObraRegistrar)
                .registrar(
                        eq(10L),
                        eq(AccionAuditoriaObra.AUTORIZAR_PETAR),
                        eq(AuditoriaObraRegistrar.ENTIDAD_PETAR_SOMA),
                        eq(1L),
                        eq("PETAR N° 1 ALTURA"));

        SomaPetarDTO suspendido = servicio.suspender(10L, 1L);
        assertEquals(EstadoPetar.SUSPENDIDO, suspendido.getEstado());
        SomaPetarDTO reanudado = servicio.reanudar(10L, 1L);
        assertEquals(EstadoPetar.AUTORIZADO, reanudado.getEstado());
        SomaPetarDTO cerrado = servicio.cerrar(10L, 1L);
        assertEquals(EstadoPetar.CERRADO, cerrado.getEstado());

        IllegalArgumentException desdeCerrado =
                assertThrows(IllegalArgumentException.class, () -> servicio.suspender(10L, 1L));
        assertTrue(desdeCerrado.getMessage().contains("Transición no permitida"));
    }

    @Test
    void autorizarRechazaBloqueadoRestriccionAlturaYChecklistIncompleto() {
        SomaPetar petar = petarListoParaAutorizar();
        stubPetar(petar);
        when(somaHabilitacionService.evaluarObra(eq(10L), any()))
                .thenReturn(habilitacion(EstadoHabilitacionSoma.BLOQUEADO));
        IllegalArgumentException bloqueado =
                assertThrows(IllegalArgumentException.class, () -> servicio.autorizar(10L, 1L));
        assertTrue(bloqueado.getMessage().contains("HABILITADO"));

        stubHabilitado();
        SomaEmoExamen emo = new SomaEmoExamen();
        emo.setAptitud(AptitudEmo.APTO_CON_RESTRICCION);
        emo.setRestricciones("Trabajo en Áltura");
        when(somaEmoExamenRepository.findFirstByPersonaIdOrderByFechaExamenDescIdDesc(20L))
                .thenReturn(Optional.of(emo));
        IllegalArgumentException altura =
                assertThrows(IllegalArgumentException.class, () -> servicio.autorizar(10L, 1L));
        assertTrue(altura.getMessage().toLowerCase().contains("altura"));

        when(somaEmoExamenRepository.findFirstByPersonaIdOrderByFechaExamenDescIdDesc(20L))
                .thenReturn(Optional.empty());
        petar.getChecklist().put("charlaAts", false);
        IllegalArgumentException checklist =
                assertThrows(IllegalArgumentException.class, () -> servicio.autorizar(10L, 1L));
        assertTrue(checklist.getMessage().toLowerCase().contains("checklist"));
    }

    @Test
    void faltantesTareoAlturaSinPetarYCubierto() {
        PeriodoCc periodo = new PeriodoCc();
        periodo.setId(3L);
        when(periodoCcRepository.findByProyectoIdAndEstado(10L, EstadoPeriodoCc.BORRADOR)).thenReturn(List.of(periodo));
        TareoCcDia dia = new TareoCcDia();
        dia.setFecha(LocalDate.of(2026, 11, 13));
        dia.setAltura(true);
        when(tareoCcDiaRepository.findByPeriodoIdIn(List.of(3L))).thenReturn(List.of(dia));
        when(somaPetarRepository.findByProyectoIdAndTipoTrabajoAndEstadoIn(
                        eq(10L), eq(TipoTrabajoPetar.ALTURA), any()))
                .thenReturn(List.of());
        when(somaPetarRepository.findByProyectoIdAndTipoTrabajoAndEstadoIn(
                        eq(10L), eq(TipoTrabajoPetar.EXCAVACION_ZANJA), any()))
                .thenReturn(List.of());

        List<SomaPetarFaltanteTareoDTO> faltantes = servicio.faltantesTareo(10L);
        assertEquals(1, faltantes.size());
        assertEquals(LocalDate.of(2026, 11, 13), faltantes.get(0).getFecha());
        assertEquals(TipoTrabajoPetar.ALTURA, faltantes.get(0).getTipoTrabajo());

        SomaPetar cubre = new SomaPetar();
        cubre.setFecha(LocalDate.of(2026, 11, 13));
        cubre.setTipoTrabajo(TipoTrabajoPetar.ALTURA);
        cubre.setEstado(EstadoPetar.AUTORIZADO);
        when(somaPetarRepository.findByProyectoIdAndTipoTrabajoAndEstadoIn(
                        eq(10L), eq(TipoTrabajoPetar.ALTURA), any()))
                .thenReturn(List.of(cubre));
        assertTrue(servicio.faltantesTareo(10L).isEmpty());
    }

    private SomaPetar petarListoParaAutorizar() {
        SomaPetar petar = new SomaPetar();
        petar.setId(1L);
        petar.setProyecto(proyecto);
        petar.setNumero(1);
        petar.setTipoTrabajo(TipoTrabajoPetar.ALTURA);
        petar.setFecha(LocalDate.of(2026, 11, 13));
        petar.setHoraInicio(LocalTime.of(7, 0));
        petar.setHoraFin(LocalTime.of(17, 0));
        petar.setUbicacionFrente("Frente A");
        petar.setDescripcionTrabajo("Andamio");
        petar.setEstado(EstadoPetar.BORRADOR);
        petar.setResidente(residente);
        petar.setSupervisorSsoma(supervisor);
        Map<String, Boolean> checklist = new LinkedHashMap<>();
        for (String clave : ChecklistPetar.itemsPara(TipoTrabajoPetar.ALTURA)) {
            checklist.put(clave, true);
        }
        petar.setChecklist(checklist);
        petar.setMediciones(new LinkedHashMap<>(Map.of("vientoKmh", new BigDecimal("12"))));
        SomaPetarTrabajador fila = new SomaPetarTrabajador();
        fila.setPersona(trabajador);
        fila.setRolEnTarea("EJECUTANTE");
        petar.addTrabajador(fila);
        return petar;
    }

    private void stubPetar(SomaPetar petar) {
        when(somaPetarRepository.findByIdAndProyectoId(1L, 10L)).thenReturn(Optional.of(petar));
    }

    private void stubHabilitado() {
        when(somaHabilitacionService.evaluarObra(eq(10L), any())).thenReturn(habilitacion(EstadoHabilitacionSoma.HABILITADO));
    }

    private SomaHabilitacionDTO habilitacion(EstadoHabilitacionSoma estado) {
        return SomaHabilitacionDTO.builder()
                .fecha(LocalDate.of(2026, 11, 13))
                .habilitados(estado == EstadoHabilitacionSoma.HABILITADO ? 1 : 0)
                .bloqueados(estado == EstadoHabilitacionSoma.BLOQUEADO ? 1 : 0)
                .emosPorVencer30Dias(0)
                .filas(List.of(SomaHabilitacionFilaDTO.builder()
                        .personaId(20L)
                        .nombres("Ana")
                        .apellidos("Perez")
                        .numeroDocumento("123")
                        .estado(estado)
                        .motivos(List.of())
                        .restricciones(null)
                        .build()))
                .build();
    }

    private static Persona persona(Long id, String nombres, String apellidos) {
        Persona persona = new Persona();
        persona.setId(id);
        persona.setNombres(nombres);
        persona.setApellidos(apellidos);
        persona.setNumeroDocumento(String.valueOf(id));
        return persona;
    }
}
