package com.convecsa.erp.asistencia.service.impl;

import com.convecsa.erp.asistencia.domain.EstadoAsistencia;
import com.convecsa.erp.asistencia.domain.CodigoTareo;
import com.convecsa.erp.asistencia.dto.AsistenciaConfigDTO;
import com.convecsa.erp.asistencia.dto.AsistenciaConfigRequest;
import com.convecsa.erp.asistencia.dto.AsistenciaConceptoVariableDTO;
import com.convecsa.erp.asistencia.dto.AsistenciaConceptoVariableRequest;
import com.convecsa.erp.asistencia.dto.AsistenciaFeriadoDTO;
import com.convecsa.erp.asistencia.dto.AsistenciaFeriadoRequest;
import com.convecsa.erp.asistencia.dto.AsistenciaFilaDTO;
import com.convecsa.erp.asistencia.dto.AsistenciaGenerarRequest;
import com.convecsa.erp.asistencia.dto.AsistenciaPlanillaDTO;
import com.convecsa.erp.asistencia.dto.AsistenciaPlanillaUpdateRequest;
import com.convecsa.erp.asistencia.dto.AsistenciaRegistroDTO;
import com.convecsa.erp.asistencia.dto.AsistenciaRegistroUpdateRequest;
import com.convecsa.erp.asistencia.entity.AsistenciaConfig;
import com.convecsa.erp.asistencia.entity.AsistenciaConceptoVariable;
import com.convecsa.erp.asistencia.entity.AsistenciaFeriado;
import com.convecsa.erp.asistencia.entity.AsistenciaRegistro;
import com.convecsa.erp.asistencia.repository.AsistenciaConfigRepository;
import com.convecsa.erp.asistencia.repository.AsistenciaConceptoVariableRepository;
import com.convecsa.erp.asistencia.repository.AsistenciaFeriadoRepository;
import com.convecsa.erp.asistencia.repository.AsistenciaRegistroRepository;
import com.convecsa.erp.asistencia.service.InterfazAsistenciaService;
import com.convecsa.erp.asistencia.support.CalendarioLaborableService;
import com.convecsa.erp.asistencia.support.HorasJornadaService;
import com.convecsa.erp.common.exception.AsistenciaFeriadoNoEncontradoException;
import com.convecsa.erp.common.exception.AsistenciaRegistroNoEncontradoException;
import com.convecsa.erp.common.exception.FeriadoDuplicadoException;
import com.convecsa.erp.contrato.catalogo.Jornada;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AsistenciaServiceImpl implements InterfazAsistenciaService {

    private final AsistenciaConfigRepository configRepository;
    private final AsistenciaConceptoVariableRepository conceptoVariableRepository;
    private final AsistenciaFeriadoRepository feriadoRepository;
    private final AsistenciaRegistroRepository registroRepository;
    private final ContratoRepository contratoRepository;
    private final CalendarioLaborableService calendarioLaborableService;
    private final HorasJornadaService horasJornadaService;

    @Override
    @Transactional(readOnly = true)
    public AsistenciaConfigDTO obtenerConfig() {
        return toConfigDto(cargarConfig());
    }

    @Override
    @Transactional
    public AsistenciaConfigDTO guardarConfig(AsistenciaConfigRequest request) {
        AsistenciaConfig config = cargarConfig();
        config.setDiasSemana(calendarioLaborableService.serializarDiasSemana(request.getDiasSemana()));
        return toConfigDto(configRepository.save(config));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaFeriadoDTO> listarFeriados(int anio) {
        validarAnio(anio);
        LocalDate inicio = LocalDate.of(anio, 1, 1);
        LocalDate fin = LocalDate.of(anio, 12, 31);
        return feriadoRepository.findByFechaBetweenOrderByFechaAsc(inicio, fin).stream()
                .map(AsistenciaServiceImpl::toFeriadoDto)
                .toList();
    }

    @Override
    @Transactional
    public AsistenciaFeriadoDTO crearFeriado(AsistenciaFeriadoRequest request) {
        if (feriadoRepository.existsByFecha(request.getFecha())) {
            throw new FeriadoDuplicadoException("Ya existe un feriado en esa fecha");
        }
        AsistenciaFeriado feriado = new AsistenciaFeriado();
        feriado.setFecha(request.getFecha());
        feriado.setNombre(request.getNombre().trim());
        feriado.setLaborable(Boolean.TRUE.equals(request.getLaborable()));
        return toFeriadoDto(feriadoRepository.save(feriado));
    }

    @Override
    @Transactional
    public void eliminarFeriado(Long id) {
        AsistenciaFeriado feriado = feriadoRepository.findById(id)
                .orElseThrow(() -> new AsistenciaFeriadoNoEncontradoException("No se encontró el feriado"));
        feriadoRepository.delete(feriado);
    }

    @Override
    @Transactional(readOnly = true)
    public AsistenciaPlanillaDTO obtenerPlanilla(int anio, int mes) {
        return armarPlanilla(anio, mes);
    }

    @Override
    @Transactional
    public AsistenciaPlanillaDTO generarPlanilla(AsistenciaGenerarRequest request) {
        int anio = request.getAnio();
        int mes = request.getMes();
        validarPeriodo(anio, mes);
        YearMonth yearMonth = YearMonth.of(anio, mes);
        LocalDate inicio = yearMonth.atDay(1);
        LocalDate fin = yearMonth.atEndOfMonth();
        AsistenciaConfig config = cargarConfig();
        List<Integer> diasSemana = calendarioLaborableService.parseDiasSemana(config.getDiasSemana());
        List<AsistenciaFeriado> feriadosMes = feriadoRepository.findByFechaBetweenOrderByFechaAsc(inicio, fin);
        Set<LocalDate> noLaborables = feriadosMes.stream()
                .filter(feriado -> !feriado.isLaborable())
                .map(AsistenciaFeriado::getFecha)
                .collect(Collectors.toSet());
        List<LocalDate> laborables = calendarioLaborableService.diasLaborables(anio, mes, diasSemana, noLaborables);
        if (laborables.isEmpty()) {
            throw new IllegalArgumentException("No hay días laborables en el periodo");
        }
        Map<Long, Contrato> porPersona = contratosVigentes(inicio, fin);
        if (porPersona.isEmpty()) {
            throw new IllegalArgumentException("No hay contratos activos vigentes en el periodo");
        }
        Map<Long, Set<LocalDate>> existentesPorPersona = registroRepository
                .findByFechaBetweenOrderByPersonaApellidosAscPersonaNombresAscFechaAsc(inicio, fin)
                .stream()
                .collect(Collectors.groupingBy(
                        registro -> registro.getPersona().getId(),
                        Collectors.mapping(AsistenciaRegistro::getFecha, Collectors.toSet())));
        int diasConfig = diasSemana.size();
        for (Contrato contrato : porPersona.values()) {
            Persona persona = contrato.getPersona();
            BigDecimal esperado = horasJornadaService.horasEsperadasDia(null, diasConfig);
            Set<LocalDate> existentes = existentesPorPersona.getOrDefault(persona.getId(), Set.of());
            List<LocalDate> faltan = CalendarioLaborableService.faltantes(
                    fechasVigentes(fechasDelMes(yearMonth), contrato), existentes);
            for (LocalDate fecha : faltan) {
                AsistenciaRegistro registro = new AsistenciaRegistro();
                registro.setPersona(persona);
                registro.setContrato(contrato);
                registro.setFecha(fecha);
                CodigoTareo codigo = codigoInicial(fecha, laborables, noLaborables);
                registro.setCodigo(codigo);
                registro.setHorasEsperadas(codigo == CodigoTareo.A ? esperado : BigDecimal.ZERO);
                registro.setHorasTrabajadas(codigo == CodigoTareo.A ? esperado : BigDecimal.ZERO);
                registro.setEstado(estadoPorCodigo(codigo));
                registroRepository.save(registro);
            }
        }
        return armarPlanilla(anio, mes);
    }

    @Override
    @Transactional
    public AsistenciaPlanillaDTO actualizarPlanilla(AsistenciaPlanillaUpdateRequest request) {
        Integer anio = null;
        Integer mes = null;
        for (AsistenciaRegistroUpdateRequest item : request.getItems()) {
            AsistenciaRegistro registro = registroRepository.findById(item.getId())
                    .orElseThrow(() -> new AsistenciaRegistroNoEncontradoException(
                            "No se encontró el registro de asistencia"));
            CodigoTareo codigo = item.getCodigo();
            EstadoAsistencia estado = estadoPorCodigo(codigo);
            BigDecimal horas = item.getHorasTrabajadas().setScale(2, RoundingMode.HALF_UP);
            if (bloqueaHoras(codigo)) {
                horas = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            } else if (codigo == CodigoTareo.A && horas.compareTo(BigDecimal.ZERO) == 0) {
                horas = registro.getHorasEsperadas();
            }
            registro.setEstado(estado);
            registro.setCodigo(codigo);
            registro.setHorasTrabajadas(horas);
            registro.setMinutosExtras25(minutosNoNegativos(item.getMinutosExtras25(), "Horas extras al 25%"));
            registro.setMinutosExtras35(minutosNoNegativos(item.getMinutosExtras35(), "Horas extras al 35%"));
            registro.setMinutosTardanza(minutosNoNegativos(item.getMinutosTardanza(), "Tardanzas"));
            registro.setObservacion(trimToNull(item.getObservacion()));
            anio = registro.getFecha().getYear();
            mes = registro.getFecha().getMonthValue();
        }
        if (anio == null) {
            throw new IllegalArgumentException("Debe indicar al menos un registro");
        }
        return armarPlanilla(anio, mes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaConceptoVariableDTO> listarConceptosVariables(int anio, int mes) {
        validarPeriodo(anio, mes);
        return conceptoVariableRepository.findByAnioAndMesOrderByPersonaApellidosAscPersonaNombresAscIdAsc(anio, mes)
                .stream().map(AsistenciaServiceImpl::toConceptoVariableDto).toList();
    }

    @Override
    @Transactional
    public AsistenciaConceptoVariableDTO registrarConceptoVariable(
            int anio, int mes, AsistenciaConceptoVariableRequest request) {
        validarPeriodo(anio, mes);
        Contrato contrato = contratoRepository.findById(request.getContratoId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el contrato"));
        if (!contrato.getPersona().getId().equals(request.getPersonaId())) {
            throw new IllegalArgumentException("El contrato no corresponde a la persona indicada");
        }
        AsistenciaConceptoVariable concepto = new AsistenciaConceptoVariable();
        concepto.setPersona(contrato.getPersona());
        concepto.setContrato(contrato);
        concepto.setAnio(anio);
        concepto.setMes(mes);
        concepto.setTipo(request.getTipo());
        concepto.setMonto(request.getMonto().setScale(2, RoundingMode.HALF_UP));
        concepto.setDetalle(trimToNull(request.getDetalle()));
        return toConceptoVariableDto(conceptoVariableRepository.save(concepto));
    }

    @Override
    @Transactional
    public void eliminarConceptoVariable(Long id) {
        if (!conceptoVariableRepository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró el concepto variable");
        }
        conceptoVariableRepository.deleteById(id);
    }

    private AsistenciaPlanillaDTO armarPlanilla(int anio, int mes) {
        validarPeriodo(anio, mes);
        YearMonth yearMonth = YearMonth.of(anio, mes);
        LocalDate inicio = yearMonth.atDay(1);
        LocalDate fin = yearMonth.atEndOfMonth();
        AsistenciaConfig config = cargarConfig();
        List<Integer> diasSemana = calendarioLaborableService.parseDiasSemana(config.getDiasSemana());
        List<AsistenciaFeriado> feriadosMes = feriadoRepository.findByFechaBetweenOrderByFechaAsc(inicio, fin);
        Set<LocalDate> noLaborables = feriadosMes.stream()
                .filter(feriado -> !feriado.isLaborable())
                .map(AsistenciaFeriado::getFecha)
                .collect(Collectors.toSet());
        List<LocalDate> laborables = calendarioLaborableService.diasLaborables(anio, mes, diasSemana, noLaborables);
        Map<Long, Contrato> porPersona = contratosVigentes(inicio, fin);
        List<AsistenciaRegistro> registros = registroRepository
                .findByFechaBetweenOrderByPersonaApellidosAscPersonaNombresAscFechaAsc(inicio, fin);
        Map<Long, List<AsistenciaRegistro>> porPersonaRegistros = new LinkedHashMap<>();
        for (AsistenciaRegistro registro : registros) {
            porPersonaRegistros
                    .computeIfAbsent(registro.getPersona().getId(), key -> new ArrayList<>())
                    .add(registro);
        }
        int diasConfig = diasSemana.size();
        List<AsistenciaFilaDTO> filas = new ArrayList<>();
        List<Contrato> contratos = new ArrayList<>(porPersona.values());
        contratos.sort(Comparator
                .comparing((Contrato contrato) -> contrato.getPersona().getApellidos(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(contrato -> contrato.getPersona().getNombres(), String.CASE_INSENSITIVE_ORDER));
        for (Contrato contrato : contratos) {
            Persona persona = contrato.getPersona();
            String jornada = contrato.metadata("jornada");
            filas.add(new AsistenciaFilaDTO(
                    persona.getId(),
                    persona.getNombres(),
                    persona.getApellidos(),
                    persona.getNumeroDocumento(),
                    contrato.getId(),
                    null,
                    jornada,
                    horasJornadaService.horasEsperadasDia(null, diasConfig),
                    porPersonaRegistros.getOrDefault(persona.getId(), List.of()).stream()
                            .map(AsistenciaServiceImpl::toRegistroDto)
                            .toList()));
        }
        return new AsistenciaPlanillaDTO(
                anio,
                mes,
                toConfigDto(config),
                feriadosMes.stream().map(AsistenciaServiceImpl::toFeriadoDto).toList(),
                fechasDelMes(yearMonth),
                !registros.isEmpty(),
                filas);
    }

    private Map<Long, Contrato> contratosVigentes(LocalDate inicio, LocalDate fin) {
        List<Contrato> vigentes = contratoRepository.findVigentesEnRango(inicio, fin);
        Map<Long, Contrato> porPersona = new LinkedHashMap<>();
        for (Contrato contrato : vigentes) {
            porPersona.putIfAbsent(contrato.getPersona().getId(), contrato);
        }
        return porPersona;
    }

    private static List<LocalDate> fechasVigentes(List<LocalDate> laborables, Contrato contrato) {
        List<LocalDate> fechas = new ArrayList<>();
        for (LocalDate fecha : laborables) {
            if (contrato.getFechaInicio() != null && fecha.isBefore(contrato.getFechaInicio())) {
                continue;
            }
            if (contrato.getFechaFin() != null && fecha.isAfter(contrato.getFechaFin())) {
                continue;
            }
            fechas.add(fecha);
        }
        return fechas;
    }

    private AsistenciaConfig cargarConfig() {
        return configRepository.findById(AsistenciaConfig.ID_UNICO).orElseGet(() -> {
            AsistenciaConfig config = new AsistenciaConfig();
            config.setId(AsistenciaConfig.ID_UNICO);
            config.setDiasSemana("1,2,3,4,5");
            return configRepository.save(config);
        });
    }

    private AsistenciaConfigDTO toConfigDto(AsistenciaConfig config) {
        return new AsistenciaConfigDTO(calendarioLaborableService.parseDiasSemana(config.getDiasSemana()));
    }

    private static AsistenciaFeriadoDTO toFeriadoDto(AsistenciaFeriado feriado) {
        return new AsistenciaFeriadoDTO(
                feriado.getId(),
                feriado.getFecha(),
                feriado.getNombre(),
                feriado.isLaborable());
    }

    private static AsistenciaRegistroDTO toRegistroDto(AsistenciaRegistro registro) {
        return new AsistenciaRegistroDTO(
                registro.getId(),
                registro.getFecha(),
                registro.getHorasEsperadas(),
                registro.getHorasTrabajadas(),
                registro.getEstado(),
                registro.getCodigo(),
                registro.getMinutosExtras25(),
                registro.getMinutosExtras35(),
                registro.getMinutosTardanza(),
                registro.getObservacion());
    }

    private static AsistenciaConceptoVariableDTO toConceptoVariableDto(AsistenciaConceptoVariable concepto) {
        return new AsistenciaConceptoVariableDTO(
                concepto.getId(),
                concepto.getPersona().getId(),
                concepto.getContrato().getId(),
                concepto.getTipo(),
                concepto.getMonto(),
                concepto.getDetalle());
    }

    private static List<LocalDate> fechasDelMes(YearMonth yearMonth) {
        List<LocalDate> fechas = new ArrayList<>();
        for (int dia = 1; dia <= yearMonth.lengthOfMonth(); dia++) {
            fechas.add(yearMonth.atDay(dia));
        }
        return fechas;
    }

    private static CodigoTareo codigoInicial(
            LocalDate fecha, List<LocalDate> laborables, Set<LocalDate> feriadosNoLaborables) {
        if (feriadosNoLaborables.contains(fecha)) {
            return CodigoTareo.F;
        }
        return laborables.contains(fecha) ? CodigoTareo.A : CodigoTareo.D;
    }

    private static EstadoAsistencia estadoPorCodigo(CodigoTareo codigo) {
        return switch (codigo) {
            case A, TF, TD -> EstadoAsistencia.ASISTIO;
            case D, F -> EstadoAsistencia.DESCANSO;
            case FI -> EstadoAsistencia.FALTA;
            default -> EstadoAsistencia.PERMISO;
        };
    }

    private static boolean bloqueaHoras(CodigoTareo codigo) {
        return switch (codigo) {
            case D, F, DM, SIT, MAT_PRE, MAT_POST, VAC, VAC_COMP, LCG, LSG, PAT, FI -> true;
            default -> false;
        };
    }

    private static int minutosNoNegativos(Integer valor, String etiqueta) {
        int minutos = valor == null ? 0 : valor;
        if (minutos < 0 || minutos > 1_440) {
            throw new IllegalArgumentException(etiqueta + " deben estar entre 0 y 1440 minutos");
        }
        return minutos;
    }

    private static void validarPeriodo(int anio, int mes) {
        validarAnio(anio);
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12");
        }
    }

    private static void validarAnio(int anio) {
        if (anio < 2000 || anio > 2100) {
            throw new IllegalArgumentException("El año es inválido");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
