package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.domain.EstadoObra;
import com.convecsa.erp.construccion.domain.SeveridadAlertaObra;
import com.convecsa.erp.construccion.dto.AlertaObraDTO;
import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.domain.EstadoHallazgoSoma;
import com.convecsa.erp.soma.domain.EstadoInvestigacionSst;
import com.convecsa.erp.soma.domain.EstadoSctrPoliza;
import com.convecsa.erp.soma.domain.PotencialPerdidaSoma;
import com.convecsa.erp.soma.domain.TipoRequerimientoComiteSoma;
import com.convecsa.erp.soma.domain.TipoTrabajoPetar;
import com.convecsa.erp.soma.dto.SomaComiteMiembroDTO;
import com.convecsa.erp.soma.dto.SomaComiteRequerimientoDTO;
import com.convecsa.erp.soma.dto.SomaPetarFaltanteTareoDTO;
import com.convecsa.erp.soma.entity.SomaEmoExamen;
import com.convecsa.erp.soma.entity.SomaEventoSst;
import com.convecsa.erp.soma.entity.SomaHallazgo;
import com.convecsa.erp.soma.entity.SomaSctrPoliza;
import com.convecsa.erp.soma.repository.SomaEmoExamenRepository;
import com.convecsa.erp.soma.repository.SomaEventoSstRepository;
import com.convecsa.erp.soma.repository.SomaHallazgoRepository;
import com.convecsa.erp.soma.repository.SomaSctrPolizaRepository;
import com.convecsa.erp.soma.repository.SomaTrabajadorObraRepository;
import com.convecsa.erp.soma.service.impl.SomaAlertaServiceImpl;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SomaAlertaServiceImplTest {

    private static final Long PROYECTO_ID = 10L;
    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    @Mock
    private ProyectoRepository proyectoRepository;
    @Mock
    private SomaTrabajadorObraRepository somaTrabajadorObraRepository;
    @Mock
    private SomaEmoExamenRepository somaEmoExamenRepository;
    @Mock
    private SomaSctrPolizaRepository somaSctrPolizaRepository;
    @Mock
    private SomaHallazgoRepository somaHallazgoRepository;
    @Mock
    private SomaEventoSstRepository somaEventoSstRepository;
    @Mock
    private InterfazSomaComiteService somaComiteService;
    @Mock
    private InterfazSomaPetarService somaPetarService;

    @InjectMocks
    private SomaAlertaServiceImpl servicio;

    private Proyecto proyecto;
    private LocalDate hoy;

    @BeforeEach
    void preparar() {
        hoy = LocalDate.now(LIMA);
        proyecto = new Proyecto();
        proyecto.setId(PROYECTO_ID);
        proyecto.setEstadoObra(EstadoObra.EN_EJECUCION);
        lenient().when(proyectoRepository.findById(PROYECTO_ID)).thenReturn(Optional.of(proyecto));
        lenient()
                .when(somaTrabajadorObraRepository.buscarActivos(
                        eq(PROYECTO_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        lenient().when(somaComiteService.requerimiento(PROYECTO_ID)).thenReturn(supervisor(1));
        lenient().when(somaComiteService.listarMiembros(PROYECTO_ID)).thenReturn(List.of());
        lenient()
                .when(somaHallazgoRepository.findByProyectoIdOrderByFechaDeteccionDescIdDesc(PROYECTO_ID))
                .thenReturn(List.of());
        lenient().when(somaPetarService.faltantesTareo(PROYECTO_ID)).thenReturn(List.of());
        lenient()
                .when(somaEventoSstRepository.findByProyectoIdOrderByFechaHoraDescNumeroDesc(PROYECTO_ID))
                .thenReturn(List.of());
    }

    @Test
    void emoVencidoSinExamen() {
        Persona persona = persona(1L);
        stubActivos(persona);
        List<AlertaObraDTO> alertas = servicio.alertas(PROYECTO_ID);
        AlertaObraDTO alerta = alerta(alertas, "SOMA_EMO_VENCIDO");
        assertEquals(SeveridadAlertaObra.CRITICAL, alerta.getSeveridad());
        assertEquals("1 trabajador con EMO vencido o sin EMO", alerta.getMensaje());
    }

    @Test
    void emoVigenteNoEmiteVencidoNiPorVencer() {
        Persona persona = persona(1L);
        stubActivos(persona);
        stubEmo(persona.getId(), hoy.plusDays(60));
        stubPolizaVigente();
        Set<String> codigos = codigos(servicio.alertas(PROYECTO_ID));
        assertFalse(codigos.contains("SOMA_EMO_VENCIDO"));
        assertFalse(codigos.contains("SOMA_EMO_POR_VENCER"));
    }

    @Test
    void emoPorVencerEnVentanaDe30Dias() {
        Persona persona = persona(2L);
        stubActivos(persona);
        stubEmo(persona.getId(), hoy.plusDays(15));
        stubPolizaVigente();
        AlertaObraDTO alerta = alerta(servicio.alertas(PROYECTO_ID), "SOMA_EMO_POR_VENCER");
        assertEquals(SeveridadAlertaObra.WARNING, alerta.getSeveridad());
        assertEquals("1 trabajador con EMO por vencer", alerta.getMensaje());
        assertFalse(codigos(servicio.alertas(PROYECTO_ID)).contains("SOMA_EMO_VENCIDO"));
    }

    @Test
    void emoVencidoNoCuentaComoPorVencer() {
        Persona persona = persona(3L);
        stubActivos(persona);
        stubEmo(persona.getId(), hoy.minusDays(1));
        stubPolizaVigente();
        Set<String> codigos = codigos(servicio.alertas(PROYECTO_ID));
        assertTrue(codigos.contains("SOMA_EMO_VENCIDO"));
        assertFalse(codigos.contains("SOMA_EMO_POR_VENCER"));
    }

    @Test
    void sctrSinPolizaEnEjecucionConActivos() {
        Persona persona = persona(4L);
        stubActivos(persona);
        stubEmo(persona.getId(), hoy.plusYears(1));
        AlertaObraDTO alerta = alerta(servicio.alertas(PROYECTO_ID), "SOMA_SCTR_SIN_POLIZA");
        assertEquals(SeveridadAlertaObra.CRITICAL, alerta.getSeveridad());
        assertEquals("Obra en ejecución sin póliza SCTR vigente del mes", alerta.getMensaje());
    }

    @Test
    void sctrNoEmiteSinActivosNiConPolizaVigente() {
        assertFalse(codigos(servicio.alertas(PROYECTO_ID)).contains("SOMA_SCTR_SIN_POLIZA"));

        Persona persona = persona(5L);
        stubActivos(persona);
        stubEmo(persona.getId(), hoy.plusYears(1));
        stubPolizaVigente();
        assertFalse(codigos(servicio.alertas(PROYECTO_ID)).contains("SOMA_SCTR_SIN_POLIZA"));

        proyecto.setEstadoObra(EstadoObra.PARALIZADA);
        assertFalse(codigos(servicio.alertas(PROYECTO_ID)).contains("SOMA_SCTR_SIN_POLIZA"));
    }

    @Test
    void comiteRequeridoSupervisorSinActivo() {
        when(somaComiteService.requerimiento(PROYECTO_ID)).thenReturn(supervisor(0));
        AlertaObraDTO alerta = alerta(servicio.alertas(PROYECTO_ID), "SOMA_COMITE_REQUERIDO");
        assertEquals(SeveridadAlertaObra.WARNING, alerta.getSeveridad());
        assertEquals("Falta un supervisor SST activo", alerta.getMensaje());
    }

    @Test
    void comiteRequeridoSubcomiteSinMiembrosActivos() {
        when(somaComiteService.requerimiento(PROYECTO_ID)).thenReturn(subcomite());
        when(somaComiteService.listarMiembros(PROYECTO_ID))
                .thenReturn(List.of(SomaComiteMiembroDTO.builder().activo(false).build()));
        AlertaObraDTO alerta = alerta(servicio.alertas(PROYECTO_ID), "SOMA_COMITE_REQUERIDO");
        assertEquals(SeveridadAlertaObra.WARNING, alerta.getSeveridad());
        assertEquals("Falta constituir el subcomité SST", alerta.getMensaje());
    }

    @Test
    void comiteNoEmiteConSupervisorOSubcomiteActivo() {
        assertFalse(codigos(servicio.alertas(PROYECTO_ID)).contains("SOMA_COMITE_REQUERIDO"));

        when(somaComiteService.requerimiento(PROYECTO_ID)).thenReturn(subcomite());
        when(somaComiteService.listarMiembros(PROYECTO_ID))
                .thenReturn(List.of(SomaComiteMiembroDTO.builder().activo(true).build()));
        assertFalse(codigos(servicio.alertas(PROYECTO_ID)).contains("SOMA_COMITE_REQUERIDO"));
    }

    @Test
    void hallazgoVencidoAltoGraveEsCritico() {
        when(somaHallazgoRepository.findByProyectoIdOrderByFechaDeteccionDescIdDesc(PROYECTO_ID))
                .thenReturn(List.of(hallazgo(EstadoHallazgoSoma.ABIERTO, PotencialPerdidaSoma.ALTO_GRAVE, hoy.minusDays(1))));
        AlertaObraDTO alerta = alerta(servicio.alertas(PROYECTO_ID), "SOMA_HALLAZGO_VENCIDO");
        assertEquals(SeveridadAlertaObra.CRITICAL, alerta.getSeveridad());
        assertEquals("1 hallazgo vencido", alerta.getMensaje());
    }

    @Test
    void hallazgoVencidoMedioEsWarning() {
        when(somaHallazgoRepository.findByProyectoIdOrderByFechaDeteccionDescIdDesc(PROYECTO_ID))
                .thenReturn(List.of(hallazgo(
                        EstadoHallazgoSoma.EN_PROCESO, PotencialPerdidaSoma.MEDIO_MODERADO, hoy.minusDays(2))));
        AlertaObraDTO alerta = alerta(servicio.alertas(PROYECTO_ID), "SOMA_HALLAZGO_VENCIDO");
        assertEquals(SeveridadAlertaObra.WARNING, alerta.getSeveridad());
        assertEquals("1 hallazgo vencido", alerta.getMensaje());
    }

    @Test
    void hallazgoLevantadoOEnPlazoNoEmite() {
        when(somaHallazgoRepository.findByProyectoIdOrderByFechaDeteccionDescIdDesc(PROYECTO_ID))
                .thenReturn(List.of(
                        hallazgo(EstadoHallazgoSoma.LEVANTADO, PotencialPerdidaSoma.ALTO_GRAVE, hoy.minusDays(10)),
                        hallazgo(EstadoHallazgoSoma.ABIERTO, PotencialPerdidaSoma.ALTO_GRAVE, hoy)));
        assertFalse(codigos(servicio.alertas(PROYECTO_ID)).contains("SOMA_HALLAZGO_VENCIDO"));
    }

    @Test
    void petarFaltanteCuentaDiasUnicos() {
        LocalDate dia = hoy.minusDays(1);
        when(somaPetarService.faltantesTareo(PROYECTO_ID)).thenReturn(List.of(
                SomaPetarFaltanteTareoDTO.builder().fecha(dia).tipoTrabajo(TipoTrabajoPetar.ALTURA).build(),
                SomaPetarFaltanteTareoDTO.builder().fecha(dia).tipoTrabajo(TipoTrabajoPetar.IZAJE_CARGAS).build(),
                SomaPetarFaltanteTareoDTO.builder()
                        .fecha(hoy.minusDays(2))
                        .tipoTrabajo(TipoTrabajoPetar.ESPACIO_CONFINADO)
                        .build()));
        AlertaObraDTO alerta = alerta(servicio.alertas(PROYECTO_ID), "SOMA_PETAR_FALTANTE_TAREO");
        assertEquals(SeveridadAlertaObra.WARNING, alerta.getSeveridad());
        assertEquals("2 días de tareo sin PETAR autorizado", alerta.getMensaje());
    }

    @Test
    void petarSinFaltantesNoEmite() {
        assertFalse(codigos(servicio.alertas(PROYECTO_ID)).contains("SOMA_PETAR_FALTANTE_TAREO"));
    }

    @Test
    void eventoAbiertoMasDe30Dias() {
        SomaEventoSst abierto = evento(EstadoInvestigacionSst.EN_INVESTIGACION, hoy.minusDays(31));
        when(somaEventoSstRepository.findByProyectoIdOrderByFechaHoraDescNumeroDesc(PROYECTO_ID))
                .thenReturn(List.of(abierto));
        AlertaObraDTO alerta = alerta(servicio.alertas(PROYECTO_ID), "SOMA_EVENTO_ABIERTO");
        assertEquals(SeveridadAlertaObra.INFO, alerta.getSeveridad());
        assertEquals("1 investigación SST abierta más de 30 días", alerta.getMensaje());
    }

    @Test
    void eventoCerradoORecienteNoEmite() {
        SomaEventoSst cerrado = evento(EstadoInvestigacionSst.CERRADO, hoy.minusDays(40));
        SomaEventoSst reciente = evento(EstadoInvestigacionSst.EN_INVESTIGACION, hoy.minusDays(30));
        when(somaEventoSstRepository.findByProyectoIdOrderByFechaHoraDescNumeroDesc(PROYECTO_ID))
                .thenReturn(List.of(cerrado, reciente));
        assertFalse(codigos(servicio.alertas(PROYECTO_ID)).contains("SOMA_EVENTO_ABIERTO"));
    }

    private void stubActivos(Persona... personas) {
        when(somaTrabajadorObraRepository.buscarActivos(eq(PROYECTO_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(java.util.Arrays.stream(personas).map(SomaAlertaServiceImplTest::contratoCc).toList());
    }

    private void stubEmo(Long personaId, LocalDate vencimiento) {
        SomaEmoExamen emo = new SomaEmoExamen();
        emo.setFechaVencimiento(vencimiento);
        emo.setFechaExamen(vencimiento.minusMonths(12));
        when(somaEmoExamenRepository.findFirstByPersonaIdOrderByFechaExamenDescIdDesc(personaId))
                .thenReturn(Optional.of(emo));
    }

    private void stubPolizaVigente() {
        when(somaSctrPolizaRepository.findByProyectoIdAndAnioAndMesAndEstado(
                        eq(PROYECTO_ID), anyInt(), anyInt(), eq(EstadoSctrPoliza.VIGENTE)))
                .thenReturn(Optional.of(new SomaSctrPoliza()));
    }

    private static SomaComiteRequerimientoDTO supervisor(int activos) {
        return SomaComiteRequerimientoDTO.builder()
                .tipoRequerido(TipoRequerimientoComiteSoma.SUPERVISOR_SST)
                .supervisoresActivos(activos)
                .build();
    }

    private static SomaComiteRequerimientoDTO subcomite() {
        return SomaComiteRequerimientoDTO.builder()
                .tipoRequerido(TipoRequerimientoComiteSoma.SUBCOMITE)
                .supervisoresActivos(0)
                .build();
    }

    private static SomaHallazgo hallazgo(
            EstadoHallazgoSoma estado, PotencialPerdidaSoma potencial, LocalDate fechaLimite) {
        SomaHallazgo hallazgo = new SomaHallazgo();
        hallazgo.setEstado(estado);
        hallazgo.setPotencialPerdida(potencial);
        hallazgo.setFechaLimite(fechaLimite);
        return hallazgo;
    }

    private SomaEventoSst evento(EstadoInvestigacionSst estado, LocalDate fechaLima) {
        SomaEventoSst evento = new SomaEventoSst();
        evento.setEstadoInvestigacion(estado);
        evento.setFechaHora(fechaLima.atStartOfDay(LIMA).toInstant());
        return evento;
    }

    private static Persona persona(long id) {
        Persona persona = new Persona();
        persona.setId(id);
        persona.setNombres("Nombre" + id);
        persona.setApellidos("Apellido" + id);
        return persona;
    }

    private static ContratoCc contratoCc(Persona persona) {
        Contrato contrato = new Contrato();
        contrato.setId(persona.getId());
        contrato.setPersona(persona);
        contrato.setActivo(true);
        ContratoCc cc = new ContratoCc();
        cc.setContrato(contrato);
        return cc;
    }

    private static AlertaObraDTO alerta(List<AlertaObraDTO> alertas, String codigo) {
        return alertas.stream()
                .filter(item -> codigo.equals(item.getCodigo()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se emitió " + codigo + ": " + codigos(alertas)));
    }

    private static Set<String> codigos(List<AlertaObraDTO> alertas) {
        return alertas.stream().map(AlertaObraDTO::getCodigo).collect(Collectors.toSet());
    }
}
