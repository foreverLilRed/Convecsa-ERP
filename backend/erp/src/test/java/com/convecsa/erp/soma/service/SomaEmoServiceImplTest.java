package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.domain.AccionAuditoriaObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
import com.convecsa.erp.persona.domain.AptitudMedica;
import com.convecsa.erp.persona.domain.CriticidadDocumento;
import com.convecsa.erp.persona.domain.EstadoDocumentoChecklist;
import com.convecsa.erp.persona.domain.TipoDocumentoChecklist;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.entity.PersonaDocumentoChecklist;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.AptitudEmo;
import com.convecsa.erp.soma.domain.TipoEmo;
import com.convecsa.erp.soma.dto.SomaEmoRequest;
import com.convecsa.erp.soma.entity.SomaEmoExamen;
import com.convecsa.erp.soma.repository.SomaEmoExamenRepository;
import com.convecsa.erp.soma.service.impl.SomaEmoServiceImpl;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SomaEmoServiceImplTest {

    @Mock
    private SomaEmoExamenRepository somaEmoExamenRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private DocumentoObraRepository documentoObraRepository;

    @Mock
    private AuditoriaObraRegistrar auditoriaObraRegistrar;

    @InjectMocks
    private SomaEmoServiceImpl servicio;

    private final List<SomaEmoExamen> almacenados = new ArrayList<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    @BeforeEach
    void stubPersistencia() {
        lenient().when(somaEmoExamenRepository.save(any(SomaEmoExamen.class))).thenAnswer(invocation -> {
            SomaEmoExamen examen = invocation.getArgument(0);
            if (examen.getId() == null) {
                examen.setId(secuencia.getAndIncrement());
            }
            almacenados.removeIf(item -> item.getId().equals(examen.getId()));
            almacenados.add(examen);
            return examen;
        });
        lenient()
                .when(somaEmoExamenRepository.findFirstByPersonaIdOrderByFechaExamenDescIdDesc(anyLong()))
                .thenAnswer(invocation -> {
                    Long personaId = invocation.getArgument(0);
                    return almacenados.stream()
                            .filter(item -> item.getPersona() != null && personaId.equals(item.getPersona().getId()))
                            .max(Comparator.comparing(SomaEmoExamen::getFechaExamen)
                                    .thenComparing(SomaEmoExamen::getId));
                });
    }

    @ParameterizedTest
    @CsvSource({
        "APTO, 2028-01-01, APTO, CONFORME",
        "APTO_CON_RESTRICCION, 2028-01-01, OBSERVADO, CONFORME",
        "OBSERVADO, 2028-01-01, OBSERVADO, OBSERVADO",
        "NO_APTO, 2028-01-01, OBSERVADO, OBSERVADO",
        "APTO, 2020-01-01, VENCIDO, PENDIENTE"
    })
    void ultimoEmoSincronizaPersonaYChecklist(
            AptitudEmo aptitudEmo,
            LocalDate vencimiento,
            AptitudMedica aptitudEsperada,
            EstadoDocumentoChecklist estadoEsperado) {
        Persona persona = persona(3L);
        when(personaRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(persona));

        servicio.registrar(null, request(3L, aptitudEmo, LocalDate.of(2020, 1, 1), vencimiento));

        assertEquals(aptitudEsperada, persona.getAptitudMedica());
        assertEquals(vencimiento, persona.getFechaVigenciaEmo());
        PersonaDocumentoChecklist fila = filaEmo(persona);
        assertEquals(estadoEsperado, fila.getEstado());
        assertEquals(CriticidadDocumento.CRITICO, fila.getCriticidad());
    }

    @Test
    void emoMasAntiguoNoPisaPersona() {
        Persona persona = persona(3L);
        persona.setAptitudMedica(AptitudMedica.APTO);
        persona.setFechaVigenciaEmo(LocalDate.of(2028, 6, 1));
        persona.setRestriccionesMedicas(null);
        SomaEmoExamen vigente = new SomaEmoExamen();
        vigente.setId(secuencia.getAndIncrement());
        vigente.setPersona(persona);
        vigente.setFechaExamen(LocalDate.of(2026, 6, 1));
        vigente.setFechaVencimiento(LocalDate.of(2028, 6, 1));
        vigente.setAptitud(AptitudEmo.APTO);
        almacenados.add(vigente);
        when(personaRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(persona));

        servicio.registrar(
                null,
                request(3L, AptitudEmo.NO_APTO, LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1)));

        assertEquals(AptitudMedica.APTO, persona.getAptitudMedica());
        assertEquals(LocalDate.of(2028, 6, 1), persona.getFechaVigenciaEmo());
        assertTrue(persona.getDocumentosChecklist().isEmpty());
    }

    @Test
    void creaFilaChecklistSiNoExiste() {
        Persona persona = persona(3L);
        when(personaRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(persona));

        servicio.registrar(
                null,
                request(3L, AptitudEmo.APTO, LocalDate.of(2026, 1, 1), LocalDate.of(2028, 1, 1)));

        assertEquals(1, persona.getDocumentosChecklist().size());
        PersonaDocumentoChecklist fila = filaEmo(persona);
        assertEquals(TipoDocumentoChecklist.EMO_APTO_VIGENTE, fila.getTipoDocumento());
        assertEquals(EstadoDocumentoChecklist.CONFORME, fila.getEstado());
        assertEquals(CriticidadDocumento.CRITICO, fila.getCriticidad());
        assertEquals(persona, fila.getPersona());
    }

    @Test
    void auditoriaSoloConObra() {
        Persona persona = persona(3L);
        Proyecto proyecto = new Proyecto();
        proyecto.setId(10L);
        when(personaRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(persona));
        when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));

        servicio.registrar(
                10L, request(3L, AptitudEmo.APTO, LocalDate.of(2026, 1, 1), LocalDate.of(2028, 1, 1)));
        servicio.registrar(
                null, request(3L, AptitudEmo.APTO, LocalDate.of(2026, 2, 1), LocalDate.of(2028, 2, 1)));

        verify(auditoriaObraRegistrar, times(1))
                .registrar(
                        eq(10L),
                        eq(AccionAuditoriaObra.REGISTRAR_EMO_SOMA),
                        eq(AuditoriaObraRegistrar.ENTIDAD_EMO_SOMA),
                        eq(1L),
                        any());
        verify(auditoriaObraRegistrar, times(1)).registrar(any(), any(), any(), any(), any());
    }

    @Test
    void restriccionesVaciasQuedanNull() {
        Persona persona = persona(3L);
        persona.setRestriccionesMedicas("altura");
        when(personaRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(persona));
        SomaEmoRequest request =
                request(3L, AptitudEmo.APTO, LocalDate.of(2026, 1, 1), LocalDate.of(2028, 1, 1));
        request.setRestricciones("   ");

        servicio.registrar(null, request);

        assertNull(persona.getRestriccionesMedicas());
    }

    @Test
    void mapeoDirectoCubreTablaDeDecision() {
        LocalDate hoy = LocalDate.of(2026, 11, 11);
        assertEquals(
                AptitudMedica.APTO,
                SomaEmoServiceImpl.mapear(AptitudEmo.APTO, LocalDate.of(2027, 1, 1), hoy).aptitudMedica());
        assertEquals(
                EstadoDocumentoChecklist.CONFORME,
                SomaEmoServiceImpl.mapear(AptitudEmo.APTO, LocalDate.of(2027, 1, 1), hoy).estadoChecklist());
        assertEquals(
                AptitudMedica.OBSERVADO,
                SomaEmoServiceImpl.mapear(AptitudEmo.APTO_CON_RESTRICCION, LocalDate.of(2027, 1, 1), hoy)
                        .aptitudMedica());
        assertEquals(
                EstadoDocumentoChecklist.CONFORME,
                SomaEmoServiceImpl.mapear(AptitudEmo.APTO_CON_RESTRICCION, LocalDate.of(2027, 1, 1), hoy)
                        .estadoChecklist());
        assertEquals(
                AptitudMedica.OBSERVADO,
                SomaEmoServiceImpl.mapear(AptitudEmo.OBSERVADO, LocalDate.of(2027, 1, 1), hoy).aptitudMedica());
        assertEquals(
                EstadoDocumentoChecklist.OBSERVADO,
                SomaEmoServiceImpl.mapear(AptitudEmo.NO_APTO, LocalDate.of(2027, 1, 1), hoy).estadoChecklist());
        assertEquals(
                AptitudMedica.VENCIDO,
                SomaEmoServiceImpl.mapear(AptitudEmo.APTO, LocalDate.of(2026, 11, 10), hoy).aptitudMedica());
        assertEquals(
                EstadoDocumentoChecklist.PENDIENTE,
                SomaEmoServiceImpl.mapear(AptitudEmo.NO_APTO, LocalDate.of(2026, 11, 10), hoy).estadoChecklist());
    }

    private static Persona persona(Long id) {
        Persona persona = new Persona();
        persona.setId(id);
        persona.setNombres("Ana");
        persona.setApellidos("García");
        return persona;
    }

    private static SomaEmoRequest request(
            Long personaId, AptitudEmo aptitud, LocalDate fechaExamen, LocalDate fechaVencimiento) {
        SomaEmoRequest request = new SomaEmoRequest();
        request.setPersonaId(personaId);
        request.setTipoEmo(TipoEmo.INGRESO);
        request.setClinicaNombre("Clínica Lima");
        request.setFechaExamen(fechaExamen);
        request.setFechaVencimiento(fechaVencimiento);
        request.setAptitud(aptitud);
        return request;
    }

    private static PersonaDocumentoChecklist filaEmo(Persona persona) {
        return persona.getDocumentosChecklist().stream()
                .filter(item -> item.getTipoDocumento() == TipoDocumentoChecklist.EMO_APTO_VIGENTE)
                .findFirst()
                .orElseThrow();
    }
}
