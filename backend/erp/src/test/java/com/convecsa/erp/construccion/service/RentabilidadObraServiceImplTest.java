package com.convecsa.erp.construccion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import com.convecsa.erp.construccion.domain.EstadoFactoring;
import com.convecsa.erp.construccion.domain.EstadoPeriodoCc;
import com.convecsa.erp.construccion.domain.EstadoValorizacion;
import com.convecsa.erp.construccion.dto.AsientoObraDTO;
import com.convecsa.erp.construccion.dto.AsientoObraLineaDTO;
import com.convecsa.erp.construccion.dto.RentabilidadObraDTO;
import com.convecsa.erp.construccion.dto.RentabilidadPuntoDTO;
import com.convecsa.erp.construccion.entity.AlmacenObra;
import com.convecsa.erp.construccion.entity.BoletaCc;
import com.convecsa.erp.construccion.entity.ImputacionEquipo;
import com.convecsa.erp.construccion.entity.OperacionFactoring;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.SubcontratoAvance;
import com.convecsa.erp.construccion.entity.ValeSalida;
import com.convecsa.erp.construccion.entity.ValeSalidaItem;
import com.convecsa.erp.construccion.entity.Valorizacion;
import com.convecsa.erp.construccion.repository.AlmacenObraRepository;
import com.convecsa.erp.construccion.repository.BoletaCcRepository;
import com.convecsa.erp.construccion.repository.ImputacionEquipoRepository;
import com.convecsa.erp.construccion.repository.OperacionFactoringRepository;
import com.convecsa.erp.construccion.repository.PeriodoCcRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.SubcontratoAvanceRepository;
import com.convecsa.erp.construccion.repository.ValeSalidaRepository;
import com.convecsa.erp.construccion.repository.ValorizacionRepository;
import com.convecsa.erp.construccion.service.impl.RentabilidadObraServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RentabilidadObraServiceImplTest {

    @Mock
    private ProyectoRepository proyectoRepository;
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
    private OperacionFactoringRepository operacionFactoringRepository;
    @Mock
    private ImputacionEquipoRepository imputacionEquipoRepository;
    @Mock
    private SubcontratoAvanceRepository subcontratoAvanceRepository;

    @InjectMocks
    private RentabilidadObraServiceImpl servicio;

    private final List<Valorizacion> valorizaciones = new ArrayList<>();
    private final List<PeriodoCc> periodos = new ArrayList<>();
    private final List<BoletaCc> boletas = new ArrayList<>();
    private final List<ValeSalida> vales = new ArrayList<>();
    private final List<OperacionFactoring> factoring = new ArrayList<>();
    private Proyecto proyecto;

    @BeforeEach
    void preparar() {
        proyecto = new Proyecto();
        proyecto.setId(7L);
        proyecto.setCodigo("OB001");
        proyecto.setCentroCosto("CC");
        proyecto.setFechaInicio(LocalDate.of(2026, 11, 1));
        proyecto.setFechaFin(LocalDate.of(2026, 12, 31));

        valorizaciones.add(valorizacion(1L, EstadoValorizacion.APROBADA, 2026, 11, "100.00"));
        valorizaciones.add(valorizacion(2L, EstadoValorizacion.BORRADOR, 2026, 11, "50.00"));

        PeriodoCc cerrado = periodo(11L, EstadoPeriodoCc.CERRADO, LocalDate.of(2026, 11, 20));
        PeriodoCc borrador = periodo(12L, EstadoPeriodoCc.BORRADOR, LocalDate.of(2026, 11, 25));
        periodos.add(cerrado);
        periodos.add(borrador);
        boletas.add(boleta(cerrado, "40.00", "10.00"));
        boletas.add(boleta(borrador, "999.00", "99.00"));

        ValeSalida vale = new ValeSalida();
        vale.setFecha(LocalDate.of(2026, 11, 15));
        vale.setCentroCosto("F1");
        ValeSalidaItem item = new ValeSalidaItem();
        item.setCostoTotal(new BigDecimal("25.00"));
        vale.getItems().add(item);
        vales.add(vale);

        OperacionFactoring operacion = new OperacionFactoring();
        operacion.setFechaAdelanto(LocalDate.of(2026, 11, 12));
        operacion.setEstado(EstadoFactoring.ADELANTADA);
        operacion.setImporteNominal(new BigDecimal("100.00"));
        operacion.setDescuento(new BigDecimal("5.00"));
        operacion.setComision(new BigDecimal("10.00"));
        operacion.setIgvComision(new BigDecimal("1.80"));
        operacion.setImporteNeto(new BigDecimal("83.20"));
        factoring.add(operacion);

        AlmacenObra almacen = new AlmacenObra();
        almacen.setId(9L);
        almacen.setProyecto(proyecto);

        lenient().when(proyectoRepository.findById(7L)).thenReturn(Optional.of(proyecto));
        lenient().when(valorizacionRepository.findByProyectoIdOrderByNumeroDesc(7L))
                .thenAnswer(invocation -> List.copyOf(valorizaciones));
        lenient().when(periodoCcRepository.findByProyectoIdOrderByFechaDesdeDesc(7L))
                .thenAnswer(invocation -> List.copyOf(periodos));
        lenient().when(boletaCcRepository.findByPeriodoIdIn(any())).thenAnswer(invocation -> {
            List<Long> ids = invocation.getArgument(0);
            return boletas.stream()
                    .filter(boleta -> ids.contains(boleta.getPeriodo().getId()))
                    .toList();
        });
        lenient().when(almacenObraRepository.findByProyectoId(7L)).thenReturn(Optional.of(almacen));
        lenient().when(valeSalidaRepository.findByAlmacenIdOrderByNumeroDesc(9L))
                .thenAnswer(invocation -> List.copyOf(vales));
        lenient().when(operacionFactoringRepository.findByProyectoIdOrderByFechaCesionDescIdDesc(7L))
                .thenAnswer(invocation -> List.copyOf(factoring));
        lenient().when(imputacionEquipoRepository.findByProyectoId(7L)).thenReturn(List.of());
        lenient().when(subcontratoAvanceRepository.findByProyectoId(7L)).thenReturn(List.of());
    }

    @Test
    void margenNoviembreConIngresoMoMaterialesYFactoring() {
        RentabilidadObraDTO dto = rentabilidadNoviembre();
        assertEquals(new BigDecimal("100.00"), dto.getIngreso());
        assertEquals(new BigDecimal("50.00"), dto.getCostoMo());
        assertEquals(new BigDecimal("25.00"), dto.getCostoMateriales());
        assertEquals(new BigDecimal("16.80"), dto.getCostoFactoring());
        assertEquals(new BigDecimal("25.00"), dto.getMargenBruto());
        assertEquals(new BigDecimal("8.20"), dto.getMargenNeto());

        RentabilidadPuntoDTO punto = dto.getPuntos().get(0);
        assertEquals(2026, punto.getAnio());
        assertEquals(11, punto.getMes());
        assertEquals(new BigDecimal("25.00"), punto.getMargenBrutoAcumulado());
        assertEquals(new BigDecimal("8.20"), punto.getMargenNetoAcumulado());
    }

    @Test
    void valorizacionBorradorYPeriodoAbiertoNoSuman() {
        RentabilidadObraDTO dto = rentabilidadNoviembre();
        assertEquals(new BigDecimal("100.00"), dto.getIngreso());
        assertEquals(new BigDecimal("50.00"), dto.getCostoMo());
    }

    @Test
    void asientoMaterialesAgrupaPorCentro() {
        vales.clear();
        ValeSalida vale = new ValeSalida();
        vale.setFecha(LocalDate.of(2026, 11, 18));
        vale.setCentroCosto("F1");
        ValeSalidaItem item = new ValeSalidaItem();
        item.setCostoTotal(new BigDecimal("80.00"));
        vale.getItems().add(item);
        vales.add(vale);

        AsientoObraDTO asiento = servicio.asientos(
                7L, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)).getMateriales();
        assertEquals(new BigDecimal("160.00"), asiento.getTotalDebe());
        assertEquals(new BigDecimal("160.00"), asiento.getTotalHaber());
        assertEquals(new BigDecimal("0.00"), asiento.getDiferencia());
        assertEquals(new BigDecimal("80.00"), linea(asiento, "61").getDebe());
        assertEquals(new BigDecimal("80.00"), linea(asiento, "24").getHaber());
        AsientoObraLineaDTO destino = linea(asiento, "92");
        assertEquals("OB001 / F1", destino.getAuxiliar());
        assertEquals(new BigDecimal("80.00"), destino.getDebe());
        assertEquals(new BigDecimal("80.00"), linea(asiento, "791").getHaber());
    }

    @Test
    void asientoFactoringCuadraNetoDescuentoComisionEIgv() {
        AsientoObraDTO asiento = servicio.asientos(
                7L, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)).getFactoring();
        assertEquals(new BigDecimal("100.00"), asiento.getTotalDebe());
        assertEquals(new BigDecimal("100.00"), asiento.getTotalHaber());
        assertEquals(new BigDecimal("0.00"), asiento.getDiferencia());
        assertEquals(new BigDecimal("83.20"), linea(asiento, "1041").getDebe());
        assertEquals(new BigDecimal("15.00"), linea(asiento, "67").getDebe());
        assertEquals(new BigDecimal("1.80"), linea(asiento, "4011").getDebe());
        assertEquals(new BigDecimal("100.00"), linea(asiento, "12").getHaber());
    }

    @Test
    void margenIncluyeEquiposYSubcontratos() {
        ImputacionEquipo imputacion = new ImputacionEquipo();
        imputacion.setFecha(LocalDate.of(2026, 11, 8));
        imputacion.setCostoTotal(new BigDecimal("40.00"));
        SubcontratoAvance avance = new SubcontratoAvance();
        avance.setFecha(LocalDate.of(2026, 11, 9));
        avance.setMonto(new BigDecimal("25.00"));
        lenient().when(imputacionEquipoRepository.findByProyectoId(7L)).thenReturn(List.of(imputacion));
        lenient().when(subcontratoAvanceRepository.findByProyectoId(7L)).thenReturn(List.of(avance));

        RentabilidadObraDTO dto = rentabilidadNoviembre();
        assertEquals(new BigDecimal("40.00"), dto.getCostoEquipos());
        assertEquals(new BigDecimal("25.00"), dto.getCostoSubcontratos());
        assertEquals(new BigDecimal("-40.00"), dto.getMargenBruto());
        assertEquals(new BigDecimal("-56.80"), dto.getMargenNeto());
    }

    @Test
    void acumuladoNetoArrastraElMesAnterior() {
        valorizaciones.add(valorizacion(3L, EstadoValorizacion.APROBADA, 2026, 12, "50.00"));
        RentabilidadObraDTO dto = servicio.rentabilidad(
                7L, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 31));
        assertEquals(2, dto.getPuntos().size());
        RentabilidadPuntoDTO noviembre = dto.getPuntos().get(0);
        RentabilidadPuntoDTO diciembre = dto.getPuntos().get(1);
        assertEquals(new BigDecimal("8.20"), noviembre.getMargenNetoAcumulado());
        assertEquals(new BigDecimal("50.00"), diciembre.getIngreso());
        assertEquals(new BigDecimal("50.00"), diciembre.getMargenNeto());
        assertEquals(new BigDecimal("58.20"), diciembre.getMargenNetoAcumulado());
        assertEquals(new BigDecimal("75.00"), diciembre.getMargenBrutoAcumulado());
    }

    private RentabilidadObraDTO rentabilidadNoviembre() {
        return servicio.rentabilidad(7L, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));
    }

    private static Valorizacion valorizacion(Long id, EstadoValorizacion estado, int anio, int mes, String bruto) {
        Valorizacion item = new Valorizacion();
        item.setId(id);
        item.setEstado(estado);
        item.setAnio(anio);
        item.setMes(mes);
        item.setMontoBruto(new BigDecimal(bruto));
        return item;
    }

    private static PeriodoCc periodo(Long id, EstadoPeriodoCc estado, LocalDate fechaHasta) {
        PeriodoCc periodo = new PeriodoCc();
        periodo.setId(id);
        periodo.setEstado(estado);
        periodo.setFechaHasta(fechaHasta);
        return periodo;
    }

    private static BoletaCc boleta(PeriodoCc periodo, String haberes, String aportes) {
        BoletaCc boleta = new BoletaCc();
        boleta.setPeriodo(periodo);
        boleta.setTotalHaberes(new BigDecimal(haberes));
        boleta.setTotalAportesEmpleador(new BigDecimal(aportes));
        return boleta;
    }

    private static AsientoObraLineaDTO linea(AsientoObraDTO asiento, String cuenta) {
        return asiento.getBloques().stream()
                .flatMap(bloque -> bloque.getLineas().stream())
                .filter(item -> cuenta.equals(item.getCuenta()))
                .findFirst()
                .orElseThrow();
    }
}
