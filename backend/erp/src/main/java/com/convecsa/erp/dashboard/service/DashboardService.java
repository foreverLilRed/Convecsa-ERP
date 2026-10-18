package com.convecsa.erp.dashboard.service;

import com.convecsa.erp.asistencia.domain.CodigoTareo;
import com.convecsa.erp.asistencia.entity.AsistenciaRegistro;
import com.convecsa.erp.asistencia.repository.AsistenciaRegistroRepository;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.dashboard.dto.DashboardAsistenciaDTO;
import com.convecsa.erp.dashboard.dto.DashboardContratosDTO;
import com.convecsa.erp.dashboard.dto.DashboardNominaDTO;
import com.convecsa.erp.dashboard.dto.DashboardPersonasDTO;
import com.convecsa.erp.nomina.entity.PeriodoNomina;
import com.convecsa.erp.nomina.repository.BoletaPagoRepository;
import com.convecsa.erp.nomina.repository.PeriodoNominaRepository;
import com.convecsa.erp.persona.repository.PersonaRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private final PersonaRepository personaRepository;
    private final ContratoRepository contratoRepository;
    private final PeriodoNominaRepository periodoNominaRepository;
    private final BoletaPagoRepository boletaPagoRepository;
    private final AsistenciaRegistroRepository asistenciaRegistroRepository;
    private final Clock clock = Clock.system(ZONA_LIMA);

    @Transactional(readOnly = true)
    public DashboardPersonasDTO personas() {
        LocalDate hoy = LocalDate.now(clock);
        YearMonth periodo = YearMonth.from(hoy);
        Instant inicioMes = periodo.atDay(1).atStartOfDay(ZONA_LIMA).toInstant();
        Instant finMesExclusivo = periodo.plusMonths(1).atDay(1).atStartOfDay(ZONA_LIMA).toInstant();
        long activos = personaRepository.countByActivoTrue();
        long inactivos = personaRepository.countByActivoFalse();
        return new DashboardPersonasDTO(
                activos + inactivos,
                activos,
                inactivos,
                personaRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(inicioMes, finMesExclusivo),
                personaRepository.countByFechaVigenciaEmoBetween(hoy, hoy.plusDays(30)));
    }

    @Transactional(readOnly = true)
    public DashboardContratosDTO contratos() {
        LocalDate hoy = LocalDate.now(clock);
        YearMonth periodo = YearMonth.from(hoy);
        long activos = contratoRepository.countByActivoTrue();
        long inactivos = contratoRepository.countByActivoFalse();
        return new DashboardContratosDTO(
                activos + inactivos,
                activos,
                inactivos,
                contratoRepository.countVigentesEnFecha(hoy),
                contratoRepository.countByActivoTrueAndFechaFinBetween(hoy, hoy.plusDays(30)),
                contratoRepository.countByActivoTrueAndFechaFinIsNull(),
                contratoRepository.sumSueldosVigentesEnRango(periodo.atDay(1), periodo.atEndOfMonth()));
    }

    @Transactional(readOnly = true)
    public DashboardNominaDTO nomina() {
        YearMonth periodoActual = YearMonth.now(clock);
        LocalDate inicio = periodoActual.atDay(1);
        LocalDate fin = periodoActual.atEndOfMonth();
        PeriodoNomina periodo = periodoNominaRepository
                .findByAnioAndMes(periodoActual.getYear(), periodoActual.getMonthValue())
                .orElse(null);
        if (periodo == null) {
            BigDecimal estimado = contratoRepository.sumSueldosVigentesEnRango(inicio, fin);
            return new DashboardNominaDTO(
                    periodoActual.getYear(), periodoActual.getMonthValue(), null, null,
                    false, true, 0, estimado, BigDecimal.ZERO, BigDecimal.ZERO, estimado);
        }
        Long periodoId = periodo.getId();
        return new DashboardNominaDTO(
                periodo.getAnio(),
                periodo.getMes(),
                periodoId,
                periodo.getEstado(),
                true,
                false,
                boletaPagoRepository.countByPeriodoId(periodoId),
                boletaPagoRepository.sumHaberesByPeriodoId(periodoId),
                boletaPagoRepository.sumDescuentosByPeriodoId(periodoId),
                boletaPagoRepository.sumAportesEmpleadorByPeriodoId(periodoId),
                boletaPagoRepository.sumNetoByPeriodoId(periodoId));
    }

    @Transactional(readOnly = true)
    public DashboardAsistenciaDTO asistencia() {
        YearMonth periodo = YearMonth.now(clock);
        List<AsistenciaRegistro> registros = asistenciaRegistroRepository
                .findByFechaBetweenOrderByPersonaApellidosAscPersonaNombresAscFechaAsc(
                        periodo.atDay(1), periodo.atEndOfMonth());
        long personas = registros.stream().map(item -> item.getPersona().getId()).distinct().count();
        long inasistencias = registros.stream().filter(item -> item.getCodigo() == CodigoTareo.FI).count();
        long descansosMedicos = registros.stream().filter(item -> item.getCodigo() == CodigoTareo.DM).count();
        int tardanzas = registros.stream().mapToInt(AsistenciaRegistro::getMinutosTardanza).sum();
        int extras25 = registros.stream().mapToInt(AsistenciaRegistro::getMinutosExtras25).sum();
        int extras35 = registros.stream().mapToInt(AsistenciaRegistro::getMinutosExtras35).sum();
        BigDecimal horas = registros.stream()
                .map(AsistenciaRegistro::getHorasTrabajadas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardAsistenciaDTO(
                periodo.getYear(), periodo.getMonthValue(), registros.size(), personas,
                inasistencias, descansosMedicos, tardanzas, extras25, extras35, horas);
    }
}
