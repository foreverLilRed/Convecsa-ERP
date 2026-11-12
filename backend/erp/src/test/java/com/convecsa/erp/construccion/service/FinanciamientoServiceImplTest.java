package com.convecsa.erp.construccion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.domain.AccionAuditoriaObra;
import com.convecsa.erp.construccion.domain.EstadoFacturaObra;
import com.convecsa.erp.construccion.domain.EstadoValorizacion;
import com.convecsa.erp.construccion.domain.TipoLiberacionRetencion;
import com.convecsa.erp.construccion.dto.CobranzaObraRequest;
import com.convecsa.erp.construccion.dto.FacturaObraDTO;
import com.convecsa.erp.construccion.dto.FlujoCajaDTO;
import com.convecsa.erp.construccion.dto.LiberacionRetencionRequest;
import com.convecsa.erp.construccion.dto.OperacionFactoringDTO;
import com.convecsa.erp.construccion.dto.OperacionFactoringRequest;
import com.convecsa.erp.construccion.entity.AlmacenObra;
import com.convecsa.erp.construccion.entity.FacturaObra;
import com.convecsa.erp.construccion.entity.OperacionFactoring;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.ValeSalida;
import com.convecsa.erp.construccion.entity.ValeSalidaItem;
import com.convecsa.erp.construccion.entity.Valorizacion;
import com.convecsa.erp.construccion.repository.AlmacenObraRepository;
import com.convecsa.erp.construccion.repository.BoletaCcRepository;
import com.convecsa.erp.construccion.repository.CobranzaObraRepository;
import com.convecsa.erp.construccion.repository.FacturaObraRepository;
import com.convecsa.erp.construccion.repository.LiberacionRetencionRepository;
import com.convecsa.erp.construccion.repository.OperacionFactoringRepository;
import com.convecsa.erp.construccion.repository.PeriodoCcRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ValeSalidaRepository;
import com.convecsa.erp.construccion.repository.ValorizacionRepository;
import com.convecsa.erp.construccion.service.impl.FinanciamientoServiceImpl;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
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

@ExtendWith(MockitoExtension.class)
class FinanciamientoServiceImplTest {

    @Mock
    private ProyectoRepository proyectoRepository;
    @Mock
    private FacturaObraRepository facturaObraRepository;
    @Mock
    private CobranzaObraRepository cobranzaObraRepository;
    @Mock
    private LiberacionRetencionRepository liberacionRetencionRepository;
    @Mock
    private OperacionFactoringRepository operacionFactoringRepository;
    @Mock
    private ValorizacionRepository valorizacionRepository;
    @Mock
    private PeriodoCcRepository periodoCcRepository;
    @Mock
    private BoletaCcRepository boletaCcRepository;
    @Mock
    private AlmacenObraRepository almacenObraRepository;
    @Mock
    private ValeSalidaRepository valeSalidaRepository;
    @Mock
    private AuditoriaObraRegistrar auditoriaObraRegistrar;

    @InjectMocks
    private FinanciamientoServiceImpl servicio;

    private final Map<Long, FacturaObra> facturas = new LinkedHashMap<>();
    private final Map<Long, OperacionFactoring> factoring = new LinkedHashMap<>();
    private final List<com.convecsa.erp.construccion.entity.LiberacionRetencion> liberaciones = new ArrayList<>();
    private Proyecto proyecto;
    private FacturaObra factura;

