package com.convecsa.erp.construccion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.convecsa.erp.construccion.domain.AccionAuditoriaObra;
import com.convecsa.erp.construccion.domain.EstadoAdicionalObra;
import com.convecsa.erp.construccion.domain.EstadoValorizacion;
import com.convecsa.erp.construccion.domain.EstadoVersionPresupuesto;
import com.convecsa.erp.construccion.domain.TipoAdicionalObra;
import com.convecsa.erp.construccion.domain.TipoValorizacion;
import com.convecsa.erp.construccion.domain.TipoVersionPresupuesto;
import com.convecsa.erp.construccion.dto.AdicionalObraRequest;
import com.convecsa.erp.construccion.dto.AvanceGuardarRequest;
import com.convecsa.erp.construccion.dto.AvanceObraDTO;
import com.convecsa.erp.construccion.dto.AvancePartidaRequest;
import com.convecsa.erp.construccion.dto.CronogramaGuardarRequest;
import com.convecsa.erp.construccion.dto.CronogramaPartidaRequest;
import com.convecsa.erp.construccion.dto.CurvaSDTO;
import com.convecsa.erp.construccion.dto.ValorizacionCrearRequest;
import com.convecsa.erp.construccion.dto.ValorizacionDTO;
import com.convecsa.erp.construccion.entity.AdicionalObra;
import com.convecsa.erp.construccion.entity.AvanceObra;
import com.convecsa.erp.construccion.entity.AvancePartida;
import com.convecsa.erp.construccion.entity.CronogramaPartida;
import com.convecsa.erp.construccion.entity.FacturaObra;
import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.PresupuestoVersion;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.Valorizacion;
import com.convecsa.erp.construccion.entity.ValorizacionItem;
import com.convecsa.erp.construccion.repository.AdicionalObraRepository;
import com.convecsa.erp.construccion.repository.AvanceObraRepository;
import com.convecsa.erp.construccion.repository.CronogramaPartidaRepository;
import com.convecsa.erp.construccion.repository.FacturaObraRepository;
import com.convecsa.erp.construccion.repository.PartidaPresupuestoRepository;
import com.convecsa.erp.construccion.repository.PresupuestoVersionRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ValorizacionItemRepository;
import com.convecsa.erp.construccion.repository.ValorizacionRepository;
import com.convecsa.erp.construccion.service.impl.ValorizacionServiceImpl;
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
class ValorizacionServiceImplTest {

    @Mock
    private ProyectoRepository proyectoRepository;
    @Mock
    private PresupuestoVersionRepository presupuestoVersionRepository;
    @Mock
    private PartidaPresupuestoRepository partidaPresupuestoRepository;
    @Mock
    private CronogramaPartidaRepository cronogramaPartidaRepository;
    @Mock
    private AvanceObraRepository avanceObraRepository;
    @Mock
    private AdicionalObraRepository adicionalObraRepository;
    @Mock
    private ValorizacionRepository valorizacionRepository;
    @Mock
    private ValorizacionItemRepository valorizacionItemRepository;
    @Mock
    private FacturaObraRepository facturaObraRepository;
    @Mock
    private AuditoriaObraRegistrar auditoriaObraRegistrar;

    @InjectMocks
    private ValorizacionServiceImpl servicio;

    private final Map<Long, AvanceObra> avances = new LinkedHashMap<>();
    private final Map<Long, Valorizacion> valorizaciones = new LinkedHashMap<>();
    private final Map<Long, AdicionalObra> adicionales = new LinkedHashMap<>();
    private final List<CronogramaPartida> cronogramas = new ArrayList<>();
    private Proyecto proyecto;
    private PresupuestoVersion vigente;
    private PartidaPresupuesto partida;

