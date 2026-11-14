package com.convecsa.erp.construccion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.common.exception.BitacoraObraNoEncontradaException;
import com.convecsa.erp.common.exception.DocumentoObraNoEncontradoException;
import com.convecsa.erp.construccion.domain.EstadoFacturaObra;
import com.convecsa.erp.construccion.domain.EstadoFactoring;
import com.convecsa.erp.construccion.domain.EstadoVersionPresupuesto;
import com.convecsa.erp.construccion.domain.SeveridadAlertaObra;
import com.convecsa.erp.construccion.domain.TipoDocumentoObra;
import com.convecsa.erp.construccion.dto.AlertaObraDTO;
import com.convecsa.erp.construccion.dto.BitacoraObraDTO;
import com.convecsa.erp.construccion.dto.BitacoraObraRequest;
import com.convecsa.erp.construccion.dto.ComparativoPresupuestoDTO;
import com.convecsa.erp.construccion.dto.DocumentoObraDTO;
import com.convecsa.erp.construccion.entity.BitacoraObra;
import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.FacturaObra;
import com.convecsa.erp.construccion.entity.OperacionFactoring;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.AlmacenObraRepository;
import com.convecsa.erp.construccion.repository.AuditoriaObraRepository;
import com.convecsa.erp.construccion.repository.BitacoraObraRepository;
import com.convecsa.erp.construccion.repository.CcParametroAnualRepository;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.FacturaObraRepository;
import com.convecsa.erp.construccion.repository.OperacionFactoringRepository;
import com.convecsa.erp.construccion.repository.PresupuestoVersionRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.service.impl.TransversalObraServiceImpl;
import com.convecsa.erp.construccion.support.DocumentoObraStorage;
import com.convecsa.erp.soma.service.InterfazSomaAlertaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class TransversalObraServiceImplTest {

    @Mock
    private ProyectoRepository proyectoRepository;
    @Mock
    private DocumentoObraRepository documentoObraRepository;
    @Mock
    private DocumentoObraStorage documentoObraStorage;
    @Mock
    private CcParametroAnualRepository parametroRepository;
    @Mock
    private AlmacenObraRepository almacenObraRepository;
    @Mock
    private InterfazAlmacenService almacenService;
    @Mock
    private FacturaObraRepository facturaObraRepository;
    @Mock
    private OperacionFactoringRepository operacionFactoringRepository;
    @Mock
    private PresupuestoVersionRepository presupuestoVersionRepository;
    @Mock
    private InterfazConstruccionService construccionService;
    @Mock
    private BitacoraObraRepository bitacoraObraRepository;
    @Mock
    private AuditoriaObraRepository auditoriaObraRepository;

    @Mock
    private InterfazSomaAlertaService somaAlertaService;

    @InjectMocks
    private TransversalObraServiceImpl servicio;

    private final Map<Long, DocumentoObra> documentos = new LinkedHashMap<>();
    private final List<FacturaObra> facturas = new ArrayList<>();
    private final List<OperacionFactoring> factoring = new ArrayList<>();
    private Proyecto proyecto;

    @BeforeEach
    void preparar() {
        proyecto = new Proyecto();
        proyecto.setId(7L);
        proyecto.setCodigo("OB001");
        proyecto.setValorObra(new BigDecimal("100000"));

        lenient().when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        lenient().when(documentoObraRepository.findByProyectoIdOrderByCreatedAtDescIdDesc(7L))
                .thenAnswer(invocation -> List.copyOf(documentos.values()));
        lenient().when(documentoObraRepository.findByIdAndProyectoId(any(), eq(7L)))
                .thenAnswer(invocation -> Optional.ofNullable(documentos.get(invocation.getArgument(0))));
        lenient().when(documentoObraRepository.save(any(DocumentoObra.class))).thenAnswer(invocation -> {
            DocumentoObra item = invocation.getArgument(0);
            if (item.getId() == null) {
                item.setId((long) documentos.size() + 1);
            }
            documentos.put(item.getId(), item);
            return item;
        });
        lenient().when(documentoObraStorage.guardar(eq(7L), any(), any()))
                .thenReturn("7/uuid_contrato.pdf");
        lenient().when(parametroRepository.findByAnio(anyInt())).thenReturn(Optional.empty());
        lenient().when(parametroRepository.findFirstByOrderByAnioDesc()).thenReturn(Optional.empty());
        lenient().when(almacenObraRepository.findByProyectoId(7L)).thenReturn(Optional.empty());
        lenient().when(facturaObraRepository.findByProyectoIdOrderByNumeroDesc(7L))
                .thenAnswer(invocation -> List.copyOf(facturas));
        lenient().when(operacionFactoringRepository.findByProyectoIdOrderByFechaCesionDescIdDesc(7L))
                .thenAnswer(invocation -> List.copyOf(factoring));
        lenient().when(presupuestoVersionRepository.existsByProyectoIdAndEstado(
                7L, EstadoVersionPresupuesto.VIGENTE)).thenReturn(false);
        lenient().when(somaAlertaService.alertas(7L)).thenReturn(List.of());
    }

    @Test
    void subirPdfContratoYRechazarMimePlano() {
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "contrato.pdf", "application/pdf", new byte[]{1, 2, 3});
        DocumentoObraDTO dto = servicio.subirDocumento(7L, TipoDocumentoObra.CONTRATO, pdf, "Contrato firmado");
        assertEquals(TipoDocumentoObra.CONTRATO, dto.getTipo());
        assertEquals("contrato.pdf", dto.getNombreOriginal());
        assertEquals(1, servicio.listarDocumentos(7L).size());

        MockMultipartFile texto = new MockMultipartFile(
                "file", "notas.txt", "text/plain", "hola".getBytes());
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.subirDocumento(7L, TipoDocumentoObra.CONTRATO, texto, null));
        assertEquals("Solo se aceptan archivos PDF, JPEG o PNG", error.getMessage());
    }

    @Test
    void eliminarDocumentoDeOtraObraDa404() {
        DocumentoObraNoEncontradoException error = assertThrows(
                DocumentoObraNoEncontradoException.class,
                () -> servicio.eliminarDocumento(7L, 99L));
        assertEquals("No se encontró el documento", error.getMessage());
        verify(documentoObraStorage, never()).borrar(any());
    }

    @Test
    void alertaFacturaVencidaYFactoringImpaga() {
        FacturaObra factura = new FacturaObra();
        factura.setNumero(3);
        factura.setEstado(EstadoFacturaObra.EMITIDA);
        factura.setFechaVencimiento(LocalDate.now().minusDays(1));
        facturas.add(factura);

        OperacionFactoring operacion = new OperacionFactoring();
        operacion.setEstado(EstadoFactoring.IMPAGA);
        operacion.setFactura(factura);
        factoring.add(operacion);

        List<AlertaObraDTO> alertas = servicio.listarAlertas(7L);
        assertTrue(alertas.stream().anyMatch(item ->
                "FACTURA_VENCIDA".equals(item.getCodigo()) && item.getSeveridad() == SeveridadAlertaObra.CRITICAL));
        assertTrue(alertas.stream().anyMatch(item ->
                "FACTORING_IMPAGA".equals(item.getCodigo()) && item.getSeveridad() == SeveridadAlertaObra.CRITICAL));
    }

    @Test
    void alertaDesvioMoSoloSiSuperaDiezPorCiento() {
        when(presupuestoVersionRepository.existsByProyectoIdAndEstado(7L, EstadoVersionPresupuesto.VIGENTE))
                .thenReturn(true);
        when(construccionService.comparativoPresupuesto(7L)).thenReturn(comparativo("15.00", "5.00"));
        List<AlertaObraDTO> conDesvio = servicio.listarAlertas(7L);
        assertTrue(conDesvio.stream().anyMatch(item -> "DESVIO_MO".equals(item.getCodigo())));
        assertTrue(conDesvio.stream().noneMatch(item -> "DESVIO_MATERIAL".equals(item.getCodigo())));

        when(construccionService.comparativoPresupuesto(7L)).thenReturn(comparativo("5.00", "5.00"));
        List<AlertaObraDTO> sinDesvio = servicio.listarAlertas(7L);
        assertTrue(sinDesvio.stream().noneMatch(item -> item.getCodigo().startsWith("DESVIO")));
    }

    @Test
    void crearNotaBitacoraGuardaTextoYUsuarioSistema() {
        lenient().when(bitacoraObraRepository.save(any(BitacoraObra.class))).thenAnswer(invocation -> {
            BitacoraObra nota = invocation.getArgument(0);
            nota.setId(1L);
            return nota;
        });
        BitacoraObraRequest request = new BitacoraObraRequest();
        request.setFecha(LocalDate.of(2026, 11, 11));
        request.setTexto("Hormigonado de zapata Z-1");

        BitacoraObraDTO dto = servicio.crearNotaBitacora(7L, request);

        assertEquals("Hormigonado de zapata Z-1", dto.getTexto());
        assertEquals(LocalDate.of(2026, 11, 11), dto.getFecha());
        assertEquals("sistema", dto.getUsuario());
    }

    @Test
    void crearNotaBitacoraRechazaTextoVacio() {
        BitacoraObraRequest request = new BitacoraObraRequest();
        request.setFecha(LocalDate.of(2026, 11, 11));
        request.setTexto("   ");
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.crearNotaBitacora(7L, request));
        assertEquals("El texto es obligatorio", error.getMessage());
        verify(bitacoraObraRepository, never()).save(any());
    }

    @Test
    void eliminarNotaBitacoraDeOtraObraDa404() {
        when(bitacoraObraRepository.findByIdAndProyectoId(99L, 7L)).thenReturn(Optional.empty());
        BitacoraObraNoEncontradaException error = assertThrows(
                BitacoraObraNoEncontradaException.class,
                () -> servicio.eliminarNotaBitacora(7L, 99L));
        assertEquals("No se encontró la nota de bitácora", error.getMessage());
        verify(bitacoraObraRepository, never()).delete(any());
    }

    private static ComparativoPresupuestoDTO comparativo(String desvioMo, String desvioMat) {
        return ComparativoPresupuestoDTO.builder()
                .metaMo(new BigDecimal("100.00"))
                .realMo(new BigDecimal("100.00"))
                .desvio(BigDecimal.ZERO)
                .desvioPct(new BigDecimal(desvioMo))
                .metaMaterial(new BigDecimal("100.00"))
                .realMaterial(new BigDecimal("100.00"))
                .desvioMaterial(BigDecimal.ZERO)
                .desvioPctMaterial(new BigDecimal(desvioMat))
                .centros(List.of())
                .build();
    }
}
