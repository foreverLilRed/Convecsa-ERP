package com.convecsa.erp.soma.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.convecsa.erp.asistencia.entity.AsistenciaRegistro;
import com.convecsa.erp.asistencia.repository.AsistenciaRegistroRepository;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import com.convecsa.erp.construccion.repository.ContratoCcRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.TareoCcDiaRepository;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.soma.dto.SomaEstadisticaAnioDTO;
import com.convecsa.erp.soma.repository.SomaEventoSstRepository;
import com.convecsa.erp.soma.service.impl.SomaEstadisticaServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SomaEstadisticaServiceImplTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private TareoCcDiaRepository tareoCcDiaRepository;

    @Mock
    private SomaEventoSstRepository somaEventoSstRepository;

    @Mock
    private AsistenciaRegistroRepository asistenciaRegistroRepository;

    @Mock
    private ContratoCcRepository contratoCcRepository;

    @InjectMocks
    private SomaEstadisticaServiceImpl servicio;

    @BeforeEach
    void stubObra() {
        lenient().when(proyectoRepository.existsById(10L)).thenReturn(true);
    }

    @Test
    void tareoDeMarzoSumaHhtDeLaObra() {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 12, 31);
        TareoCcDia dia = new TareoCcDia();
        dia.setFecha(LocalDate.of(2026, 3, 10));
        dia.setHorasOrd(new BigDecimal("8.00"));
        dia.setHe60(new BigDecimal("2.00"));
        dia.setHe100(BigDecimal.ZERO);
        when(tareoCcDiaRepository.findByPeriodoProyectoIdAndFechaBetween(10L, desde, hasta)).thenReturn(List.of(dia));
        when(somaEventoSstRepository.findByProyectoIdAndTipoEventoInAndFechaHoraGreaterThanEqualAndFechaHoraLessThan(
                        org.mockito.ArgumentMatchers.eq(10L),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());
        when(somaEventoSstRepository.findFirstByProyectoIdAndTipoEventoInOrderByFechaHoraDesc(
                        org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.Optional.empty());

        SomaEstadisticaAnioDTO dto = servicio.porObra(10L, 2026);
        assertEquals(new BigDecimal("10.00"), dto.getMeses().get(2).getHht());
        assertEquals(0, dto.getMeses().get(2).getAccidentes());
        assertEquals(new BigDecimal("10.00"), dto.getAcumulado().getHht());
    }

    @Test
    void corporativoExcluyeAsistenciaDeContratoCc() {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 12, 31);
        TareoCcDia tareo = new TareoCcDia();
        tareo.setFecha(LocalDate.of(2026, 4, 1));
        tareo.setHorasOrd(new BigDecimal("8.00"));
        tareo.setHe60(BigDecimal.ZERO);
        tareo.setHe100(BigDecimal.ZERO);
        when(tareoCcDiaRepository.findByFechaBetween(desde, hasta)).thenReturn(List.of(tareo));
        when(contratoCcRepository.findAllContratoIds()).thenReturn(List.of(50L));

        Contrato cc = new Contrato();
        cc.setId(50L);
        AsistenciaRegistro oficinaCc = new AsistenciaRegistro();
        oficinaCc.setFecha(LocalDate.of(2026, 4, 2));
        oficinaCc.setHorasTrabajadas(new BigDecimal("8.00"));
        oficinaCc.setContrato(cc);

        Contrato oficina = new Contrato();
        oficina.setId(90L);
        AsistenciaRegistro oficinaSinCc = new AsistenciaRegistro();
        oficinaSinCc.setFecha(LocalDate.of(2026, 4, 3));
        oficinaSinCc.setHorasTrabajadas(new BigDecimal("7.00"));
        oficinaSinCc.setContrato(oficina);

        when(asistenciaRegistroRepository.findByFechaBetweenOrderByPersonaApellidosAscPersonaNombresAscFechaAsc(
                        desde, hasta))
                .thenReturn(List.of(oficinaCc, oficinaSinCc));
        when(somaEventoSstRepository.findByTipoEventoInAndFechaHoraGreaterThanEqualAndFechaHoraLessThan(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());
        when(somaEventoSstRepository.findFirstByTipoEventoInOrderByFechaHoraDesc(org.mockito.ArgumentMatchers.any()))
                .thenReturn(java.util.Optional.empty());

        SomaEstadisticaAnioDTO dto = servicio.corporativo(2026);
        assertEquals(new BigDecimal("15.00"), dto.getMeses().get(3).getHht());
        assertEquals(new BigDecimal("15.00"), dto.getAcumulado().getHht());
    }

    @Test
    void anioAusenteEs400() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> servicio.porObra(10L, null));
        assertEquals("El año es obligatorio y debe ser válido", ex.getMessage());
    }
}