    @BeforeEach
    void preparar() {
        proyecto = new Proyecto();
        proyecto.setId(7L);
        proyecto.setCodigo("OB-01");
        proyecto.setFechaInicio(LocalDate.of(2026, 11, 1));
        proyecto.setFechaFin(LocalDate.of(2026, 11, 30));
        proyecto.setPorcentajeRetencion(new BigDecimal("10.00"));
        proyecto.setClienteRazon("Cliente SAC");
        proyecto.setClienteRuc("20123456789");

        vigente = new PresupuestoVersion();
        vigente.setId(10L);
        vigente.setProyecto(proyecto);
        vigente.setTipo(TipoVersionPresupuesto.INICIAL);
        vigente.setEstado(EstadoVersionPresupuesto.VIGENTE);
        vigente.setNumero(1);

        partida = new PartidaPresupuesto();
        partida.setId(3L);
        partida.setVersion(vigente);
        partida.setCodigo("01.01");
        partida.setDescripcion("Muros");
        partida.setUnidad("M2");
        partida.setMetrado(new BigDecimal("20.0000"));
        partida.setPrecioUnitario(new BigDecimal("10.00"));
        partida.setTotal(new BigDecimal("200.00"));
        partida.setOrden(0);

        lenient().when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        lenient().when(presupuestoVersionRepository.findByProyectoIdAndEstado(7L, EstadoVersionPresupuesto.VIGENTE))
                .thenReturn(Optional.of(vigente));
        lenient().when(partidaPresupuestoRepository.findByVersionIdOrderByOrdenAsc(10L)).thenReturn(List.of(partida));
        lenient().when(partidaPresupuestoRepository.findById(3L)).thenReturn(Optional.of(partida));
        lenient().when(avanceObraRepository.findByProyectoIdAndAnioAndMes(eq(7L), any(Integer.class), any(Integer.class)))
                .thenAnswer(invocation -> avances.values().stream()
                        .filter(item -> item.getAnio() == (Integer) invocation.getArgument(1)
                                && item.getMes() == (Integer) invocation.getArgument(2))
                        .findFirst());
        lenient().when(avanceObraRepository.findByIdAndProyectoId(any(), eq(7L)))
                .thenAnswer(invocation -> Optional.ofNullable(avances.get(invocation.getArgument(0))));
        lenient().when(avanceObraRepository.findByProyectoIdOrderByAnioAscMesAsc(7L))
                .thenAnswer(invocation -> List.copyOf(avances.values()));
        lenient().when(avanceObraRepository.save(any(AvanceObra.class))).thenAnswer(invocation -> {
            AvanceObra avance = invocation.getArgument(0);
            if (avance.getId() == null) {
                avance.setId((long) avances.size() + 1);
            }
            avances.put(avance.getId(), avance);
            return avance;
        });
        lenient().when(valorizacionRepository.findByProyectoIdOrderByNumeroDesc(7L))
                .thenAnswer(invocation -> new ArrayList<>(valorizaciones.values()));
        lenient().when(valorizacionRepository.findByIdAndProyectoId(any(), eq(7L)))
                .thenAnswer(invocation -> Optional.ofNullable(valorizaciones.get(invocation.getArgument(0))));
        lenient().when(valorizacionRepository.findByProyectoIdAndTipoAndAnioAndMes(eq(7L), any(), any(Integer.class), any(Integer.class)))
                .thenAnswer(invocation -> valorizaciones.values().stream()
                        .filter(item -> item.getTipo() == invocation.getArgument(1)
                                && item.getAnio() == (Integer) invocation.getArgument(2)
                                && item.getMes() == (Integer) invocation.getArgument(3))
                        .findFirst());
        lenient().when(valorizacionRepository.findByProyectoIdAndTipoAndEstadoOrderByAnioDescMesDesc(
                        eq(7L), eq(TipoValorizacion.REGULAR), eq(EstadoValorizacion.APROBADA)))
                .thenAnswer(invocation -> valorizaciones.values().stream()
                        .filter(item -> item.getTipo() == TipoValorizacion.REGULAR
                                && item.getEstado() == EstadoValorizacion.APROBADA)
                        .toList());
        lenient().when(valorizacionRepository.findByProyectoIdAndEstado(7L, EstadoValorizacion.APROBADA))
                .thenAnswer(invocation -> valorizaciones.values().stream()
                        .filter(item -> item.getEstado() == EstadoValorizacion.APROBADA)
                        .toList());
        lenient().when(valorizacionRepository.findFirstByProyectoIdOrderByNumeroDesc(7L))
                .thenAnswer(invocation -> valorizaciones.values().stream().reduce((a, b) -> b));
        lenient().when(valorizacionRepository.save(any(Valorizacion.class))).thenAnswer(invocation -> {
            Valorizacion valorizacion = invocation.getArgument(0);
            if (valorizacion.getId() == null) {
                valorizacion.setId((long) valorizaciones.size() + 1);
            }
            valorizaciones.put(valorizacion.getId(), valorizacion);
            return valorizacion;
        });
        lenient().when(adicionalObraRepository.findByProyectoIdOrderByIdDesc(7L))
                .thenAnswer(invocation -> List.copyOf(adicionales.values()));
        lenient().when(adicionalObraRepository.findByIdAndProyectoId(any(), eq(7L)))
                .thenAnswer(invocation -> Optional.ofNullable(adicionales.get(invocation.getArgument(0))));
        lenient().when(adicionalObraRepository.findByProyectoIdAndTipoAndEstado(eq(7L), any(), any()))
                .thenAnswer(invocation -> adicionales.values().stream()
                        .filter(item -> item.getTipo() == invocation.getArgument(1)
                                && item.getEstado() == invocation.getArgument(2))
                        .toList());
        lenient().when(adicionalObraRepository.save(any(AdicionalObra.class))).thenAnswer(invocation -> {
            AdicionalObra adicional = invocation.getArgument(0);
            if (adicional.getId() == null) {
                adicional.setId((long) adicionales.size() + 1);
            }
            adicionales.put(adicional.getId(), adicional);
            return adicional;
        });
        lenient().when(valorizacionItemRepository.findByAdicionalId(any())).thenReturn(List.of());
        lenient().when(facturaObraRepository.existsByValorizacionId(any())).thenReturn(false);
        lenient().when(facturaObraRepository.findFirstByProyectoIdOrderByNumeroDesc(7L)).thenReturn(Optional.empty());
        lenient().when(facturaObraRepository.save(any(FacturaObra.class))).thenAnswer(invocation -> {
            FacturaObra factura = invocation.getArgument(0);
            factura.setId(1L);
            factura.getValorizacion().setFactura(factura);
            return factura;
        });
        lenient().when(cronogramaPartidaRepository.findByProyectoIdOrderByCodigoAsc(7L)).thenAnswer(invocation -> List.copyOf(cronogramas));
        lenient().doNothing().when(cronogramaPartidaRepository).deleteByProyectoId(7L);
        lenient().doNothing().when(cronogramaPartidaRepository).flush();
        lenient().when(cronogramaPartidaRepository.save(any(CronogramaPartida.class))).thenAnswer(invocation -> {
            CronogramaPartida fila = invocation.getArgument(0);
            cronogramas.add(fila);
            return fila;
        });
    }

