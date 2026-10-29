package com.convecsa.erp.nomina.declaracion.service;

import com.convecsa.erp.asistencia.entity.AsistenciaRegistro;
import com.convecsa.erp.asistencia.repository.AsistenciaRegistroRepository;
import com.convecsa.erp.common.exception.DeclaracionMensualDuplicadaException;
import com.convecsa.erp.common.exception.DeclaracionMensualNoEncontradaException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.exception.ReciboHonorarioNoEncontradoException;
import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.construccion.domain.EstadoPeriodoCc;
import com.convecsa.erp.construccion.entity.BoletaCc;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import com.convecsa.erp.construccion.repository.BoletaCcRepository;
import com.convecsa.erp.construccion.repository.PeriodoCcRepository;
import com.convecsa.erp.construccion.repository.TareoCcDiaRepository;
import com.convecsa.erp.nomina.declaracion.domain.EstadoDeclaracion;
import com.convecsa.erp.nomina.declaracion.dto.ArchivoDescargaDTO;
import com.convecsa.erp.nomina.declaracion.dto.ConsolidadoCcConceptoDTO;
import com.convecsa.erp.nomina.declaracion.dto.ConsolidadoCcDTO;
import com.convecsa.erp.nomina.declaracion.dto.DeclaracionDetalleDTO;
import com.convecsa.erp.nomina.declaracion.dto.DeclaracionMensualDTO;
import com.convecsa.erp.nomina.declaracion.dto.DeclaracionMensualRequest;
import com.convecsa.erp.nomina.declaracion.dto.ReciboHonorarioAsignarRequest;
import com.convecsa.erp.nomina.declaracion.dto.ReciboHonorarioDTO;
import com.convecsa.erp.nomina.declaracion.dto.RxhImportacionDTO;
import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCc;
import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCcConcepto;
import com.convecsa.erp.nomina.declaracion.entity.DeclaracionMensual;
import com.convecsa.erp.nomina.declaracion.entity.ReciboHonorario;
import com.convecsa.erp.nomina.declaracion.repository.ConsolidadoCcRepository;
import com.convecsa.erp.nomina.declaracion.repository.DeclaracionMensualRepository;
import com.convecsa.erp.nomina.declaracion.repository.ReciboHonorarioRepository;
import com.convecsa.erp.nomina.declaracion.support.AfpnetArchivoService;
import com.convecsa.erp.nomina.declaracion.support.AsientoRccService;
import com.convecsa.erp.nomina.declaracion.support.ConsolidadoCalculo;
import com.convecsa.erp.nomina.declaracion.support.ConsolidadoCcCalculator;
import com.convecsa.erp.nomina.declaracion.support.DispersionArchivoService;
import com.convecsa.erp.nomina.declaracion.support.PlameArchivoService;
import com.convecsa.erp.nomina.declaracion.support.PlameFormato;
import com.convecsa.erp.nomina.declaracion.support.PlameZipService;
import com.convecsa.erp.nomina.declaracion.support.RxhParser;
import com.convecsa.erp.nomina.dto.AsientoContableDTO;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.nomina.entity.NominaDocumentoConfiguracion;
import com.convecsa.erp.nomina.entity.PeriodoNomina;
import com.convecsa.erp.nomina.repository.BoletaPagoRepository;
import com.convecsa.erp.nomina.repository.NominaDocumentoConfiguracionRepository;
import com.convecsa.erp.nomina.repository.PeriodoNominaRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeclaracionMensualService {

    private static final String ZIP_TYPE = "application/zip";
    private static final String TXT_TYPE = "text/plain;charset=UTF-8";
    private static final String CSV_TYPE = "text/csv;charset=UTF-8";
    private static final String PDF_TYPE = "application/pdf";

    private final DeclaracionMensualRepository declaracionRepository;
    private final ConsolidadoCcRepository consolidadoRepository;
    private final ReciboHonorarioRepository reciboRepository;
    private final PeriodoCcRepository periodoCcRepository;
    private final BoletaCcRepository boletaCcRepository;
    private final TareoCcDiaRepository tareoCcDiaRepository;
    private final PeriodoNominaRepository periodoNominaRepository;
    private final BoletaPagoRepository boletaPagoRepository;
    private final AsistenciaRegistroRepository asistenciaRegistroRepository;
    private final PersonaRepository personaRepository;
    private final NominaDocumentoConfiguracionRepository documentoConfiguracionRepository;
    private final PlameArchivoService plameArchivoService;
    private final PlameZipService plameZipService;
    private final AfpnetArchivoService afpnetArchivoService;
    private final DispersionArchivoService dispersionArchivoService;
    private final AsientoRccService asientoRccService;
    private final ConsolidadoCcCalculator calculator = new ConsolidadoCcCalculator();
    private final RxhParser rxhParser = new RxhParser();

    @Transactional(readOnly = true)
    public PagedResponse<DeclaracionMensualDTO> listar(int page, int size) {
        return PagedResponse.from(declaracionRepository
                .findAllByOrderByAnioDescMesDesc(PageRequest.of(page, size))
                .map(this::toResumen));
    }

    @Transactional
    public DeclaracionMensualDTO crear(DeclaracionMensualRequest request) {
        if (declaracionRepository.existsByAnioAndMes(request.getAnio(), request.getMes())) {
            throw new DeclaracionMensualDuplicadaException(
                    "Ya existe una declaración para " + request.getAnio() + "-" + request.getMes());
        }
        DeclaracionMensual declaracion = new DeclaracionMensual();
        declaracion.setAnio(request.getAnio());
        declaracion.setMes(request.getMes());
        declaracion.setEstado(EstadoDeclaracion.BORRADOR);
        return toResumen(declaracionRepository.save(declaracion));
    }

    @Transactional(readOnly = true)
    public DeclaracionDetalleDTO obtener(Long id) {
        DeclaracionMensual declaracion = cargar(id);
        return new DeclaracionDetalleDTO(
                toResumen(declaracion),
                consolidadoRepository.findByDeclaracionIdOrderByIdAsc(id).stream().map(this::toConsolidadoDto).toList(),
                reciboRepository.findByDeclaracionIdOrderByIdAsc(id).stream().map(this::toReciboDto).toList());
    }

    @Transactional
    public DeclaracionDetalleDTO consolidarRcc(Long id) {
        DeclaracionMensual declaracion = cargarEditable(id);
        YearMonth ym = YearMonth.of(declaracion.getAnio(), declaracion.getMes());
        LocalDate desde = ym.atDay(1);
        LocalDate hasta = ym.atEndOfMonth();
        if (periodoCcRepository.existsByFechaHastaBetweenAndEstado(desde, hasta, EstadoPeriodoCc.BORRADOR)) {
            throw new IllegalArgumentException(
                    "Hay periodos RCC en borrador cuyo fin cae en este mes. Ciérrelos antes de consolidar.");
        }
        List<PeriodoCc> periodos = periodoCcRepository.findByFechaHastaBetween(desde, hasta).stream()
                .filter(p -> p.getEstado() == EstadoPeriodoCc.CERRADO)
                .toList();
        List<Long> periodoIds = periodos.stream().map(PeriodoCc::getId).toList();
        List<BoletaCc> boletas = periodoIds.isEmpty()
                ? List.of()
                : boletaCcRepository.findByPeriodoIdIn(periodoIds);
        List<TareoCcDia> dias = periodoIds.isEmpty()
                ? List.of()
                : tareoCcDiaRepository.findByPeriodoIdIn(periodoIds);

        List<ConsolidadoCc> existentes = consolidadoRepository.findByDeclaracionIdOrderByIdAsc(id);
        consolidadoRepository.deleteAll(existentes);
        consolidadoRepository.flush();

        List<ConsolidadoCalculo> calculos = calculator.calcular(boletas, dias);
        for (ConsolidadoCalculo calculo : calculos) {
            ConsolidadoCc fila = new ConsolidadoCc();
            fila.setDeclaracion(declaracion);
            fila.setPersona(calculo.getPersona());
            fila.setProyecto(calculo.getProyecto());
            fila.setTotalHaberes(calculo.getTotalHaberes());
            fila.setTotalDescuentos(calculo.getTotalDescuentos());
            fila.setTotalAportesEmpleador(calculo.getTotalAportesEmpleador());
            fila.setTotalAfecto(calculo.getTotalAfecto());
            fila.setNeto(calculo.getNeto());
            fila.setHorasOrd(calculo.getHorasOrd());
            fila.setHe60(calculo.getHe60());
            fila.setHe100(calculo.getHe100());
            fila.setDiasLab(calculo.getDiasLab());
            fila.setDiasFalta(calculo.getDiasFalta());
            fila.setDiasDm(calculo.getDiasDm());
            fila.setDiasFe(calculo.getDiasFe());
            fila.setDiasLic(calculo.getDiasLic());
            List<ConsolidadoCcConcepto> conceptos = new ArrayList<>();
            for (ConsolidadoCalculo.Concepto item : calculo.getConceptos()) {
                ConsolidadoCcConcepto concepto = new ConsolidadoCcConcepto();
                concepto.setCategoria(item.getCategoria());
                concepto.setCodigo(item.getCodigo());
                concepto.setLabel(item.getLabel());
                concepto.setMonto(item.getMonto());
                conceptos.add(concepto);
            }
            fila.reemplazarConceptos(conceptos);
            consolidadoRepository.save(fila);
        }
        return obtener(id);
    }

    @Transactional
    public DeclaracionMensualDTO cerrar(Long id) {
        DeclaracionMensual declaracion = cargarEditable(id);
        declaracion.setEstado(EstadoDeclaracion.CERRADO);
        declaracion.setClosedAt(Instant.now());
        return toResumen(declaracion);
    }

    @Transactional
    public RxhImportacionDTO importarRxh(Long id, String contenido) {
        DeclaracionMensual declaracion = cargarEditable(id);
        List<ReciboHonorario> parseados = rxhParser.parsear(contenido);
        Set<String> documentos = new HashSet<>();
        for (ReciboHonorario recibo : parseados) {
            String dni = RxhParser.dniDesdeDocumento(recibo.getTipoDocumento(), recibo.getNumeroDocumento());
            if (dni != null && !dni.isBlank()) {
                documentos.add(dni);
            }
            documentos.add(recibo.getNumeroDocumento());
        }
        Map<String, Persona> porDocumento = new HashMap<>();
        if (!documentos.isEmpty()) {
            for (Persona persona : personaRepository.findByNumeroDocumentoIn(documentos)) {
                porDocumento.put(persona.getNumeroDocumento(), persona);
            }
        }

        List<ReciboHonorario> existentes = reciboRepository.findByDeclaracionIdOrderByIdAsc(id);
        reciboRepository.deleteAll(existentes);
        reciboRepository.flush();

        int conMatch = 0;
        for (ReciboHonorario recibo : parseados) {
            recibo.setDeclaracion(declaracion);
            Persona persona = porDocumento.get(recibo.getNumeroDocumento());
            if (persona == null) {
                String dni = RxhParser.dniDesdeDocumento(recibo.getTipoDocumento(), recibo.getNumeroDocumento());
                persona = porDocumento.get(dni);
            }
            if (persona != null) {
                recibo.setPersona(persona);
                conMatch++;
            }
            reciboRepository.save(recibo);
        }
        return new RxhImportacionDTO(parseados.size(), conMatch, parseados.size() - conMatch);
    }

    @Transactional
    public ReciboHonorarioDTO asignarRxh(Long id, Long reciboId, ReciboHonorarioAsignarRequest request) {
        cargarEditable(id);
        ReciboHonorario recibo = reciboRepository.findByIdAndDeclaracionId(reciboId, id)
                .orElseThrow(() -> new ReciboHonorarioNoEncontradoException("No se encontró el recibo por honorarios"));
        Persona persona;
        if (request.getPersonaId() != null) {
            persona = personaRepository.findById(request.getPersonaId())
                    .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
        } else if (request.getNumeroDocumento() != null && !request.getNumeroDocumento().isBlank()) {
            persona = personaRepository.findByNumeroDocumento(request.getNumeroDocumento().trim())
                    .orElseThrow(() -> new PersonaNoEncontradaException(
                            "No hay persona con documento " + request.getNumeroDocumento().trim()));
        } else {
            throw new IllegalArgumentException("Indique el DNI o el identificador de la persona");
        }
        recibo.setPersona(persona);
        return toReciboDto(recibo);
    }

    @Transactional(readOnly = true)
    public ArchivoDescargaDTO descargarPlame(Long id) {
        DeclaracionMensual declaracion = cargar(id);
        Fuentes fuentes = cargarFuentes(declaracion);
        String base = nombreBase(declaracion);
        String jor = plameArchivoService.generarJor(fuentes.obreros, fuentes.administrativos);
        String rem = plameArchivoService.generarRem(fuentes.obreros, fuentes.administrativos, fuentes.recibos);
        String snl = plameArchivoService.generarSnl(fuentes.obreros, fuentes.asistencias);
        byte[] zip = plameZipService.empaquetar(base, jor, rem, snl);
        return new ArchivoDescargaDTO(base + ".zip", ZIP_TYPE, zip);
    }

    @Transactional(readOnly = true)
    public ArchivoDescargaDTO descargarAfpnet(Long id) {
        DeclaracionMensual declaracion = cargar(id);
        Fuentes fuentes = cargarFuentes(declaracion);
        String contenido = afpnetArchivoService.generar(fuentes.obreros, fuentes.administrativos);
        String nombre = "afpnet-" + nombreBase(declaracion) + ".csv";
        return new ArchivoDescargaDTO(nombre, CSV_TYPE, contenido.getBytes(StandardCharsets.UTF_8));
    }

    @Transactional(readOnly = true)
    public AsientoContableDTO asiento(Long id) {
        DeclaracionMensual declaracion = cargar(id);
        List<ConsolidadoCc> filas = consolidadoRepository.findByDeclaracionIdOrderByIdAsc(id);
        return asientoRccService.construir(declaracion, filas);
    }

    @Transactional(readOnly = true)
    public ArchivoDescargaDTO descargarAsientoPdf(Long id) {
        AsientoContableDTO asiento = asiento(id);
        byte[] pdf = asientoRccService.generarPdf(asiento, cargarEmpresa());
        String nombre = "asiento-rcc-" + declaracionPeriodo(cargar(id)) + ".pdf";
        return new ArchivoDescargaDTO(nombre, PDF_TYPE, pdf);
    }

    @Transactional(readOnly = true)
    public ArchivoDescargaDTO descargarDispersion(Long id) {
        DeclaracionMensual declaracion = cargar(id);
        Fuentes fuentes = cargarFuentes(declaracion);
        for (ConsolidadoCc fila : fuentes.obreros) {
            Hibernate.initialize(fila.getPersona().getCuentasBancarias());
        }
        for (BoletaPago boleta : fuentes.administrativos) {
            Hibernate.initialize(boleta.getPersona().getCuentasBancarias());
        }
        String contenido = dispersionArchivoService.generar(fuentes.obreros, fuentes.administrativos);
        String nombre = "dispersion-" + declaracionPeriodo(declaracion) + ".txt";
        return new ArchivoDescargaDTO(nombre, TXT_TYPE, contenido.getBytes(StandardCharsets.UTF_8));
    }

    private Fuentes cargarFuentes(DeclaracionMensual declaracion) {
        List<ConsolidadoCc> obreros = consolidadoRepository.findByDeclaracionIdOrderByIdAsc(declaracion.getId());
        List<ReciboHonorario> recibos = reciboRepository.findByDeclaracionIdOrderByIdAsc(declaracion.getId());
        List<BoletaPago> administrativos = List.of();
        List<AsistenciaRegistro> asistencias = List.of();
        PeriodoNomina periodoAdmin = periodoNominaRepository
                .findByAnioAndMes(declaracion.getAnio(), declaracion.getMes())
                .orElse(null);
        if (periodoAdmin != null) {
            administrativos = boletaPagoRepository.findByPeriodoId(periodoAdmin.getId());
            YearMonth ym = YearMonth.of(declaracion.getAnio(), declaracion.getMes());
            asistencias = asistenciaRegistroRepository
                    .findByFechaBetweenOrderByPersonaApellidosAscPersonaNombresAscFechaAsc(
                            ym.atDay(1), ym.atEndOfMonth());
        }
        return new Fuentes(obreros, administrativos, recibos, asistencias);
    }

    private DeclaracionMensual cargar(Long id) {
        return declaracionRepository.findById(id)
                .orElseThrow(() -> new DeclaracionMensualNoEncontradaException("No se encontró la declaración mensual"));
    }

    private DeclaracionMensual cargarEditable(Long id) {
        DeclaracionMensual declaracion = cargar(id);
        if (declaracion.getEstado() == EstadoDeclaracion.CERRADO) {
            throw new IllegalArgumentException("La declaración está cerrada");
        }
        return declaracion;
    }

    private NominaDocumentoConfiguracion cargarEmpresa() {
        return documentoConfiguracionRepository.findById(NominaDocumentoConfiguracion.ID_UNICO)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la configuración documental de nómina"));
    }

    private String nombreBase(DeclaracionMensual declaracion) {
        NominaDocumentoConfiguracion empresa = documentoConfiguracionRepository
                .findById(NominaDocumentoConfiguracion.ID_UNICO)
                .orElse(null);
        String ruc = PlameFormato.rucArchivo(empresa == null ? null : empresa.getRuc());
        return ruc + PlameFormato.periodo(declaracion.getAnio(), declaracion.getMes());
    }

    private static String declaracionPeriodo(DeclaracionMensual declaracion) {
        return PlameFormato.periodo(declaracion.getAnio(), declaracion.getMes());
    }

    private DeclaracionMensualDTO toResumen(DeclaracionMensual declaracion) {
        long obreros = consolidadoRepository.countByDeclaracionId(declaracion.getId());
        long rxh = reciboRepository.countByDeclaracionId(declaracion.getId());
        long huerfanos = reciboRepository.countByDeclaracionIdAndPersonaIsNull(declaracion.getId());
        long admin = periodoNominaRepository.findByAnioAndMes(declaracion.getAnio(), declaracion.getMes())
                .map(periodo -> boletaPagoRepository.countByPeriodoId(periodo.getId()))
                .orElse(0L);
        BigDecimal netoRaw = consolidadoRepository.sumNetoByDeclaracionId(declaracion.getId());
        BigDecimal neto = (netoRaw == null ? BigDecimal.ZERO : netoRaw).setScale(2, RoundingMode.HALF_UP);
        return new DeclaracionMensualDTO(
                declaracion.getId(),
                declaracion.getAnio(),
                declaracion.getMes(),
                declaracion.getEstado(),
                declaracion.getCreatedAt(),
                declaracion.getClosedAt(),
                admin,
                obreros,
                rxh,
                huerfanos,
                neto);
    }

    private ConsolidadoCcDTO toConsolidadoDto(ConsolidadoCc fila) {
        Persona persona = fila.getPersona();
        Proyecto proyecto = fila.getProyecto();
        return new ConsolidadoCcDTO(
                fila.getId(),
                persona.getId(),
                persona.getNombres(),
                persona.getApellidos(),
                persona.getNumeroDocumento(),
                proyecto == null ? null : proyecto.getId(),
                proyecto == null ? null : proyecto.getCodigo(),
                proyecto == null ? null : proyecto.getNombre(),
                fila.getTotalHaberes(),
                fila.getTotalDescuentos(),
                fila.getTotalAportesEmpleador(),
                fila.getTotalAfecto(),
                fila.getNeto(),
                fila.getHorasOrd(),
                fila.getHe60(),
                fila.getHe100(),
                fila.getDiasLab(),
                fila.getDiasFalta(),
                fila.getDiasDm(),
                fila.getDiasFe(),
                fila.getDiasLic(),
                fila.getConceptos().stream()
                        .map(c -> new ConsolidadoCcConceptoDTO(
                                c.getId(), c.getCategoria(), c.getCodigo(), c.getLabel(), c.getMonto(), c.getOrden()))
                        .collect(Collectors.toList()));
    }

    private ReciboHonorarioDTO toReciboDto(ReciboHonorario recibo) {
        Persona persona = recibo.getPersona();
        return new ReciboHonorarioDTO(
                recibo.getId(),
                persona == null ? null : persona.getId(),
                persona == null ? null : persona.getApellidos() + " " + persona.getNombres(),
                recibo.getTipoDocumento(),
                recibo.getNumeroDocumento(),
                recibo.getApellidoPaterno(),
                recibo.getApellidoMaterno(),
                recibo.getNombres(),
                recibo.isDomiciliado(),
                recibo.getTipoComprobante(),
                recibo.getSerie(),
                recibo.getNumero(),
                recibo.getMonto(),
                recibo.getFechaEmision(),
                recibo.getFechaPago(),
                recibo.isRetencionCuarta(),
                recibo.isRetencionPension(),
                recibo.getImporteAportePension(),
                persona == null);
    }

    private record Fuentes(
            List<ConsolidadoCc> obreros,
            List<BoletaPago> administrativos,
            List<ReciboHonorario> recibos,
            List<AsistenciaRegistro> asistencias) {}
}
