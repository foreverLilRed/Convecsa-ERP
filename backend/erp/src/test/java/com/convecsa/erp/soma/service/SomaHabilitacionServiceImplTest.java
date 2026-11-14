package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.persona.domain.EstadoDocumentoChecklist;
import com.convecsa.erp.persona.domain.TipoDocumentoChecklist;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.entity.PersonaDocumentoChecklist;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.AptitudEmo;
import com.convecsa.erp.soma.domain.EstadoHabilitacionSoma;
import com.convecsa.erp.soma.domain.EstadoSctrPoliza;
import com.convecsa.erp.soma.domain.MotivoEntregaEpp;
import com.convecsa.erp.soma.domain.TipoCapacitacionSoma;
import com.convecsa.erp.soma.dto.SomaHabilitacionDTO;
import com.convecsa.erp.soma.dto.SomaHabilitacionFilaDTO;
import com.convecsa.erp.soma.entity.SomaEmoExamen;
import com.convecsa.erp.soma.entity.SomaSctrNomina;
import com.convecsa.erp.soma.entity.SomaSctrPoliza;
import com.convecsa.erp.soma.repository.SomaCapacitacionRepository;
import com.convecsa.erp.soma.repository.SomaEmoExamenRepository;
import com.convecsa.erp.soma.repository.SomaEntregaEppRepository;
import com.convecsa.erp.soma.repository.SomaSctrPolizaRepository;
import com.convecsa.erp.soma.repository.SomaTrabajadorObraRepository;
import com.convecsa.erp.soma.service.impl.SomaHabilitacionServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SomaHabilitacionServiceImplTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 13);

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private SomaTrabajadorObraRepository somaTrabajadorObraRepository;

    @Mock
    private SomaEmoExamenRepository somaEmoExamenRepository;

    @Mock
    private SomaSctrPolizaRepository somaSctrPolizaRepository;

    @Mock
    private SomaCapacitacionRepository somaCapacitacionRepository;

    @Mock
    private SomaEntregaEppRepository somaEntregaEppRepository;

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private SomaHabilitacionServiceImpl servicio;

    private Persona persona;

    @BeforeEach
    void stubActivoYProyecto() {
        persona = personaConRetcc(EstadoDocumentoChecklist.CONFORME);
        lenient().when(proyectoRepository.existsById(10L)).thenReturn(true);
        lenient()
                .when(somaTrabajadorObraRepository.buscarActivos(10L, FECHA, FECHA))
                .thenReturn(List.of(contratoCc(persona)));
        lenient().when(personaRepository.findAllWithChecklistByIdIn(anyCollection())).thenReturn(List.of(persona));
        stubEmo(AptitudEmo.APTO, LocalDate.of(2027, 1, 1), null);
        stubSctrCubierto(true);
        lenient()
                .when(somaCapacitacionRepository.existsByProyectoIdAndTipoAndAsistentesPersonaIdAndAsistentesAprobadoTrue(
                        eq(10L), eq(TipoCapacitacionSoma.INDUCCION_HOMBRE_NUEVO), eq(20L)))
                .thenReturn(true);
        lenient()
                .when(somaEntregaEppRepository.existsByProyectoIdAndPersonaIdAndMotivo(
                        10L, 20L, MotivoEntregaEpp.DOTACION_INICIAL))
                .thenReturn(true);
    }

    @Test
    void sinEmoBloquea() {
        when(somaEmoExamenRepository.findFirstByPersonaIdOrderByFechaExamenDescIdDesc(20L)).thenReturn(Optional.empty());

        SomaHabilitacionFilaDTO fila = evaluar().getFilas().get(0);

        assertEquals(EstadoHabilitacionSoma.BLOQUEADO, fila.getEstado());
        assertTrue(fila.getMotivos().contains("Sin examen médico ocupacional"));
    }

    @Test
    void emoVencidoBloquea() {
        stubEmo(AptitudEmo.APTO, LocalDate.of(2026, 11, 12), null);

        SomaHabilitacionFilaDTO fila = evaluar().getFilas().get(0);

        assertEquals(EstadoHabilitacionSoma.BLOQUEADO, fila.getEstado());
        assertTrue(fila.getMotivos().contains("EMO vencido"));
    }

    @Test
    void emoNoAptoBloquea() {
        stubEmo(AptitudEmo.NO_APTO, LocalDate.of(2027, 1, 1), null);

        SomaHabilitacionFilaDTO fila = evaluar().getFilas().get(0);

        assertEquals(EstadoHabilitacionSoma.BLOQUEADO, fila.getEstado());
        assertTrue(fila.getMotivos().contains("EMO no apto u observado"));
    }

    @Test
    void sinSctrBloquea() {
        stubSctrCubierto(false);

        SomaHabilitacionFilaDTO fila = evaluar().getFilas().get(0);

        assertEquals(EstadoHabilitacionSoma.BLOQUEADO, fila.getEstado());
        assertTrue(fila.getMotivos().contains("Sin cobertura SCTR del mes"));
    }

    @Test
    void sinInduccionBloquea() {
        when(somaCapacitacionRepository.existsByProyectoIdAndTipoAndAsistentesPersonaIdAndAsistentesAprobadoTrue(
                        10L, TipoCapacitacionSoma.INDUCCION_HOMBRE_NUEVO, 20L))
                .thenReturn(false);

        SomaHabilitacionFilaDTO fila = evaluar().getFilas().get(0);

        assertEquals(EstadoHabilitacionSoma.BLOQUEADO, fila.getEstado());
        assertTrue(fila.getMotivos().contains("Sin inducción de hombre nuevo aprobada"));
    }

    @Test
    void sinEppInicialBloquea() {
        when(somaEntregaEppRepository.existsByProyectoIdAndPersonaIdAndMotivo(
                        10L, 20L, MotivoEntregaEpp.DOTACION_INICIAL))
                .thenReturn(false);

        SomaHabilitacionFilaDTO fila = evaluar().getFilas().get(0);

        assertEquals(EstadoHabilitacionSoma.BLOQUEADO, fila.getEstado());
        assertTrue(fila.getMotivos().contains("Sin entrega inicial de EPP"));
    }

    @Test
    void retccNoConformeBloquea() {
        persona = personaConRetcc(EstadoDocumentoChecklist.PENDIENTE);
        when(personaRepository.findAllWithChecklistByIdIn(anyCollection())).thenReturn(List.of(persona));
        when(somaTrabajadorObraRepository.buscarActivos(10L, FECHA, FECHA)).thenReturn(List.of(contratoCc(persona)));

        SomaHabilitacionFilaDTO fila = evaluar().getFilas().get(0);

        assertEquals(EstadoHabilitacionSoma.BLOQUEADO, fila.getEstado());
        assertTrue(fila.getMotivos().contains("Carnet RETCC no conforme"));
    }

    @Test
    void todosOkHabilita() {
        SomaHabilitacionDTO dto = evaluar();
        SomaHabilitacionFilaDTO fila = dto.getFilas().get(0);

        assertEquals(EstadoHabilitacionSoma.HABILITADO, fila.getEstado());
        assertTrue(fila.getMotivos().isEmpty());
        assertEquals(1, dto.getHabilitados());
        assertEquals(0, dto.getBloqueados());
    }

    @Test
    void aptoConRestriccionHabilitaYExponeRestricciones() {
        stubEmo(AptitudEmo.APTO_CON_RESTRICCION, LocalDate.of(2026, 12, 1), "No cargar más de 20 kg");

        SomaHabilitacionDTO dto = evaluar();
        SomaHabilitacionFilaDTO fila = dto.getFilas().get(0);

        assertEquals(EstadoHabilitacionSoma.HABILITADO, fila.getEstado());
        assertEquals("No cargar más de 20 kg", fila.getRestricciones());
        assertEquals(1, dto.getEmosPorVencer30Dias());
    }

    private SomaHabilitacionDTO evaluar() {
        return servicio.evaluarObra(10L, FECHA);
    }

    private void stubEmo(AptitudEmo aptitud, LocalDate vencimiento, String restricciones) {
        SomaEmoExamen examen = new SomaEmoExamen();
        examen.setPersona(persona);
        examen.setAptitud(aptitud);
        examen.setFechaExamen(LocalDate.of(2026, 1, 1));
        examen.setFechaVencimiento(vencimiento);
        examen.setRestricciones(restricciones);
        lenient()
                .when(somaEmoExamenRepository.findFirstByPersonaIdOrderByFechaExamenDescIdDesc(20L))
                .thenReturn(Optional.of(examen));
    }

    private void stubSctrCubierto(boolean cubierto) {
        if (!cubierto) {
            lenient()
                    .when(somaSctrPolizaRepository.findByProyectoIdAndAnioAndMesAndEstado(
                            10L, 2026, 11, EstadoSctrPoliza.VIGENTE))
                    .thenReturn(Optional.empty());
            return;
        }
        SomaSctrPoliza poliza = new SomaSctrPoliza();
        SomaSctrNomina fila = new SomaSctrNomina();
        fila.setPersona(persona);
        fila.setIncluidoSalud(true);
        fila.setIncluidoPension(true);
        poliza.addNomina(fila);
        lenient()
                .when(somaSctrPolizaRepository.findByProyectoIdAndAnioAndMesAndEstado(
                        10L, 2026, 11, EstadoSctrPoliza.VIGENTE))
                .thenReturn(Optional.of(poliza));
    }

    private static Persona personaConRetcc(EstadoDocumentoChecklist estado) {
        Persona persona = new Persona();
        persona.setId(20L);
        persona.setNombres("Pedro");
        persona.setApellidos("Rojas");
        persona.setNumeroDocumento("73567960");
        PersonaDocumentoChecklist fila = new PersonaDocumentoChecklist();
        fila.setTipoDocumento(TipoDocumentoChecklist.CARNET_RETCC);
        fila.setEstado(estado);
        persona.addDocumentoChecklist(fila);
        return persona;
    }

    private static ContratoCc contratoCc(Persona persona) {
        Contrato contrato = new Contrato();
        contrato.setId(1L);
        contrato.setPersona(persona);
        contrato.setActivo(true);
        ContratoCc cc = new ContratoCc();
        cc.setContrato(contrato);
        return cc;
    }
}