    @Test
    void avanceAcumulaPeriodosDeMesesSiguientes() {
        guardarAvance(2026, 10, "10");
        AvanceObraDTO segundo = guardarAvance(2026, 11, "4");
        assertEquals(new BigDecimal("4.0000"), segundo.getItems().get(0).getMetradoPeriodo());
        assertEquals(new BigDecimal("14.0000"), segundo.getItems().get(0).getMetradoAcumulado());
    }

    @Test
    void valorizacionRegularUsaPeriodoYRetencion() {
        AvanceObraDTO primero = guardarAvance(2026, 10, "10");
        servicio.cerrarAvance(7L, primero.getId());
        ValorizacionDTO primera = crearRegular(2026, 10);
        primera = servicio.presentarValorizacion(7L, primera.getId());
        servicio.aprobarValorizacion(7L, primera.getId());
        verify(auditoriaObraRegistrar).registrar(
                eq(7L),
                eq(AccionAuditoriaObra.APROBAR_VALORIZACION),
                eq(AuditoriaObraRegistrar.ENTIDAD_VALORIZACION),
                eq(primera.getId()),
                any());

        AvanceObraDTO segundo = guardarAvance(2026, 11, "4");
        servicio.cerrarAvance(7L, segundo.getId());
        ValorizacionDTO dto = crearRegular(2026, 11);

        assertEquals(new BigDecimal("10.0000"), dto.getItems().get(0).getMetradoAnterior());
        assertEquals(new BigDecimal("4.0000"), dto.getItems().get(0).getMetradoPeriodo());
        assertEquals(new BigDecimal("14.0000"), dto.getItems().get(0).getMetradoAcumulado());
        assertEquals(new BigDecimal("40.00"), dto.getMontoBruto());
        assertEquals(new BigDecimal("4.00"), dto.getMontoRetencion());
        assertEquals(new BigDecimal("36.00"), dto.getMontoNeto());
    }