    @BeforeEach
    void preparar() {
        proyecto = new Proyecto();
        proyecto.setId(7L);
        proyecto.setFechaInicio(LocalDate.of(2026, 11, 1));
        proyecto.setFechaFin(LocalDate.of(2026, 11, 30));

        Valorizacion valorizacion = new Valorizacion();
        valorizacion.setId(3L);
        valorizacion.setProyecto(proyecto);
        valorizacion.setEstado(EstadoValorizacion.APROBADA);
        valorizacion.setMontoRetencion(new BigDecimal("20.00"));

        factura = new FacturaObra();
        factura.setId(1L);
        factura.setProyecto(proyecto);
        factura.setValorizacion(valorizacion);
        factura.setNumero(1);
        factura.setFecha(LocalDate.of(2026, 11, 5));
        factura.setTotal(new BigDecimal("100.00"));
        factura.setSubtotal(new BigDecimal("84.75"));
        factura.setIgv(new BigDecimal("15.25"));
        factura.setEstado(EstadoFacturaObra.EMITIDA);
        facturas.put(1L, factura);

        lenient().when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        lenient().when(facturaObraRepository.findByIdAndProyectoId(any(), eq(7L)))
                .thenAnswer(invocation -> Optional.ofNullable(facturas.get(invocation.getArgument(0))));
        lenient().when(facturaObraRepository.findByProyectoIdOrderByNumeroDesc(7L))
                .thenAnswer(invocation -> List.copyOf(facturas.values()));
        lenient().when(facturaObraRepository.save(any(FacturaObra.class))).thenAnswer(invocation -> {
            FacturaObra item = invocation.getArgument(0);
            if (item.getId() == null) {
                item.setId((long) facturas.size() + 1);
            }
            long n = 1;
            for (var cobro : item.getCobranzas()) {
                if (cobro.getId() == null) {
                    cobro.setId(n);
                }
                cobro.setFactura(item);
                cobro.setProyecto(proyecto);
                n++;
            }
            facturas.put(item.getId(), item);
            return item;
        });
        lenient().when(cobranzaObraRepository.findByFacturaIdOrderByFechaAscIdAsc(any()))
                .thenAnswer(invocation -> {
                    FacturaObra item = facturas.get(invocation.getArgument(0));
                    return item == null ? List.of() : List.copyOf(item.getCobranzas());
                });
        lenient().when(cobranzaObraRepository.findByProyectoIdOrderByFechaDescIdDesc(7L))
                .thenAnswer(invocation -> facturas.values().stream()
                        .flatMap(item -> item.getCobranzas().stream())
                        .toList());
        lenient().when(operacionFactoringRepository.findByFacturaId(any()))
                .thenAnswer(invocation -> factoring.values().stream()
                        .filter(item -> item.getFactura() != null
                                && item.getFactura().getId().equals(invocation.getArgument(0)))
                        .findFirst());
        lenient().when(operacionFactoringRepository.findByIdAndProyectoId(any(), eq(7L)))
                .thenAnswer(invocation -> Optional.ofNullable(factoring.get(invocation.getArgument(0))));
        lenient().when(operacionFactoringRepository.findByProyectoIdOrderByFechaCesionDescIdDesc(7L))
                .thenAnswer(invocation -> List.copyOf(factoring.values()));
        lenient().when(operacionFactoringRepository.save(any(OperacionFactoring.class))).thenAnswer(invocation -> {
            OperacionFactoring item = invocation.getArgument(0);
            if (item.getId() == null) {
                item.setId((long) factoring.size() + 1);
            }
            factoring.put(item.getId(), item);
            return item;
        });
        lenient().when(valorizacionRepository.findByProyectoIdAndEstado(7L, EstadoValorizacion.APROBADA))
                .thenReturn(List.of(valorizacion));
        lenient().when(liberacionRetencionRepository.findByProyectoIdOrderByFechaDescIdDesc(7L))
                .thenAnswer(invocation -> List.copyOf(liberaciones));
        lenient().when(liberacionRetencionRepository.save(any())).thenAnswer(invocation -> {
            var item = invocation.getArgument(0, com.convecsa.erp.construccion.entity.LiberacionRetencion.class);
            item.setId((long) liberaciones.size() + 1);
            item.setProyecto(proyecto);
            liberaciones.add(item);
            return item;
        });
        lenient().when(periodoCcRepository.findByProyectoIdAndEstado(eq(7L), any())).thenReturn(List.of());
        lenient().when(boletaCcRepository.findByPeriodoIdIn(any())).thenReturn(List.of());
        AlmacenObra almacen = new AlmacenObra();
        almacen.setId(9L);
        almacen.setProyecto(proyecto);
        lenient().when(almacenObraRepository.findByProyectoId(7L)).thenReturn(Optional.of(almacen));
        ValeSalida vale = new ValeSalida();
        vale.setFecha(LocalDate.of(2026, 11, 15));
        ValeSalidaItem item = new ValeSalidaItem();
        item.setCostoTotal(new BigDecimal("25.00"));
        vale.getItems().add(item);
        lenient().when(valeSalidaRepository.findByAlmacenIdOrderByNumeroDesc(9L)).thenReturn(List.of(vale));
    }

    @Test
    void cobroParcialYLuegoCobraLaFactura() {
        FacturaObraDTO primero = cobrar("40");
        assertEquals(EstadoFacturaObra.PARCIAL, primero.getEstado());
        assertEquals(new BigDecimal("60.00"), primero.getSaldo());

        FacturaObraDTO segundo = cobrar("60");
        assertEquals(EstadoFacturaObra.COBRADA, segundo.getEstado());
        assertEquals(new BigDecimal("0.00"), segundo.getSaldo());
    }

