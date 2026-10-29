package com.convecsa.erp.construccion.service.impl;

import com.convecsa.erp.common.exception.BoletaCcNoEncontradaException;
import com.convecsa.erp.common.exception.PeriodoCcDuplicadoException;
import com.convecsa.erp.common.exception.PeriodoCcNoEncontradoException;
import com.convecsa.erp.common.exception.ProyectoCodigoDuplicadoException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.domain.CategoriaObrera;
import com.convecsa.erp.construccion.domain.CodigoTareoCc;
import com.convecsa.erp.construccion.domain.EstadoPeriodoCc;
import com.convecsa.erp.construccion.domain.SemestreGrati;
import com.convecsa.erp.construccion.dto.BoletaCcConceptoDTO;
import com.convecsa.erp.construccion.dto.BoletaCcDTO;
import com.convecsa.erp.construccion.dto.BoletaCcResumenDTO;
import com.convecsa.erp.construccion.dto.ConstruccionCatalogosDTO;
import com.convecsa.erp.construccion.dto.PeriodoCcDTO;
import com.convecsa.erp.construccion.dto.PeriodoCcRequest;
import com.convecsa.erp.construccion.dto.PersonalObraDTO;
import com.convecsa.erp.construccion.dto.ProyectoDTO;
import com.convecsa.erp.construccion.dto.ProyectoRequest;
import com.convecsa.erp.construccion.dto.TareoCcDiaDTO;
import com.convecsa.erp.construccion.dto.TareoCcGrillaDTO;
import com.convecsa.erp.construccion.dto.TareoCcGuardarRequest;
import com.convecsa.erp.construccion.entity.BoletaCc;
import com.convecsa.erp.construccion.entity.BoletaCcConcepto;
import com.convecsa.erp.construccion.entity.CcCategoriaJornal;
import com.convecsa.erp.construccion.entity.CcEspecialidadBae;
import com.convecsa.erp.construccion.entity.CcParametroAnual;
import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import com.convecsa.erp.construccion.repository.BoletaCcRepository;
import com.convecsa.erp.construccion.repository.CcParametroAnualRepository;
import com.convecsa.erp.construccion.repository.ContratoCcRepository;
import com.convecsa.erp.construccion.repository.PeriodoCcRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.TareoCcDiaRepository;
import com.convecsa.erp.construccion.service.InterfazConstruccionService;
import com.convecsa.erp.construccion.support.BoletaCcPlantillaService;
import com.convecsa.erp.construccion.support.CalculoNominaCcService;
import com.convecsa.erp.construccion.support.CcLiquidacionInput;
import com.convecsa.erp.construccion.support.CcLiquidacionResult;
import com.convecsa.erp.construccion.support.CcParametros;
import com.convecsa.erp.construccion.support.RentaQuintaProyeccionService;
import com.convecsa.erp.construccion.support.UitObraAlerta;
import com.convecsa.erp.contrato.dto.CatalogoItemDTO;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.nomina.catalogo.Afp;
import com.convecsa.erp.nomina.domain.TipoComisionAfp;
import com.convecsa.erp.nomina.entity.AfpTasa;
import com.convecsa.erp.nomina.entity.NominaDocumentoConfiguracion;
import com.convecsa.erp.nomina.repository.AfpRepository;
import com.convecsa.erp.nomina.repository.AfpTasaRepository;
import com.convecsa.erp.nomina.repository.NominaDocumentoConfiguracionRepository;
import com.convecsa.erp.nomina.support.BoletaPdfService;
import com.convecsa.erp.persona.entity.Persona;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConstruccionServiceImpl implements InterfazConstruccionService {

    private static final BigDecimal CUOTA_SINDICAL_DEFAULT = new BigDecimal("40.00");

    private final ProyectoRepository proyectoRepository;
    private final CcParametroAnualRepository parametroRepository;
    private final ContratoCcRepository contratoCcRepository;
    private final PeriodoCcRepository periodoCcRepository;
    private final TareoCcDiaRepository tareoCcDiaRepository;
    private final BoletaCcRepository boletaCcRepository;
    private final CalculoNominaCcService calculoNominaCcService;
    private final RentaQuintaProyeccionService rentaQuintaProyeccionService;
    private final BoletaCcPlantillaService boletaCcPlantillaService;
    private final BoletaPdfService boletaPdfService;
    private final AfpRepository afpRepository;
    private final AfpTasaRepository afpTasaRepository;
    private final NominaDocumentoConfiguracionRepository documentoConfiguracionRepository;

    @Override
    @Transactional(readOnly = true)
    public ConstruccionCatalogosDTO catalogos() {
        CcParametroAnual parametros = parametrosVigentes(LocalDate.now().getYear());
        List<CatalogoItemDTO> categorias = parametros.getCategorias().stream()
                .map(item -> new CatalogoItemDTO(
                        item.getId(),
                        item.getCodigo().name(),
                        labelCategoria(item.getCodigo())))
                .toList();
        List<CatalogoItemDTO> especialidades = parametros.getEspecialidades().stream()
                .map(item -> new CatalogoItemDTO(item.getId(), item.getCodigo(), item.getLabel()))
                .toList();
        return new ConstruccionCatalogosDTO(
                categorias,
                especialidades,
                listarProyectos(true),
                parametros.getAnio(),
                parametros.getAnio(),
                parametros.getUit(),
                UitObraAlerta.umbral(parametros.getUit()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoDTO> listarProyectos(Boolean activo) {
        List<Proyecto> proyectos = Boolean.TRUE.equals(activo)
                ? proyectoRepository.findByActivoTrueOrderByCodigoAsc()
                : proyectoRepository.findAllByOrderByCodigoAsc();
        CcParametroAnual parametros = parametrosVigentes(LocalDate.now().getYear());
        return proyectos.stream().map(item -> toProyectoDto(item, parametros)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProyectoDTO obtenerProyecto(Long id) {
        return toProyectoDto(buscarProyecto(id), parametrosVigentes(LocalDate.now().getYear()));
    }

    @Override
    @Transactional
    public ProyectoDTO crearProyecto(ProyectoRequest request) {
        String codigo = request.getCodigo().trim().toUpperCase();
        if (proyectoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ProyectoCodigoDuplicadoException("Ya existe un proyecto con el código " + codigo);
        }
        Proyecto proyecto = new Proyecto();
        proyecto.setCodigo(codigo);
        proyecto.setNombre(request.getNombre().trim());
        proyecto.setUbicacion(blankToNull(request.getUbicacion()));
        proyecto.setCentroCosto(blankToNull(request.getCentroCosto()));
        proyecto.setValorObra(request.getValorObra());
        proyecto.setActivo(request.getActivo() == null || request.getActivo());
        return toProyectoDto(proyectoRepository.save(proyecto), parametrosVigentes(LocalDate.now().getYear()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalObraDTO> listarPersonal(Long proyectoId) {
        buscarProyecto(proyectoId);
        return contratoCcRepository.findActivosByProyectoId(proyectoId).stream()
                .map(ConstruccionServiceImpl::toPersonalDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeriodoCcDTO> listarPeriodos(Long proyectoId) {
        buscarProyecto(proyectoId);
        return periodoCcRepository.findByProyectoIdOrderByFechaDesdeDesc(proyectoId).stream()
                .map(periodo -> toPeriodoDto(periodo, boletaCcRepository.findByPeriodoIdOrderByIdAsc(periodo.getId()).size()))
                .toList();
    }

    @Override
    @Transactional
    public PeriodoCcDTO crearPeriodo(Long proyectoId, PeriodoCcRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        if (periodoCcRepository.existsByProyectoIdAndFechaDesdeAndFechaHasta(
                proyectoId, request.getFechaDesde(), request.getFechaHasta())) {
            throw new PeriodoCcDuplicadoException("Ya existe un periodo con ese rango de fechas");
        }
        PeriodoCc periodo = new PeriodoCc();
        periodo.setProyecto(proyecto);
        periodo.setFechaDesde(request.getFechaDesde());
        periodo.setFechaHasta(request.getFechaHasta());
        periodo.setNroSemana(request.getNroSemana() == null
                ? request.getFechaDesde().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
                : request.getNroSemana());
        periodo.setEstado(EstadoPeriodoCc.BORRADOR);
        PeriodoCc saved = periodoCcRepository.save(periodo);
        asegurarTareoInicial(saved);
        return toPeriodoDto(saved, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public PeriodoCcDTO obtenerPeriodo(Long proyectoId, Long periodoId) {
        PeriodoCc periodo = buscarPeriodo(proyectoId, periodoId);
        return toPeriodoDto(periodo, boletaCcRepository.findByPeriodoIdOrderByIdAsc(periodoId).size());
    }

    @Override
    @Transactional
    public PeriodoCcDTO cerrarPeriodo(Long proyectoId, Long periodoId) {
        PeriodoCc periodo = buscarPeriodo(proyectoId, periodoId);
        if (periodo.getEstado() == EstadoPeriodoCc.CERRADO) {
            return toPeriodoDto(periodo, boletaCcRepository.findByPeriodoIdOrderByIdAsc(periodoId).size());
        }
        if (boletaCcRepository.findByPeriodoIdOrderByIdAsc(periodoId).isEmpty()) {
            throw new IllegalArgumentException("Liquide la planilla antes de cerrar el periodo");
        }
        periodo.setEstado(EstadoPeriodoCc.CERRADO);
        periodo.setClosedAt(Instant.now());
        return toPeriodoDto(periodoCcRepository.save(periodo), boletaCcRepository.findByPeriodoIdOrderByIdAsc(periodoId).size());
    }

    @Override
    @Transactional
    public TareoCcGrillaDTO obtenerTareo(Long proyectoId, Long periodoId) {
        PeriodoCc periodo = buscarPeriodo(proyectoId, periodoId);
        asegurarTareoInicial(periodo);
        return armarGrilla(periodo);
    }

    @Override
    @Transactional
    public TareoCcGrillaDTO guardarTareo(Long proyectoId, Long periodoId, TareoCcGuardarRequest request) {
        PeriodoCc periodo = buscarPeriodo(proyectoId, periodoId);
        exigirBorrador(periodo);
        Map<String, TareoCcDia> existentes = indexarTareo(tareoCcDiaRepository.findByPeriodoIdOrderByPersonaIdAscFechaAsc(periodoId));
        Map<Long, ContratoCc> personal = personalPorPersona(proyectoId);
        for (TareoCcDiaDTO fila : request.getDias()) {
            if (fila.getPersonaId() == null || fila.getFecha() == null) {
                continue;
            }
            if (fila.getFecha().isBefore(periodo.getFechaDesde()) || fila.getFecha().isAfter(periodo.getFechaHasta())) {
                throw new IllegalArgumentException("Hay días fuera del rango del periodo");
            }
            ContratoCc contratoCc = personal.get(fila.getPersonaId());
            if (contratoCc == null) {
                throw new IllegalArgumentException("La persona no está asignada a esta obra");
            }
            TareoCcDia dia = existentes.get(clave(fila.getPersonaId(), fila.getFecha()));
            if (dia == null) {
                dia = new TareoCcDia();
                dia.setPeriodo(periodo);
                dia.setPersona(contratoCc.getContrato().getPersona());
                dia.setFecha(fila.getFecha());
            }
            aplicarFila(dia, fila);
            tareoCcDiaRepository.save(dia);
        }
        return armarGrilla(periodo);
    }

    @Override
    @Transactional
    public List<BoletaCcResumenDTO> liquidar(Long proyectoId, Long periodoId) {
        PeriodoCc periodo = buscarPeriodo(proyectoId, periodoId);
        exigirBorrador(periodo);
        asegurarTareoInicial(periodo);
        CcParametros parametros = toParametros(parametrosVigentes(periodo.getFechaHasta().getYear()));
        Map<Long, ContratoCc> personal = personalPorPersona(proyectoId);
        Map<Long, List<TareoCcDia>> porPersona = tareoCcDiaRepository
                .findByPeriodoIdOrderByPersonaIdAscFechaAsc(periodoId)
                .stream()
                .collect(Collectors.groupingBy(dia -> dia.getPersona().getId(), LinkedHashMap::new, Collectors.toList()));

        List<BoletaCcResumenDTO> boletas = new ArrayList<>();
        for (Map.Entry<Long, ContratoCc> entry : personal.entrySet()) {
            List<TareoCcDia> dias = porPersona.getOrDefault(entry.getKey(), List.of());
            if (dias.isEmpty()) {
                continue;
            }
            ContratoCc contratoCc = entry.getValue();
            CcLiquidacionInput input = agregarTareo(dias, contratoCc, periodo);
            CcLiquidacionResult preliminar = calculoNominaCcService.calcular(input, parametros);
            BigDecimal rentaQuinta = rentaQuintaProyeccionService.proyectar(
                    contratoCc.getContrato().getPersona().getNumeroDocumento(),
                    periodo.getFechaHasta(),
                    preliminar.getTotalAfecto());
            CcLiquidacionResult resultado = rentaQuinta.compareTo(BigDecimal.ZERO) == 0
                    ? preliminar
                    : calculoNominaCcService.calcular(input.toBuilder().rentaQuinta(rentaQuinta).build(), parametros);
            BoletaCc boleta = boletaCcRepository.findByPeriodoIdAndPersonaId(periodoId, entry.getKey())
                    .orElseGet(BoletaCc::new);
            boleta.setPeriodo(periodo);
            boleta.setPersona(contratoCc.getContrato().getPersona());
            boleta.setContrato(contratoCc.getContrato());
            boleta.setTotalHaberes(resultado.getTotalHaberes());
            boleta.setTotalDescuentos(resultado.getTotalDescuentos());
            boleta.setTotalAportesEmpleador(resultado.getTotalAportes());
            boleta.setTotalAfecto(resultado.getTotalAfecto());
            boleta.setNeto(resultado.getNeto());
            boleta.setTarifaDia(resultado.getTarifaDia());
            boleta.setTarifaHora(resultado.getTarifaHora());
            List<BoletaCcConcepto> conceptos = new ArrayList<>();
            resultado.getConceptos().forEach(item -> {
                BoletaCcConcepto concepto = new BoletaCcConcepto();
                concepto.setCategoria(item.categoria());
                concepto.setCodigo(item.codigo());
                concepto.setLabel(item.label());
                concepto.setMonto(item.monto());
                conceptos.add(concepto);
            });
            boleta.reemplazarConceptos(conceptos);
            boletas.add(toResumen(boletaCcRepository.save(boleta), contratoCc));
        }
        return boletas;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoletaCcResumenDTO> listarBoletas(Long proyectoId, Long periodoId) {
        buscarPeriodo(proyectoId, periodoId);
        Map<Long, ContratoCc> personal = personalPorPersona(proyectoId);
        return boletaCcRepository.findByPeriodoIdOrderByIdAsc(periodoId).stream()
                .map(boleta -> toResumen(boleta, personal.get(boleta.getPersona().getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BoletaCcDTO obtenerBoleta(Long proyectoId, Long periodoId, Long boletaId) {
        return toBoletaDto(buscarBoleta(proyectoId, periodoId, boletaId));
    }

    @Override
    @Transactional(readOnly = true)
    public String documentoBoletaHtml(Long proyectoId, Long periodoId, Long boletaId) {
        BoletaCc boleta = buscarBoleta(proyectoId, periodoId, boletaId);
        boleta.getPeriodo().getProyecto().getNombre();
        boleta.getPersona().getNombres();
        boleta.getContrato().getId();
        boleta.getConceptos().size();
        NominaDocumentoConfiguracion configuracion = documentoConfiguracionRepository
                .findById(NominaDocumentoConfiguracion.ID_UNICO)
                .orElse(null);
        List<TareoCcDia> dias = tareoCcDiaRepository.findByPeriodoIdAndPersonaIdOrderByFechaAsc(
                periodoId, boleta.getPersona().getId());
        BigDecimal[] tarifas = tarifasDe(boleta);
        return boletaCcPlantillaService.render(boleta, configuracion, dias, tarifas[0], tarifas[1]);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] documentoBoletaPdf(Long proyectoId, Long periodoId, Long boletaId) {
        return boletaPdfService.convertir(documentoBoletaHtml(proyectoId, periodoId, boletaId));
    }

    private void asegurarTareoInicial(PeriodoCc periodo) {
        List<ContratoCc> personal = contratoCcRepository.findActivosByProyectoId(periodo.getProyecto().getId());
        if (personal.isEmpty()) {
            return;
        }
        CcParametroAnual parametros = parametrosVigentes(periodo.getFechaHasta().getYear());
        Map<String, TareoCcDia> existentes = indexarTareo(
                tareoCcDiaRepository.findByPeriodoIdOrderByPersonaIdAscFechaAsc(periodo.getId()));
        List<LocalDate> fechas = fechasDe(periodo);
        for (ContratoCc contratoCc : personal) {
            Persona persona = contratoCc.getContrato().getPersona();
            for (LocalDate fecha : fechas) {
                if (existentes.containsKey(clave(persona.getId(), fecha))) {
                    continue;
                }
                TareoCcDia dia = nuevoDiaDefault(periodo, persona, fecha, contratoCc, parametros);
                tareoCcDiaRepository.save(dia);
            }
        }
    }

    private TareoCcDia nuevoDiaDefault(
            PeriodoCc periodo,
            Persona persona,
            LocalDate fecha,
            ContratoCc contratoCc,
            CcParametroAnual parametros) {
        TareoCcDia dia = new TareoCcDia();
        dia.setPeriodo(periodo);
        dia.setPersona(persona);
        dia.setFecha(fecha);
        DayOfWeek dow = fecha.getDayOfWeek();
        if (dow == DayOfWeek.SUNDAY) {
            dia.setCodigo(CodigoTareoCc.DSC);
            dia.setHorasOrd(BigDecimal.ZERO);
            dia.setMovilidad(false);
            dia.setBae(false);
        } else {
            dia.setCodigo(CodigoTareoCc.LAB);
            dia.setHorasOrd(dow == DayOfWeek.SATURDAY ? parametros.getJornadaSabado() : parametros.getJornadaLv());
            dia.setMovilidad(true);
            dia.setBae(contratoCc.getEspecialidadBae() != null && !contratoCc.getEspecialidadBae().isBlank());
        }
        return dia;
    }

    private TareoCcGrillaDTO armarGrilla(PeriodoCc periodo) {
        List<PersonalObraDTO> personal = listarPersonal(periodo.getProyecto().getId());
        List<TareoCcDiaDTO> dias = tareoCcDiaRepository.findByPeriodoIdOrderByPersonaIdAscFechaAsc(periodo.getId())
                .stream()
                .map(ConstruccionServiceImpl::toDiaDto)
                .toList();
        CcParametroAnual parametros = parametrosVigentes(periodo.getFechaHasta().getYear());
        return TareoCcGrillaDTO.builder()
                .periodo(toPeriodoDto(periodo, boletaCcRepository.findByPeriodoIdOrderByIdAsc(periodo.getId()).size()))
                .fechas(fechasDe(periodo))
                .personal(personal)
                .dias(dias)
                .jornadaLv(parametros.getJornadaLv())
                .jornadaSabado(parametros.getJornadaSabado())
                .build();
    }

    private CcLiquidacionInput agregarTareo(List<TareoCcDia> dias, ContratoCc contratoCc, PeriodoCc periodo) {
        BigDecimal horasOrd = BigDecimal.ZERO;
        BigDecimal he60 = BigDecimal.ZERO;
        BigDecimal he100 = BigDecimal.ZERO;
        int faltas = 0;
        int feriados = 0;
        int feriadoTrabajado = 0;
        int descansoTrabajado = 0;
        int diasMovilidad = 0;
        int diasMovilidadFeriado = 0;
        int diasBae = 0;
        int pisos = 0;
        int diasAltura = 0;
        int diasAltitud = 0;
        int diasCotaCero = 0;
        int diasAltasTemp = 0;
        int diasContactoAgua = 0;
        int diasAguasServidas = 0;
        int diasNocturno = 0;
        int diasLab = 0;
        int diasDm = 0;
        int diasLic = 0;

        for (TareoCcDia dia : dias) {
            he60 = he60.add(nz(dia.getHe60()));
            he100 = he100.add(nz(dia.getHe100()));
            pisos = Math.max(pisos, dia.getPisos());
            CodigoTareoCc codigo = dia.getCodigo() == null ? CodigoTareoCc.LAB : dia.getCodigo();
            BigDecimal horas = nz(dia.getHorasOrd());
            boolean laborable = codigo == CodigoTareoCc.LAB && horas.compareTo(BigDecimal.ZERO) > 0;
            if (dia.getFecha().getDayOfWeek() != DayOfWeek.SUNDAY && codigo == CodigoTareoCc.LAB) {
                horasOrd = horasOrd.add(horas);
            }
            switch (codigo) {
                case F -> faltas++;
                case FE -> {
                    if (horas.compareTo(BigDecimal.ZERO) > 0) {
                        feriadoTrabajado += horas.intValue();
                    } else {
                        feriados++;
                    }
                    if (dia.isMovilidad()) {
                        diasMovilidadFeriado++;
                    }
                }
                case DSC -> {
                    if (horas.compareTo(BigDecimal.ZERO) > 0) {
                        descansoTrabajado += horas.intValue();
                    }
                }
                case DM -> diasDm++;
                case LIC -> diasLic++;
                case LAB -> {
                    if (laborable) {
                        diasLab++;
                    }
                }
            }
            if (laborable || codigo == CodigoTareoCc.FE) {
                if (dia.isMovilidad() && codigo != CodigoTareoCc.FE) {
                    diasMovilidad++;
                }
                if (dia.isBae()) {
                    diasBae++;
                }
                if (dia.isAltura()) {
                    diasAltura++;
                }
                if (dia.isAltitud()) {
                    diasAltitud++;
                }
                if (dia.isCotaCero()) {
                    diasCotaCero++;
                }
                if (dia.isAltasTemp()) {
                    diasAltasTemp++;
                }
                if (dia.isAgua()) {
                    diasContactoAgua++;
                }
                if (dia.isAguasServidas()) {
                    diasAguasServidas++;
                }
                if (dia.isNocturno()) {
                    diasNocturno++;
                }
            }
        }

        Persona persona = contratoCc.getContrato().getPersona();
        boolean onp = esOnp(persona);
        BigDecimal comision = BigDecimal.ZERO;
        BigDecimal prima = BigDecimal.ZERO;
        BigDecimal aporte = BigDecimal.ZERO;
        if (!onp) {
            AfpTasa tasa = tasaAfp(persona);
            comision = tasa.getComision();
            prima = tasa.getPrimaSeguro();
            aporte = tasa.getAporteObligatorio();
        }
        BigDecimal cuota = contratoCc.isAfiliadoSindical()
                ? (contratoCc.getCuotaSindical() == null || contratoCc.getCuotaSindical().compareTo(BigDecimal.ZERO) == 0
                        ? CUOTA_SINDICAL_DEFAULT
                        : contratoCc.getCuotaSindical())
                : BigDecimal.ZERO;

        return CcLiquidacionInput.builder()
                .categoria(contratoCc.getCategoria())
                .especialidadBae(contratoCc.getEspecialidadBae())
                .hijosEscolaridad(contratoCc.getHijosEscolaridad())
                .onp(onp)
                .comisionAfp(comision)
                .primaAfp(prima)
                .aporteAfp(aporte)
                .cuotaSindical(cuota)
                .rentaQuinta(BigDecimal.ZERO)
                .horasOrdinarias(horasOrd)
                .horasDominical(horasOrd.divide(new BigDecimal("6"), 4, RoundingMode.HALF_UP))
                .he60(he60)
                .he100(he100)
                .diasEfectivos(diasLab)
                .faltas(faltas)
                .feriados(feriados)
                .feriadoTrabajado(feriadoTrabajado)
                .descansoTrabajado(descansoTrabajado)
                .diasMovilidad(diasMovilidad)
                .diasMovilidadFeriado(diasMovilidadFeriado)
                .diasBae(diasBae)
                .pisos(pisos)
                .diasAltura(diasAltura)
                .diasAltitud(diasAltitud)
                .diasCotaCero(diasCotaCero)
                .diasAltasTemp(diasAltasTemp)
                .diasContactoAgua(diasContactoAgua)
                .diasAguasServidas(diasAguasServidas)
                .diasNocturno(diasNocturno)
                .diasValidacionGrati(diasLab)
                .semestreGrati(SemestreGrati.de(periodo.getFechaHasta()))
                .diasDescansoMedico(diasDm)
                .diasLicencia(diasLic)
                .build();
    }

    private AfpTasa tasaAfp(Persona persona) {
        String codigo = persona.getPension();
        Afp afp = afpRepository.findByCodigo(codigo == null ? "" : codigo.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la AFP " + codigo));
        TipoComisionAfp tipo = persona.getTipoComisionAfp() == null ? TipoComisionAfp.FLUJO : persona.getTipoComisionAfp();
        return afpTasaRepository.findByAfpIdAndTipoComisionAndActivoTrue(afp.getId(), tipo)
                .orElseThrow(() -> new IllegalArgumentException("No hay tasas activas para la AFP " + codigo));
    }

    private static boolean esOnp(Persona persona) {
        String pension = persona.getPension();
        return pension == null || pension.isBlank() || "ONP".equalsIgnoreCase(pension);
    }

    private Map<Long, ContratoCc> personalPorPersona(Long proyectoId) {
        Map<Long, ContratoCc> mapa = new LinkedHashMap<>();
        for (ContratoCc contratoCc : contratoCcRepository.findActivosByProyectoId(proyectoId)) {
            mapa.put(contratoCc.getContrato().getPersona().getId(), contratoCc);
        }
        return mapa;
    }

    private CcParametroAnual parametrosVigentes(int anio) {
        return parametroRepository.findByAnio(anio)
                .or(() -> parametroRepository.findFirstByOrderByAnioDesc())
                .orElseThrow(() -> new IllegalArgumentException("No hay parámetros de construcción civil"));
    }

    private static CcParametros toParametros(CcParametroAnual p) {
        Map<CategoriaObrera, BigDecimal> jornales = new EnumMap<>(CategoriaObrera.class);
        Map<CategoriaObrera, BigDecimal> buc = new EnumMap<>(CategoriaObrera.class);
        for (CcCategoriaJornal categoria : p.getCategorias()) {
            jornales.put(categoria.getCodigo(), categoria.getJornal());
            buc.put(categoria.getCodigo(), categoria.getBuc());
        }
        Map<String, BigDecimal> bae = new HashMap<>();
        for (CcEspecialidadBae especialidad : p.getEspecialidades()) {
            bae.put(especialidad.getCodigo(), especialidad.getTasa());
        }
        return CcParametros.builder()
                .horasDia(p.getHorasDia())
                .movilidadDia(p.getMovilidadDia())
                .movilidadFeriado(p.getMovilidadFeriado())
                .conafovicer(p.getConafovicer())
                .fondoCapacitacion(p.getFondoCapacitacion())
                .essalud(p.getEssalud())
                .vacaciones(p.getVacaciones())
                .cts(p.getCts())
                .he60Factor(p.getHe60Factor())
                .he100Factor(p.getHe100Factor())
                .alturaTramo(p.getAlturaTramo())
                .altitudDia(p.getAltitudDia())
                .cotaCeroDia(p.getCotaCeroDia())
                .altasTempDia(p.getAltasTempDia())
                .nocturno(p.getNocturno())
                .contactoAgua(p.getContactoAgua())
                .aguasServidas(p.getAguasServidas())
                .bonifLey29351(p.getBonifLey29351())
                .gratiJornales(p.getGratiJornales())
                .gratiMesesFp(p.getGratiMesesFp())
                .gratiMesesNav(p.getGratiMesesNav())
                .escolaridadJornales(p.getEscolaridadJornales())
                .jornales(jornales)
                .buc(buc)
                .bae(bae)
                .build();
    }

    private Proyecto buscarProyecto(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private PeriodoCc buscarPeriodo(Long proyectoId, Long periodoId) {
        return periodoCcRepository.findByIdAndProyectoId(periodoId, proyectoId)
                .orElseThrow(() -> new PeriodoCcNoEncontradoException("No se encontró el periodo"));
    }

    private BoletaCc buscarBoleta(Long proyectoId, Long periodoId, Long boletaId) {
        buscarPeriodo(proyectoId, periodoId);
        return boletaCcRepository.findByIdAndPeriodoId(boletaId, periodoId)
                .orElseThrow(() -> new BoletaCcNoEncontradaException("No se encontró la boleta"));
    }

    private static void exigirBorrador(PeriodoCc periodo) {
        if (periodo.getEstado() == EstadoPeriodoCc.CERRADO) {
            throw new IllegalArgumentException("El periodo está cerrado");
        }
    }

    private static void aplicarFila(TareoCcDia dia, TareoCcDiaDTO fila) {
        dia.setHorasOrd(nz(fila.getHorasOrd()));
        dia.setHe60(nz(fila.getHe60()));
        dia.setHe100(nz(fila.getHe100()));
        dia.setCodigo(fila.getCodigo() == null ? CodigoTareoCc.LAB : fila.getCodigo());
        dia.setAltura(fila.isAltura());
        dia.setAltitud(fila.isAltitud());
        dia.setAgua(fila.isAgua());
        dia.setAltasTemp(fila.isAltasTemp());
        dia.setCotaCero(fila.isCotaCero());
        dia.setNocturno(fila.isNocturno());
        dia.setAguasServidas(fila.isAguasServidas());
        dia.setBae(fila.isBae());
        dia.setMovilidad(fila.isMovilidad());
        dia.setPisos(Math.max(0, fila.getPisos()));
    }

    private static Map<String, TareoCcDia> indexarTareo(List<TareoCcDia> dias) {
        Map<String, TareoCcDia> mapa = new HashMap<>();
        for (TareoCcDia dia : dias) {
            mapa.put(clave(dia.getPersona().getId(), dia.getFecha()), dia);
        }
        return mapa;
    }

    private static List<LocalDate> fechasDe(PeriodoCc periodo) {
        List<LocalDate> fechas = new ArrayList<>();
        LocalDate cursor = periodo.getFechaDesde();
        while (!cursor.isAfter(periodo.getFechaHasta())) {
            fechas.add(cursor);
            cursor = cursor.plusDays(1);
        }
        return fechas;
    }

    private static String clave(Long personaId, LocalDate fecha) {
        return personaId + "|" + fecha;
    }

    private BigDecimal[] tarifasDe(BoletaCc boleta) {
        BigDecimal tarifaDia = nz(boleta.getTarifaDia());
        BigDecimal tarifaHora = nz(boleta.getTarifaHora());
        if (tarifaDia.signum() > 0 && tarifaHora.signum() > 0) {
            return new BigDecimal[] {tarifaDia, tarifaHora};
        }
        CcParametros parametros = toParametros(parametrosVigentes(boleta.getPeriodo().getFechaHasta().getYear()));
        CategoriaObrera categoria = contratoCcRepository.findByContratoId(boleta.getContrato().getId())
                .map(ContratoCc::getCategoria)
                .orElse(CategoriaObrera.OPERARIO);
        BigDecimal jornal = parametros.getJornales().getOrDefault(categoria, BigDecimal.ZERO);
        BigDecimal horasDia = parametros.getHorasDia() == null || parametros.getHorasDia().signum() == 0
                ? new BigDecimal("8")
                : parametros.getHorasDia();
        BigDecimal hora = jornal.divide(horasDia, 4, RoundingMode.HALF_UP);
        return new BigDecimal[] {
                tarifaDia.signum() > 0 ? tarifaDia : jornal,
                tarifaHora.signum() > 0 ? tarifaHora : hora
        };
    }

    private static ProyectoDTO toProyectoDto(Proyecto proyecto, CcParametroAnual parametros) {
        return ProyectoDTO.builder()
                .id(proyecto.getId())
                .codigo(proyecto.getCodigo())
                .nombre(proyecto.getNombre())
                .ubicacion(proyecto.getUbicacion())
                .centroCosto(proyecto.getCentroCosto())
                .valorObra(proyecto.getValorObra())
                .activo(proyecto.isActivo())
                .alertaUit(UitObraAlerta.supera(proyecto.getValorObra(), parametros.getUit()))
                .uitAnio(parametros.getAnio())
                .build();
    }

    private static PersonalObraDTO toPersonalDto(ContratoCc contratoCc) {
        Contrato contrato = contratoCc.getContrato();
        Persona persona = contrato.getPersona();
        return PersonalObraDTO.builder()
                .personaId(persona.getId())
                .contratoId(contrato.getId())
                .nombres(persona.getNombres())
                .apellidos(persona.getApellidos())
                .numeroDocumento(persona.getNumeroDocumento())
                .categoria(contratoCc.getCategoria())
                .especialidadBae(contratoCc.getEspecialidadBae())
                .hijosEscolaridad(contratoCc.getHijosEscolaridad())
                .afiliadoSindical(contratoCc.isAfiliadoSindical())
                .build();
    }

    private static PeriodoCcDTO toPeriodoDto(PeriodoCc periodo, int cantidadBoletas) {
        Proyecto proyecto = periodo.getProyecto();
        return PeriodoCcDTO.builder()
                .id(periodo.getId())
                .proyectoId(proyecto.getId())
                .proyectoCodigo(proyecto.getCodigo())
                .proyectoNombre(proyecto.getNombre())
                .fechaDesde(periodo.getFechaDesde())
                .fechaHasta(periodo.getFechaHasta())
                .nroSemana(periodo.getNroSemana())
                .estado(periodo.getEstado())
                .createdAt(periodo.getCreatedAt())
                .closedAt(periodo.getClosedAt())
                .cantidadBoletas(cantidadBoletas)
                .build();
    }

    private static TareoCcDiaDTO toDiaDto(TareoCcDia dia) {
        return TareoCcDiaDTO.builder()
                .personaId(dia.getPersona().getId())
                .fecha(dia.getFecha())
                .horasOrd(dia.getHorasOrd())
                .he60(dia.getHe60())
                .he100(dia.getHe100())
                .codigo(dia.getCodigo())
                .altura(dia.isAltura())
                .altitud(dia.isAltitud())
                .agua(dia.isAgua())
                .altasTemp(dia.isAltasTemp())
                .cotaCero(dia.isCotaCero())
                .nocturno(dia.isNocturno())
                .aguasServidas(dia.isAguasServidas())
                .bae(dia.isBae())
                .movilidad(dia.isMovilidad())
                .pisos(dia.getPisos())
                .build();
    }

    private static BoletaCcResumenDTO toResumen(BoletaCc boleta, ContratoCc contratoCc) {
        Persona persona = boleta.getPersona();
        return BoletaCcResumenDTO.builder()
                .id(boleta.getId())
                .personaId(persona.getId())
                .personaNombreCompleto(persona.getApellidos() + ", " + persona.getNombres())
                .numeroDocumento(persona.getNumeroDocumento())
                .categoria(contratoCc == null ? null : contratoCc.getCategoria().name())
                .totalHaberes(boleta.getTotalHaberes())
                .totalDescuentos(boleta.getTotalDescuentos())
                .neto(boleta.getNeto())
                .build();
    }

    private static BoletaCcDTO toBoletaDto(BoletaCc boleta) {
        Persona persona = boleta.getPersona();
        return BoletaCcDTO.builder()
                .id(boleta.getId())
                .periodoId(boleta.getPeriodo().getId())
                .personaId(persona.getId())
                .contratoId(boleta.getContrato().getId())
                .personaNombres(persona.getNombres())
                .personaApellidos(persona.getApellidos())
                .numeroDocumento(persona.getNumeroDocumento())
                .categoria(null)
                .totalHaberes(boleta.getTotalHaberes())
                .totalDescuentos(boleta.getTotalDescuentos())
                .totalAportesEmpleador(boleta.getTotalAportesEmpleador())
                .totalAfecto(boleta.getTotalAfecto())
                .neto(boleta.getNeto())
                .tarifaDia(boleta.getTarifaDia())
                .tarifaHora(boleta.getTarifaHora())
                .createdAt(boleta.getCreatedAt())
                .conceptos(boleta.getConceptos().stream()
                        .map(item -> new BoletaCcConceptoDTO(
                                item.getCategoria(),
                                item.getCodigo(),
                                item.getLabel(),
                                item.getMonto(),
                                item.getOrden()))
                        .toList())
                .build();
    }

    private static String labelCategoria(CategoriaObrera categoria) {
        return switch (categoria) {
            case OPERARIO -> "Operario";
            case OFICIAL -> "Oficial";
            case PEON -> "Peón";
        };
    }

    private static BigDecimal nz(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private static String blankToNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