    @Test
    void rechazaSegundaRegularDelMismoMesYFacturaSinAprobar() {
        AvanceObraDTO avance = guardarAvance(2026, 11, "4");
        servicio.cerrarAvance(7L, avance.getId());
        ValorizacionDTO dto = crearRegular(2026, 11);
        servicio.presentarValorizacion(7L, dto.getId());

        IllegalArgumentException duplicada = assertThrows(
                IllegalArgumentException.class,
                () -> crearRegular(2026, 11));
        assertEquals("Ya existe una valorización regular de ese mes", duplicada.getMessage());

        IllegalArgumentException factura = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.facturarValorizacion(7L, dto.getId()));
        assertEquals("Solo se puede facturar una valorización aprobada", factura.getMessage());
    }

    @Test
    void adicionalVigenteEntraEnValorizacionAdicional() {
        AdicionalObraRequest request = new AdicionalObraRequest();
        request.setTipo(TipoAdicionalObra.ADICIONAL);
        request.setCodigo("AD-01");
        request.setDescripcion("Sobremetrado");
        request.setMetrado(new BigDecimal("2"));
        request.setPrecioUnitario(new BigDecimal("15"));
        Long id = servicio.crearAdicional(7L, request).getId();
        servicio.vigentarAdicional(7L, id);

        ValorizacionCrearRequest crear = new ValorizacionCrearRequest();
        crear.setTipo(TipoValorizacion.ADICIONAL);
        crear.setAnio(2026);
        crear.setMes(11);
        ValorizacionDTO dto = servicio.crearValorizacion(7L, crear);

        assertEquals(1, dto.getItems().size());
        assertEquals(new BigDecimal("30.00"), dto.getMontoBruto());
        assertEquals("AD-01", dto.getItems().get(0).getCodigo());
    }

    @Test
    void curvaSProgramaElMetradoEnElMesDelCronograma() {
        CronogramaGuardarRequest cronograma = new CronogramaGuardarRequest();
        CronogramaPartidaRequest fila = new CronogramaPartidaRequest();
        fila.setPartidaId(3L);
        fila.setFechaInicio(LocalDate.of(2026, 11, 1));
        fila.setFechaFin(LocalDate.of(2026, 11, 30));
        cronograma.setPartidas(List.of(fila));
        servicio.guardarCronograma(7L, cronograma);

        CurvaSDTO curva = servicio.curvaS(7L);
        assertEquals(1, curva.getPuntos().size());
        assertEquals(new BigDecimal("20.0000"), curva.getPuntos().get(0).getProgramadoFisico());
        assertEquals(new BigDecimal("200.00"), curva.getPuntos().get(0).getProgramadoEconomico());
    }

    private AvanceObraDTO guardarAvance(int anio, int mes, String periodo) {
        AvanceGuardarRequest request = new AvanceGuardarRequest();
        request.setAnio(anio);
        request.setMes(mes);
        AvancePartidaRequest item = new AvancePartidaRequest();
        item.setPartidaId(3L);
        item.setMetradoPeriodo(new BigDecimal(periodo));
        request.setItems(List.of(item));
        return servicio.guardarAvance(7L, request);
    }

    private ValorizacionDTO crearRegular(int anio, int mes) {
        ValorizacionCrearRequest request = new ValorizacionCrearRequest();
        request.setTipo(TipoValorizacion.REGULAR);
        request.setAnio(anio);
        request.setMes(mes);
        return servicio.crearValorizacion(7L, request);
    }
}
