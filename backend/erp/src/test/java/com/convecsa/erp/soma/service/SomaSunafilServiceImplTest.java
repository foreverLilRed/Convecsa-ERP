package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.entity.Material;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.UsuarioProyectoRepository;
import com.convecsa.erp.construccion.support.AccesoObraService;
import com.convecsa.erp.nomina.declaracion.dto.ArchivoDescargaDTO;
import com.convecsa.erp.nomina.entity.NominaDocumentoConfiguracion;
import com.convecsa.erp.nomina.repository.NominaDocumentoConfiguracionRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.domain.AptitudEmo;
import com.convecsa.erp.soma.domain.EstadoItemEpp;
import com.convecsa.erp.soma.domain.MotivoEntregaEpp;
import com.convecsa.erp.soma.domain.ClasificacionHallazgoSoma;
import com.convecsa.erp.soma.domain.EstadoHallazgoSoma;
import com.convecsa.erp.soma.domain.EstadoInvestigacionSst;
import com.convecsa.erp.soma.domain.PotencialPerdidaSoma;
import com.convecsa.erp.soma.domain.TipoAgenteMonitoreoSoma;
import com.convecsa.erp.soma.domain.TipoEmo;
import com.convecsa.erp.soma.domain.TipoEventoSst;
import com.convecsa.erp.soma.domain.TipoInspeccionSoma;
import com.convecsa.erp.soma.dto.SomaEstadisticaAnioDTO;
import com.convecsa.erp.soma.dto.SomaEstadisticaMesDTO;
import com.convecsa.erp.soma.dto.SomaSunafilDocumentoDTO;
import com.convecsa.erp.soma.entity.SomaEmoExamen;
import com.convecsa.erp.soma.entity.SomaEntregaEpp;
import com.convecsa.erp.soma.entity.SomaEntregaEppItem;
import com.convecsa.erp.soma.entity.SomaEventoSst;
import com.convecsa.erp.soma.entity.SomaHallazgo;
import com.convecsa.erp.soma.entity.SomaInspeccion;
import com.convecsa.erp.soma.entity.SomaMonitoreo;
import com.convecsa.erp.soma.repository.SomaEmoExamenRepository;
import com.convecsa.erp.soma.repository.SomaEntregaEppRepository;
import com.convecsa.erp.soma.repository.SomaEventoSstRepository;
import com.convecsa.erp.soma.repository.SomaHallazgoRepository;
import com.convecsa.erp.soma.repository.SomaInspeccionRepository;
import com.convecsa.erp.soma.repository.SomaMonitoreoRepository;
import com.convecsa.erp.soma.service.impl.SomaSunafilServiceImpl;
import com.convecsa.erp.soma.support.SomaAcceso;
import com.convecsa.erp.soma.support.SomaSunafilExcelExporter;
import com.convecsa.erp.soma.support.SomaSunafilPdfService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
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
class SomaSunafilServiceImplTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    @Mock
    private AccesoObraService accesoObraService;
    @Mock
    private SomaAcceso somaAcceso;
    @Mock
    private ProyectoRepository proyectoRepository;
    @Mock
    private UsuarioProyectoRepository usuarioProyectoRepository;
    @Mock
    private NominaDocumentoConfiguracionRepository configuracionRepository;
    @Mock
    private SomaEventoSstRepository somaEventoSstRepository;
    @Mock
    private SomaEmoExamenRepository somaEmoExamenRepository;
    @Mock
    private SomaMonitoreoRepository somaMonitoreoRepository;
    @Mock
    private SomaInspeccionRepository somaInspeccionRepository;
    @Mock
    private SomaHallazgoRepository somaHallazgoRepository;
    @Mock
    private InterfazSomaEstadisticaService somaEstadisticaService;
    @Mock
    private SomaEntregaEppRepository somaEntregaEppRepository;
    @Mock
    private SomaSunafilPdfService somaSunafilPdfService;
    @Mock
    private SomaSunafilExcelExporter somaSunafilExcelExporter;

    @InjectMocks
    private SomaSunafilServiceImpl servicio;

    private Proyecto proyecto;

    @BeforeEach
    void preparar() {
        proyecto = new Proyecto();
        proyecto.setId(10L);
        proyecto.setCodigo("0000");
        proyecto.setNombre("Obra piloto");
        lenient().when(proyectoRepository.findById(10L)).thenReturn(Optional.of(proyecto));
        NominaDocumentoConfiguracion cfg = new NominaDocumentoConfiguracion();
        cfg.setRazonSocial("Convecsa SAC");
        cfg.setRuc("20100000000");
        cfg.setDomicilio("Lima");
        lenient().when(configuracionRepository.findById(NominaDocumentoConfiguracion.ID_UNICO))
                .thenReturn(Optional.of(cfg));
        lenient().when(somaSunafilPdfService.generar(any())).thenReturn("%PDF-1.4".getBytes());
        lenient().when(somaSunafilExcelExporter.exportar(any())).thenReturn(new byte[] {0x50, 0x4B, 0x03, 0x04});
    }

    @Test
    void formato7NoImplementado() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> servicio.exportar(7, 10L, null, null, "pdf"));
        assertTrue(ex.getMessage().contains("aún no implementado"));
    }

    @Test
    void tipoInvalido() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> servicio.exportar(1, 10L, null, null, "doc"));
        assertEquals("El tipo debe ser pdf o xlsx", ex.getMessage());
    }

    @Test
    void mesSinAnio() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> servicio.exportar(1, 10L, null, 11, "pdf"));
        assertEquals("El mes exige el año", ex.getMessage());
    }

    @Test
    void filtraEventosPorMes() {
        SomaEventoSst noviembre = evento(1, LocalDate.of(2026, 11, 5), proyecto);
        SomaEventoSst octubre = evento(2, LocalDate.of(2026, 10, 20), proyecto);
        when(somaEventoSstRepository.findByProyectoIdOrderByFechaHoraDescNumeroDesc(10L))
                .thenReturn(List.of(noviembre, octubre));

        ArchivoDescargaDTO archivo = servicio.exportar(1, 10L, 2026, 11, "pdf");
        assertEquals("sunafil-formato-1-0000-2026-11.pdf", archivo.getFilename());
        assertEquals("application/pdf", archivo.getContentType());

        ArgumentCaptor<SomaSunafilDocumentoDTO> captor = ArgumentCaptor.forClass(SomaSunafilDocumentoDTO.class);
        verify(somaSunafilPdfService).generar(captor.capture());
        SomaSunafilDocumentoDTO doc = captor.getValue();
        assertEquals(1, doc.getFilas().size());
        assertEquals("1", doc.getFilas().get(0).get(0));
        assertEquals("2026-11", doc.getPeriodo());
        verify(somaEventoSstRepository, never()).findAllByOrderByFechaHoraDescNumeroDesc();
    }

    @Test
    void conProyectoIdNoMezclaOtrasObras() {
        SomaEventoSst propio = evento(3, LocalDate.of(2026, 1, 2), proyecto);
        when(somaEventoSstRepository.findByProyectoIdOrderByFechaHoraDescNumeroDesc(10L))
                .thenReturn(List.of(propio));

        servicio.exportar(1, 10L, null, null, "xlsx");

        verify(somaEventoSstRepository).findByProyectoIdOrderByFechaHoraDescNumeroDesc(10L);
        verify(somaEventoSstRepository, never()).findAllByOrderByFechaHoraDescNumeroDesc();
        verify(somaEventoSstRepository, never()).findByProyectoIdInOrderByFechaHoraDescNumeroDesc(any());
        ArgumentCaptor<SomaSunafilDocumentoDTO> captor = ArgumentCaptor.forClass(SomaSunafilDocumentoDTO.class);
        verify(somaSunafilExcelExporter).exportar(captor.capture());
        assertEquals(1, captor.getValue().getFilas().size());
        assertEquals("0000", captor.getValue().getFilas().get(0).get(11));
        assertEquals("sunafil-formato-1-0000.xlsx", servicio.exportar(1, 10L, null, null, "xlsx").getFilename());
    }

    @Test
    void formato2FiltraPorObraYFechaExamen() {
        SomaEmoExamen enObra = emo(LocalDate.of(2026, 3, 10), proyecto);
        SomaEmoExamen otroMes = emo(LocalDate.of(2026, 1, 1), proyecto);
        when(somaEmoExamenRepository.findByProyectoIdOrderByFechaExamenDescIdDesc(10L))
                .thenReturn(List.of(enObra, otroMes));

        servicio.exportar(2, 10L, 2026, 3, "pdf");

        ArgumentCaptor<SomaSunafilDocumentoDTO> captor = ArgumentCaptor.forClass(SomaSunafilDocumentoDTO.class);
        verify(somaSunafilPdfService).generar(captor.capture());
        assertEquals(2, captor.getValue().getFormato());
        assertEquals(1, captor.getValue().getFilas().size());
        assertEquals("PERIODICO", captor.getValue().getFilas().get(0).get(2));
        verify(somaEmoExamenRepository, never()).buscarCorporativo(any(), any());
    }

    @Test
    void formato3FiltraMonitoreoPorMes() {
        SomaMonitoreo noviembre = monitoreo(LocalDate.of(2026, 11, 4), proyecto);
        SomaMonitoreo octubre = monitoreo(LocalDate.of(2026, 10, 1), proyecto);
        when(somaMonitoreoRepository.findByProyectoIdOrderByFechaDescIdDesc(10L))
                .thenReturn(List.of(noviembre, octubre));

        servicio.exportar(3, 10L, 2026, 11, "pdf");

        ArgumentCaptor<SomaSunafilDocumentoDTO> captor = ArgumentCaptor.forClass(SomaSunafilDocumentoDTO.class);
        verify(somaSunafilPdfService).generar(captor.capture());
        assertEquals(3, captor.getValue().getFormato());
        assertEquals(1, captor.getValue().getFilas().size());
        assertEquals("FISICO_RUIDO", captor.getValue().getFilas().get(0).get(1));
        assertEquals("Sí", captor.getValue().getFilas().get(0).get(7));
        verify(somaMonitoreoRepository, never()).findAllByOrderByFechaDescIdDesc();
    }

    @Test
    void formato4IncluyeInspeccionYHallazgoHuerfano() {
        SomaInspeccion inspeccion = inspeccion(LocalDate.of(2026, 11, 2), proyecto);
        SomaHallazgo deInspeccion = hallazgo(LocalDate.of(2026, 11, 3), inspeccion, proyecto);
        SomaHallazgo huerfano = hallazgo(LocalDate.of(2026, 11, 10), null, proyecto);
        SomaHallazgo otroMes = hallazgo(LocalDate.of(2026, 1, 1), null, proyecto);
        when(somaInspeccionRepository.findByProyectoIdOrderByFechaDescIdDesc(10L)).thenReturn(List.of(inspeccion));
        when(somaHallazgoRepository.findByProyectoIdOrderByFechaDeteccionDescIdDesc(10L))
                .thenReturn(List.of(deInspeccion, huerfano, otroMes));

        servicio.exportar(4, 10L, 2026, 11, "xlsx");

        ArgumentCaptor<SomaSunafilDocumentoDTO> captor = ArgumentCaptor.forClass(SomaSunafilDocumentoDTO.class);
        verify(somaSunafilExcelExporter).exportar(captor.capture());
        assertEquals(4, captor.getValue().getFormato());
        assertEquals(2, captor.getValue().getFilas().size());
        assertEquals("PLANEADA_MENSUAL", captor.getValue().getFilas().get(0).get(1));
        assertEquals("CONDICION_SUBESTANDAR", captor.getValue().getFilas().get(0).get(6));
        assertEquals("", captor.getValue().getFilas().get(1).get(1));
        assertEquals("Cable suelto", captor.getValue().getFilas().get(1).get(9));
        verify(somaInspeccionRepository, never()).findAllByOrderByFechaDescIdDesc();
        verify(somaHallazgoRepository, never()).findAllByOrderByFechaDeteccionDescIdDesc();
    }

    @Test
    void formato5FiltraMesYAgregaAcumulado() {
        when(somaEstadisticaService.porObra(10L, 2026)).thenReturn(estadisticaAnio());

        servicio.exportar(5, 10L, 2026, 11, "pdf");

        ArgumentCaptor<SomaSunafilDocumentoDTO> captor = ArgumentCaptor.forClass(SomaSunafilDocumentoDTO.class);
        verify(somaSunafilPdfService).generar(captor.capture());
        SomaSunafilDocumentoDTO doc = captor.getValue();
        assertEquals(5, doc.getFormato());
        assertEquals(2, doc.getFilas().size());
        assertEquals("11", doc.getFilas().get(0).get(0));
        assertEquals("Acumulado", doc.getFilas().get(1).get(0));
        assertEquals("45", doc.getFilas().get(1).get(7));
        verify(somaEstadisticaService, never()).corporativo(any());
    }

    @Test
    void formato6FiltraItemsPorMes() {
        SomaEntregaEpp noviembre = entrega(LocalDate.of(2026, 11, 8), "CASCO", proyecto);
        SomaEntregaEpp octubre = entrega(LocalDate.of(2026, 10, 1), "GUANTE", proyecto);
        when(somaEntregaEppRepository.findByProyectoIdOrderByFechaEntregaDescIdDesc(10L))
                .thenReturn(List.of(noviembre, octubre));

        servicio.exportar(6, 10L, 2026, 11, "xlsx");

        ArgumentCaptor<SomaSunafilDocumentoDTO> captor = ArgumentCaptor.forClass(SomaSunafilDocumentoDTO.class);
        verify(somaSunafilExcelExporter).exportar(captor.capture());
        assertEquals(6, captor.getValue().getFormato());
        assertEquals(1, captor.getValue().getFilas().size());
        assertTrue(captor.getValue().getFilas().get(0).get(4).contains("CASCO"));
        verify(somaEntregaEppRepository, never()).findAllByOrderByFechaEntregaDescIdDesc();
    }

    private static SomaEventoSst evento(int numero, LocalDate fecha, Proyecto proyecto) {
        SomaEventoSst evento = new SomaEventoSst();
        evento.setNumero(numero);
        evento.setTipoEvento(TipoEventoSst.INCIDENTE);
        evento.setEstadoInvestigacion(EstadoInvestigacionSst.EN_INVESTIGACION);
        evento.setFechaHora(fecha.atTime(8, 0).atZone(LIMA).toInstant());
        evento.setLugarExacto("Frente A");
        evento.setDescripcionEvento("Caída de herramienta");
        evento.setProyecto(proyecto);
        evento.setPersonaAfectada(persona());
        return evento;
    }

    private static SomaEmoExamen emo(LocalDate fechaExamen, Proyecto proyecto) {
        SomaEmoExamen emo = new SomaEmoExamen();
        emo.setTipoEmo(TipoEmo.PERIODICO);
        emo.setFechaExamen(fechaExamen);
        emo.setFechaVencimiento(fechaExamen.plusYears(1));
        emo.setAptitud(AptitudEmo.APTO);
        emo.setClinicaNombre("Clinica Uno");
        emo.setProyecto(proyecto);
        emo.setPersona(persona());
        return emo;
    }

    private static SomaMonitoreo monitoreo(LocalDate fecha, Proyecto proyecto) {
        SomaMonitoreo item = new SomaMonitoreo();
        item.setFecha(fecha);
        item.setTipoAgente(TipoAgenteMonitoreoSoma.FISICO_RUIDO);
        item.setParametroMedido("Ruido");
        item.setPuntoMedicion("Taller");
        item.setValorMedido(new BigDecimal("80"));
        item.setLimitePermisible(new BigDecimal("85"));
        item.setUnidad("dBA");
        item.setCumpleLmp(true);
        item.setLaboratorioNombre("Lab");
        item.setProyecto(proyecto);
        return item;
    }

    private static SomaInspeccion inspeccion(LocalDate fecha, Proyecto proyecto) {
        SomaInspeccion item = new SomaInspeccion();
        item.setId(21L);
        item.setFecha(fecha);
        item.setTipo(TipoInspeccionSoma.PLANEADA_MENSUAL);
        item.setAreaFrente("Frente A");
        item.setInspector(persona());
        item.setPuntajeCumplimiento(new BigDecimal("90"));
        item.setProyecto(proyecto);
        return item;
    }

    private static SomaHallazgo hallazgo(LocalDate fechaDeteccion, SomaInspeccion inspeccion, Proyecto proyecto) {
        SomaHallazgo item = new SomaHallazgo();
        item.setFechaDeteccion(fechaDeteccion);
        item.setFechaLimite(fechaDeteccion.plusDays(7));
        item.setClasificacion(ClasificacionHallazgoSoma.CONDICION_SUBESTANDAR);
        item.setPotencialPerdida(PotencialPerdidaSoma.MEDIO_MODERADO);
        item.setEstado(EstadoHallazgoSoma.ABIERTO);
        item.setDescripcion("Cable suelto");
        item.setInspeccion(inspeccion);
        item.setProyecto(proyecto);
        item.setResponsable(persona());
        return item;
    }

    private static SomaEstadisticaAnioDTO estadisticaAnio() {
        List<SomaEstadisticaMesDTO> meses = new java.util.ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            meses.add(SomaEstadisticaMesDTO.builder()
                    .mes(mes)
                    .hht(BigDecimal.TEN)
                    .accidentes(mes == 11 ? 1 : 0)
                    .diasPerdidos(mes == 11 ? 2 : 0)
                    .indiceFrecuencia(BigDecimal.ZERO)
                    .indiceSeveridad(BigDecimal.ZERO)
                    .indiceAccidentabilidad(BigDecimal.ZERO)
                    .build());
        }
        return SomaEstadisticaAnioDTO.builder()
                .anio(2026)
                .meses(meses)
                .acumulado(SomaEstadisticaMesDTO.builder()
                        .mes(null)
                        .hht(new BigDecimal("120"))
                        .accidentes(1)
                        .diasPerdidos(2)
                        .indiceFrecuencia(BigDecimal.ONE)
                        .indiceSeveridad(BigDecimal.ONE)
                        .indiceAccidentabilidad(BigDecimal.ONE)
                        .build())
                .diasSinAccidente(45)
                .build();
    }

    private static SomaEntregaEpp entrega(LocalDate fecha, String codigoMaterial, Proyecto proyecto) {
        SomaEntregaEpp entrega = new SomaEntregaEpp();
        entrega.setFechaEntrega(fecha);
        entrega.setMotivo(MotivoEntregaEpp.DOTACION_INICIAL);
        entrega.setFirmadoTrabajador(true);
        entrega.setEntregadoPor("Residente");
        entrega.setPersona(persona());
        entrega.setProyecto(proyecto);
        Material material = new Material();
        material.setCodigo(codigoMaterial);
        material.setDescripcion(codigoMaterial);
        SomaEntregaEppItem item = new SomaEntregaEppItem();
        item.setMaterial(material);
        item.setCantidad(BigDecimal.ONE);
        item.setTalla("M");
        item.setEstadoItem(EstadoItemEpp.VIGENTE);
        entrega.addItem(item);
        return entrega;
    }

    private static Persona persona() {
        Persona persona = new Persona();
        persona.setId(5L);
        persona.setApellidos("Rojas");
        persona.setNombres("Pedro");
        persona.setNumeroDocumento("73567960");
        return persona;
    }
}
