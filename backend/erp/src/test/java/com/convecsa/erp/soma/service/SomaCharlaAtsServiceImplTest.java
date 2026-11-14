package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.domain.RolResponsableObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.ProyectoResponsable;
import com.convecsa.erp.construccion.repository.CuadrillaMiembroRepository;
import com.convecsa.erp.construccion.repository.CuadrillaObraRepository;
import com.convecsa.erp.construccion.repository.PartidaPresupuestoRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ProyectoResponsableRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.EstadoHabilitacionSoma;
import com.convecsa.erp.soma.domain.PasoAts;
import com.convecsa.erp.soma.dto.SomaCharlaAsistenteRequest;
import com.convecsa.erp.soma.dto.SomaCharlaAtsDTO;
import com.convecsa.erp.soma.dto.SomaCharlaAtsRequest;
import com.convecsa.erp.soma.dto.SomaHabilitacionDTO;
import com.convecsa.erp.soma.dto.SomaHabilitacionFilaDTO;
import com.convecsa.erp.soma.entity.SomaCharlaAts;
import com.convecsa.erp.soma.repository.SomaCharlaAtsRepository;
import com.convecsa.erp.soma.service.impl.SomaCharlaAtsServiceImpl;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SomaCharlaAtsServiceImplTest {

    @Mock
    private SomaCharlaAtsRepository somaCharlaAtsRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private CuadrillaObraRepository cuadrillaObraRepository;

    @Mock
    private CuadrillaMiembroRepository cuadrillaMiembroRepository;

    @Mock
    private PartidaPresupuestoRepository partidaPresupuestoRepository;

    @Mock
    private ProyectoResponsableRepository proyectoResponsableRepository;

    @Mock
    private InterfazSomaHabilitacionService somaHabilitacionService;

    @InjectMocks
    private SomaCharlaAtsServiceImpl servicio;

    private Proyecto proyecto;
    private Persona trabajador;
    private Persona supervisor;

    @BeforeEach
    void stubBasico() {
        proyecto = new Proyecto();
        proyecto.setId(10L);
        trabajador = persona(1L, "Pedro", "Rojas");
        supervisor = persona(9L, "Ana", "Soto");
        lenient().when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        lenient().when(personaRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        lenient().when(personaRepository.findById(9L)).thenReturn(Optional.of(supervisor));
        lenient().when(proyectoResponsableRepository.findByProyectoIdOrderByRolAsc(10L)).thenReturn(List.of());
        lenient().when(somaCharlaAtsRepository.save(any(SomaCharlaAts.class))).thenAnswer(invocation -> {
            SomaCharlaAts charla = invocation.getArgument(0);
            if (charla.getId() == null) {
                charla.setId(44L);
            }
            return charla;
        });
    }

    @Test
    void sinPasosRechaza() {
        SomaCharlaAtsRequest vacia = requestValida();
        vacia.setPasos(List.of());
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(10L, vacia));

        SomaCharlaAtsRequest enBlanco = requestValida();
        enBlanco.setPasos(List.of(new PasoAts("  ", "caída", "arnés")));
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> servicio.crear(10L, enBlanco));
        assertEquals("El ATS debe tener al menos un paso", ex.getMessage());
    }

    @Test
    void asistenteBloqueadoSeGuardaConAdvertencia() {
        when(somaHabilitacionService.evaluarObra(10L, LocalDate.of(2026, 11, 13)))
                .thenReturn(semaforo(EstadoHabilitacionSoma.BLOQUEADO));

        SomaCharlaAtsDTO dto = servicio.crear(10L, requestValida());

        assertEquals(44L, dto.getId());
        assertFalse(dto.getAsistentes().get(0).isHabilitadoSoma());
        assertEquals("Asistentes no habilitados SOMA: Rojas, Pedro", dto.getAdvertencia());
    }

    @Test
    void asistenteHabilitadoSinAdvertencia() {
        when(somaHabilitacionService.evaluarObra(10L, LocalDate.of(2026, 11, 13)))
                .thenReturn(semaforo(EstadoHabilitacionSoma.HABILITADO));

        SomaCharlaAtsDTO dto = servicio.crear(10L, requestValida());

        assertTrue(dto.getAsistentes().get(0).isHabilitadoSoma());
        assertNull(dto.getAdvertencia());
    }

    @Test
    void supervisorOmitidoSeRellenaDesdeProyecto() {
        when(somaHabilitacionService.evaluarObra(10L, LocalDate.of(2026, 11, 13)))
                .thenReturn(semaforo(EstadoHabilitacionSoma.HABILITADO));
        ProyectoResponsable responsable = new ProyectoResponsable();
        responsable.setRol(RolResponsableObra.SUPERVISOR_SSOMA);
        responsable.setPersona(supervisor);
        when(proyectoResponsableRepository.findByProyectoIdOrderByRolAsc(10L)).thenReturn(List.of(responsable));

        SomaCharlaAtsRequest request = requestValida();
        request.setSupervisorSsomaId(null);

        SomaCharlaAtsDTO dto = servicio.crear(10L, request);

        assertEquals(9L, dto.getSupervisorSsomaId());
        assertEquals("Soto, Ana", dto.getSupervisorSsomaNombre());
    }

    private SomaCharlaAtsRequest requestValida() {
        SomaCharlaAsistenteRequest asistente = new SomaCharlaAsistenteRequest();
        asistente.setPersonaId(1L);
        SomaCharlaAtsRequest request = new SomaCharlaAtsRequest();
        request.setFecha(LocalDate.of(2026, 11, 13));
        request.setHoraInicio(LocalTime.of(7, 0));
        request.setTemaCharla("Charla de izaje");
        request.setPasos(List.of(new PasoAts("Izaje de viga", "Caída de carga", "Señalero y izaje certificado")));
        request.setAsistentes(List.of(asistente));
        return request;
    }

    private SomaHabilitacionDTO semaforo(EstadoHabilitacionSoma estado) {
        return SomaHabilitacionDTO.builder()
                .fecha(LocalDate.of(2026, 11, 13))
                .habilitados(estado == EstadoHabilitacionSoma.HABILITADO ? 1 : 0)
                .bloqueados(estado == EstadoHabilitacionSoma.BLOQUEADO ? 1 : 0)
                .emosPorVencer30Dias(0)
                .filas(List.of(SomaHabilitacionFilaDTO.builder()
                        .personaId(1L)
                        .nombres("Pedro")
                        .apellidos("Rojas")
                        .numeroDocumento("123")
                        .estado(estado)
                        .motivos(List.of())
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
