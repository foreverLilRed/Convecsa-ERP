package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.asistencia.entity.AsistenciaRegistro;
import com.convecsa.erp.asistencia.repository.AsistenciaRegistroRepository;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import com.convecsa.erp.construccion.repository.ContratoCcRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.TareoCcDiaRepository;
import com.convecsa.erp.soma.domain.TipoEventoSst;
import com.convecsa.erp.soma.dto.SomaEstadisticaAnioDTO;
import com.convecsa.erp.soma.dto.SomaEstadisticaMesDTO;
import com.convecsa.erp.soma.entity.SomaEventoSst;
import com.convecsa.erp.soma.repository.SomaEventoSstRepository;
import com.convecsa.erp.soma.service.InterfazSomaEstadisticaService;
import com.convecsa.erp.soma.support.SomaEstadisticaCalculator;
import com.convecsa.erp.soma.support.SomaEstadisticaCalculator.IndicesSst;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaEstadisticaServiceImpl implements InterfazSomaEstadisticaService {

    static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    static final List<TipoEventoSst> COMPUTABLES =
            List.of(TipoEventoSst.ACCIDENTE_INCAPACITANTE, TipoEventoSst.ACCIDENTE_MORTAL);

    private final ProyectoRepository proyectoRepository;
    private final TareoCcDiaRepository tareoCcDiaRepository;
    private final SomaEventoSstRepository somaEventoSstRepository;
    private final AsistenciaRegistroRepository asistenciaRegistroRepository;
    private final ContratoCcRepository contratoCcRepository;

    @Override
    @Transactional(readOnly = true)
    public SomaEstadisticaAnioDTO porObra(Long proyectoId, Integer anio) {
        exigirProyecto(proyectoId);
        validarAnio(anio);
        RangoAnio rango = rango(anio);
        BigDecimal[] hht = hhtVacio();
        for (TareoCcDia dia :
                tareoCcDiaRepository.findByPeriodoProyectoIdAndFechaBetween(proyectoId, rango.desde, rango.hasta)) {
            acumularHht(hht, dia.getFecha(), SomaEstadisticaCalculator.horasDia(dia.getHorasOrd(), dia.getHe60(), dia.getHe100()));
        }
        TotalesEvento eventos = totalesEventos(
                somaEventoSstRepository.findByProyectoIdAndTipoEventoInAndFechaHoraGreaterThanEqualAndFechaHoraLessThan(
                        proyectoId, COMPUTABLES, rango.inicio, rango.finExclusivo));
        SomaEventoSst ultimo = somaEventoSstRepository
                .findFirstByProyectoIdAndTipoEventoInOrderByFechaHoraDesc(proyectoId, COMPUTABLES)
                .orElse(null);
        return armar(anio, hht, eventos.accidentes, eventos.diasPerdidos, ultimo);
    }

    @Override
    @Transactional(readOnly = true)
    public SomaEstadisticaAnioDTO corporativo(Integer anio) {
        validarAnio(anio);
        RangoAnio rango = rango(anio);
        BigDecimal[] hht = hhtVacio();
        for (TareoCcDia dia : tareoCcDiaRepository.findByFechaBetween(rango.desde, rango.hasta)) {
            acumularHht(hht, dia.getFecha(), SomaEstadisticaCalculator.horasDia(dia.getHorasOrd(), dia.getHe60(), dia.getHe100()));
        }
        Set<Long> contratosCc = new HashSet<>(contratoCcRepository.findAllContratoIds());
        for (AsistenciaRegistro fila :
                asistenciaRegistroRepository.findByFechaBetweenOrderByPersonaApellidosAscPersonaNombresAscFechaAsc(
                        rango.desde, rango.hasta)) {
            Long contratoId = fila.getContrato() == null ? null : fila.getContrato().getId();
            if (contratoId != null && contratosCc.contains(contratoId)) {
                continue;
            }
            acumularHht(hht, fila.getFecha(), fila.getHorasTrabajadas());
        }
        TotalesEvento eventos = totalesEventos(
                somaEventoSstRepository.findByTipoEventoInAndFechaHoraGreaterThanEqualAndFechaHoraLessThan(
                        COMPUTABLES, rango.inicio, rango.finExclusivo));
        SomaEventoSst ultimo = somaEventoSstRepository
                .findFirstByTipoEventoInOrderByFechaHoraDesc(COMPUTABLES)
                .orElse(null);
        return armar(anio, hht, eventos.accidentes, eventos.diasPerdidos, ultimo);
    }

    private SomaEstadisticaAnioDTO armar(
            int anio, BigDecimal[] hht, int[] accidentes, int[] diasPerdidos, SomaEventoSst ultimo) {
        List<SomaEstadisticaMesDTO> meses = new ArrayList<>(12);
        BigDecimal hhtAnio = SomaEstadisticaCalculator.cero();
        int accidentesAnio = 0;
        int diasAnio = 0;
        for (int mes = 1; mes <= 12; mes++) {
            SomaEstadisticaMesDTO fila = mesDto(mes, hht[mes], accidentes[mes], diasPerdidos[mes]);
            meses.add(fila);
            hhtAnio = hhtAnio.add(hht[mes]);
            accidentesAnio += accidentes[mes];
            diasAnio += diasPerdidos[mes];
        }
        return SomaEstadisticaAnioDTO.builder()
                .anio(anio)
                .meses(meses)
                .acumulado(mesDto(null, hhtAnio, accidentesAnio, diasAnio))
                .diasSinAccidente(diasSinAccidente(ultimo))
                .build();
    }

    private static SomaEstadisticaMesDTO mesDto(Integer mes, BigDecimal hht, int accidentes, int diasPerdidos) {
        BigDecimal horas = hht == null ? SomaEstadisticaCalculator.cero() : hht.setScale(SomaEstadisticaCalculator.ESCALA, SomaEstadisticaCalculator.MODO);
        IndicesSst indices = SomaEstadisticaCalculator.indices(accidentes, diasPerdidos, horas);
        return SomaEstadisticaMesDTO.builder()
                .mes(mes)
                .hht(horas)
                .accidentes(accidentes)
                .diasPerdidos(diasPerdidos)
                .indiceFrecuencia(indices.indiceFrecuencia())
                .indiceSeveridad(indices.indiceSeveridad())
                .indiceAccidentabilidad(indices.indiceAccidentabilidad())
                .build();
    }

    private static void acumularHht(BigDecimal[] hht, LocalDate fecha, BigDecimal horas) {
        if (fecha == null) {
            return;
        }
        int mes = fecha.getMonthValue();
        hht[mes] = hht[mes].add(SomaEstadisticaCalculator.horasDia(horas, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    private static TotalesEvento totalesEventos(List<SomaEventoSst> filas) {
        int[] accidentes = new int[13];
        int[] dias = new int[13];
        for (SomaEventoSst evento : filas) {
            if (evento.getFechaHora() == null) {
                continue;
            }
            int mes = evento.getFechaHora().atZone(ZONA_LIMA).getMonthValue();
            accidentes[mes]++;
            dias[mes] += evento.getDiasDescansoMedico() + evento.getDiasCargoEstadistico();
        }
        return new TotalesEvento(accidentes, dias);
    }

    static Integer diasSinAccidente(SomaEventoSst ultimo) {
        if (ultimo == null || ultimo.getFechaHora() == null) {
            return null;
        }
        LocalDate fecha = ultimo.getFechaHora().atZone(ZONA_LIMA).toLocalDate();
        LocalDate hoy = LocalDate.now(ZONA_LIMA);
        if (fecha.isAfter(hoy)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(fecha, hoy);
    }

    static void validarAnio(Integer anio) {
        if (anio == null || anio < 2000 || anio > 2100) {
            throw new IllegalArgumentException("El año es obligatorio y debe ser válido");
        }
    }

    private void exigirProyecto(Long proyectoId) {
        if (proyectoId == null || !proyectoRepository.existsById(proyectoId)) {
            throw new ProyectoNoEncontradoException("No se encontró el proyecto");
        }
    }

    private static BigDecimal[] hhtVacio() {
        BigDecimal[] hht = new BigDecimal[13];
        for (int i = 0; i < hht.length; i++) {
            hht[i] = BigDecimal.ZERO;
        }
        return hht;
    }

    private static RangoAnio rango(int anio) {
        LocalDate desde = LocalDate.of(anio, 1, 1);
        LocalDate hasta = LocalDate.of(anio, 12, 31);
        Instant inicio = desde.atStartOfDay(ZONA_LIMA).toInstant();
        Instant finExclusivo = hasta.plusDays(1).atStartOfDay(ZONA_LIMA).toInstant();
        return new RangoAnio(desde, hasta, inicio, finExclusivo);
    }

    private record RangoAnio(LocalDate desde, LocalDate hasta, Instant inicio, Instant finExclusivo) {}

    private record TotalesEvento(int[] accidentes, int[] diasPerdidos) {}
}