    @Test
    void rechazaCobroQueExcedeSaldoYCobroSiEstaCedida() {
        IllegalArgumentException exceso = assertThrows(IllegalArgumentException.class, () -> cobrar("140"));
        assertEquals("El cobro excede el saldo de la factura", exceso.getMessage());

        ceder();
        IllegalArgumentException cedida = assertThrows(IllegalArgumentException.class, () -> cobrar("10"));
        assertEquals("No se puede cobrar una factura cedida", cedida.getMessage());
    }

    @Test
    void factoringCalculaNetoConTasaComisionEIgv() {
        OperacionFactoringDTO dto = ceder();
        assertEquals(new BigDecimal("5.00"), dto.getDescuento());
        assertEquals(new BigDecimal("10.00"), dto.getComision());
        assertEquals(new BigDecimal("1.80"), dto.getIgvComision());
        assertEquals(new BigDecimal("83.20"), dto.getImporteNeto());
        assertEquals(EstadoFacturaObra.CEDIDA, facturas.get(1L).getEstado());
        verify(auditoriaObraRegistrar).registrar(
                eq(7L),
                eq(AccionAuditoriaObra.CEDER_FACTURA),
                eq(AuditoriaObraRegistrar.ENTIDAD_FACTORING),
                eq(dto.getId()),
                any());
    }

    @Test
    void rechazaCederSiHayCobroYRevertirSoloSinAdelanto() {
        cobrar("40");
        IllegalArgumentException cobrada = assertThrows(IllegalArgumentException.class, this::ceder);
        assertEquals("Solo se puede ceder una factura emitida sin cobros", cobrada.getMessage());

        factura.getCobranzas().clear();
        factura.setEstado(EstadoFacturaObra.EMITIDA);
        OperacionFactoringDTO cesion = ceder();
        servicio.adelantarFactoring(7L, cesion.getId());
        IllegalArgumentException revertir = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.revertirFactoring(7L, cesion.getId()));
        assertEquals("Solo se puede revertir una cesión sin adelanto", revertir.getMessage());
    }

    @Test
    void liberacionNoSuperaElRetenido() {
        LiberacionRetencionRequest ok = new LiberacionRetencionRequest();
        ok.setTipo(TipoLiberacionRetencion.EFECTIVO);
        ok.setFecha(LocalDate.of(2026, 11, 20));
        ok.setImporte(new BigDecimal("20"));
        assertEquals(new BigDecimal("0.00"), servicio.liberarRetencion(7L, ok).getSaldo());

        LiberacionRetencionRequest deMas = new LiberacionRetencionRequest();
        deMas.setTipo(TipoLiberacionRetencion.EFECTIVO);
        deMas.setFecha(LocalDate.of(2026, 11, 21));
        deMas.setImporte(new BigDecimal("1"));
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.liberarRetencion(7L, deMas));
        assertEquals("La liberación no puede superar el saldo retenido", error.getMessage());
    }

    @Test
    void flujoCajaAgrupaCobranzaYValeEnElMismoMes() {
        cobrar("40");
        FlujoCajaDTO flujo = servicio.flujoCaja(7L, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
        assertEquals(1, flujo.getPuntos().size());
        assertEquals(new BigDecimal("40.00"), flujo.getPuntos().get(0).getIngresos());
        assertEquals(new BigDecimal("25.00"), flujo.getPuntos().get(0).getEgresos());
        assertEquals(new BigDecimal("15.00"), flujo.getPuntos().get(0).getNeto());
    }

    private FacturaObraDTO cobrar(String importe) {
        CobranzaObraRequest request = new CobranzaObraRequest();
        request.setFacturaId(1L);
        request.setFecha(LocalDate.of(2026, 11, 10));
        request.setImporte(new BigDecimal(importe));
        request.setDetraccion(BigDecimal.ZERO);
        return servicio.registrarCobranza(7L, request);
    }

    private OperacionFactoringDTO ceder() {
        OperacionFactoringRequest request = new OperacionFactoringRequest();
        request.setFacturaId(1L);
        request.setEntidadRazon("Factor SAC");
        request.setEntidadRuc("20111111111");
        request.setFechaCesion(LocalDate.of(2026, 11, 12));
        request.setTasaDescuento(new BigDecimal("5"));
        request.setComision(new BigDecimal("10"));
        return servicio.cederFactura(7L, request);
    }
}
