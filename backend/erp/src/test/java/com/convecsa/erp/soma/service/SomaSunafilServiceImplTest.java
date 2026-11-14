package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.UsuarioProyectoRepository;
import com.convecsa.erp.construccion.support.AccesoObraService;
import com.convecsa.erp.nomina.declaracion.dto.ArchivoDescargaDTO;
import com.convecsa.erp.nomina.entity.NominaDocumentoConfiguracion;
import com.convecsa.erp.nomina.repository.NominaDocumentoConfiguracionRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.domain.AptitudEmo;
import com.convecsa.erp.soma.domain.EstadoInvestigacionSst;
import com.convecsa.erp.soma.domain.TipoEmo;
import com.convecsa.erp.soma.domain.TipoEventoSst;
import com.convecsa.erp.soma.dto.SomaSunafilDocumentoDTO;
import com.convecsa.erp.soma.entity.SomaEmoExamen;
import com.convecsa.erp.soma.entity.SomaEventoSst;
import com.convecsa.erp.soma.repository.SomaEmoExamenRepository;
import com.convecsa.erp.soma.repository.SomaEventoSstRepository;
import com.convecsa.erp.soma.service.impl.SomaSunafilServiceImpl;
import com.convecsa.erp.soma.support.SomaAcceso;
import com.convecsa.erp.soma.support.SomaSunafilExcelExporter;
import com.convecsa.erp.soma.support.SomaSunafilPdfService;
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
    void formato3NoImplementado() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> servicio.exportar(3, 10L, null, null, "pdf"));
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

    private static Persona persona() {
        Persona persona = new Persona();
        persona.setId(5L);
        persona.setApellidos("Rojas");
        persona.setNombres("Pedro");
        persona.setNumeroDocumento("73567960");
        return persona;
    }
}
