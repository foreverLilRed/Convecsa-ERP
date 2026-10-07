package com.convecsa.erp.nomina.service.impl;

import com.convecsa.erp.common.exception.BoletaPagoNoEncontradaException;
import com.convecsa.erp.common.exception.PeriodoNominaDuplicadoException;
import com.convecsa.erp.common.exception.PeriodoNominaNoEncontradoException;
import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.contrato.domain.EstadoContrato;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.nomina.catalogo.Afp;
import com.convecsa.erp.nomina.domain.CodigoConcepto;
import com.convecsa.erp.nomina.domain.EstadoBoletaPago;
import com.convecsa.erp.nomina.domain.EstadoPeriodoNomina;
import com.convecsa.erp.nomina.domain.OrigenConcepto;
import com.convecsa.erp.nomina.dto.AfpDTO;
import com.convecsa.erp.nomina.dto.BoletaConceptoDTO;
import com.convecsa.erp.nomina.dto.BoletaConceptoExtraRequest;
import com.convecsa.erp.nomina.dto.BoletaDocumentoHtml;
import com.convecsa.erp.nomina.dto.BoletaPagoDTO;
import com.convecsa.erp.nomina.dto.BoletaPagoResumenDTO;
import com.convecsa.erp.nomina.dto.BoletaUpdateRequest;
import com.convecsa.erp.nomina.dto.PeriodoNominaDTO;
import com.convecsa.erp.nomina.dto.PeriodoNominaRequest;
import com.convecsa.erp.nomina.entity.BoletaConcepto;
import com.convecsa.erp.nomina.entity.BoletaPago;
import com.convecsa.erp.nomina.entity.PeriodoNomina;
import com.convecsa.erp.nomina.entity.PlantillaBoleta;
import com.convecsa.erp.nomina.repository.AfpRepository;
import com.convecsa.erp.nomina.repository.BoletaPagoRepository;
import com.convecsa.erp.nomina.repository.PeriodoNominaRepository;
import com.convecsa.erp.nomina.repository.PlantillaBoletaRepository;
import com.convecsa.erp.nomina.service.InterfazNominaService;
import com.convecsa.erp.nomina.support.BoletaPlantillaService;
import com.convecsa.erp.nomina.support.CalculoNominaService;
import com.convecsa.erp.onboarding.support.OnboardingHtmlPacket;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.entity.PersonaCuentaBancaria;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NominaServiceImpl implements InterfazNominaService {

    private static final String PLANTILLA_BOLETA = "BOLETA-01";
    private static final String AFP_DEFAULT = "ONP";

    private final AfpRepository afpRepository;
    private final PeriodoNominaRepository periodoNominaRepository;
    private final BoletaPagoRepository boletaPagoRepository;
    private final PlantillaBoletaRepository plantillaBoletaRepository;
    private final ContratoRepository contratoRepository;
    private final CalculoNominaService calculoNominaService;
    private final BoletaPlantillaService boletaPlantillaService;

    @Override
    @Transactional(readOnly = true)
    public List<AfpDTO> listarAfp() {
        return afpRepository.findByActivoTrueOrderByIdAsc().stream()
                .map(NominaServiceImpl::toAfpDto)
                .toList();
    }

    @Override
    @Transactional
    public PeriodoNominaDTO crearPeriodo(PeriodoNominaRequest request) {
        int anio = request.getAnio();
        int mes = request.getMes();
        if (periodoNominaRepository.existsByAnioAndMes(anio, mes)) {
            throw new PeriodoNominaDuplicadoException("Ya existe un periodo para " + mes + "/" + anio);
        }
        PeriodoNomina periodo = new PeriodoNomina();
        periodo.setAnio(anio);
        periodo.setMes(mes);
        periodo.setEstado(EstadoPeriodoNomina.BORRADOR);
        periodo.setObservaciones(trimToNull(request.getObservaciones()));
        return toPeriodoDto(periodoNominaRepository.save(periodo));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PeriodoNominaDTO> listarPeriodos(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? 20 : Math.min(size, 100);
        return PagedResponse.from(
                periodoNominaRepository.findAllByOrderByAnioDescMesDesc(PageRequest.of(safePage, safeSize))
                        .map(this::toPeriodoDto));
    }

    @Override
    @Transactional(readOnly = true)
    public PeriodoNominaDTO obtenerPeriodo(Long id) {
        return toPeriodoDto(cargarPeriodo(id));
    }

    @Override
    @Transactional
    public PeriodoNominaDTO generarBoletas(Long periodoId) {
        PeriodoNomina periodo = cargarPeriodo(periodoId);
        asegurarBorrador(periodo);
        YearMonth yearMonth = YearMonth.of(periodo.getAnio(), periodo.getMes());
        LocalDate inicio = yearMonth.atDay(1);
        LocalDate fin = yearMonth.atEndOfMonth();
        List<Contrato> vigentes = contratoRepository.findVigentesEnRango(EstadoContrato.ACTIVO, inicio, fin);
        Map<Long, Contrato> porPersona = new LinkedHashMap<>();
        for (Contrato contrato : vigentes) {
            Long personaId = contrato.getPersona().getId();
            porPersona.putIfAbsent(personaId, contrato);
        }
        if (porPersona.isEmpty()) {
            throw new IllegalArgumentException("No hay contratos activos vigentes en el periodo");
        }
        Afp afpDefault = afpRepository.findByCodigo(AFP_DEFAULT)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el catálogo ONP"));
        int creadas = 0;
        for (Contrato contrato : porPersona.values()) {
            Persona persona = contrato.getPersona();
            if (boletaPagoRepository.existsByPeriodoIdAndPersonaId(periodo.getId(), persona.getId())) {
                continue;
            }
            if (contrato.getSueldo() == null) {
                throw new IllegalArgumentException(
                        "El contrato de " + persona.getApellidos() + " no tiene sueldo");
            }
            BoletaPago boleta = new BoletaPago();
            boleta.setPeriodo(periodo);
            boleta.setPersona(persona);
            boleta.setContrato(contrato);
            boleta.setAfp(contrato.getAfp() == null ? afpDefault : contrato.getAfp());
            boleta.setDiasTrabajados(30);
            boleta.setAplicaAsigFamiliar(contrato.isTieneAsignacionFamiliar());
            boleta.setEstado(EstadoBoletaPago.BORRADOR);
            calculoNominaService.calcular(boleta);
            boletaPagoRepository.save(boleta);
            creadas++;
        }
        if (creadas == 0 && boletaPagoRepository.countByPeriodoId(periodo.getId()) == 0) {
            throw new IllegalArgumentException("No se generaron boletas");
        }
        return toPeriodoDto(periodo);
    }

    @Override
    @Transactional
    public PeriodoNominaDTO recalcularPeriodo(Long periodoId) {
        PeriodoNomina periodo = cargarPeriodo(periodoId);
        asegurarBorrador(periodo);
        List<BoletaPago> boletas = boletaPagoRepository.findByPeriodoId(periodo.getId());
        for (BoletaPago boleta : boletas) {
            if (boleta.getEstado() != EstadoBoletaPago.BORRADOR) {
                continue;
            }
            calculoNominaService.calcular(boleta);
        }
        return toPeriodoDto(periodo);
    }

    @Override
    @Transactional
    public PeriodoNominaDTO cerrarPeriodo(Long periodoId) {
        PeriodoNomina periodo = cargarPeriodo(periodoId);
        asegurarBorrador(periodo);
        List<BoletaPago> boletas = boletaPagoRepository.findByPeriodoId(periodo.getId());
        if (boletas.isEmpty()) {
            throw new IllegalArgumentException("No se puede cerrar un periodo sin boletas");
        }
        for (BoletaPago boleta : boletas) {
            boleta.setEstado(EstadoBoletaPago.APROBADA);
        }
        periodo.setEstado(EstadoPeriodoNomina.CERRADO);
        periodo.setClosedAt(Instant.now());
        return toPeriodoDto(periodo);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BoletaPagoResumenDTO> listarBoletas(Long periodoId, int page, int size) {
        cargarPeriodo(periodoId);
        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? 20 : Math.min(size, 100);
        return PagedResponse.from(
                boletaPagoRepository
                        .findByPeriodoIdOrderByPersonaApellidosAscPersonaNombresAsc(
                                periodoId, PageRequest.of(safePage, safeSize))
                        .map(NominaServiceImpl::toResumenDto));
    }

    @Override
    @Transactional(readOnly = true)
    public BoletaPagoDTO obtenerBoleta(Long id) {
        return toBoletaDto(cargarBoleta(id));
    }

    @Override
    @Transactional
    public BoletaPagoDTO actualizarBoleta(Long id, BoletaUpdateRequest request) {
        BoletaPago boleta = cargarBoleta(id);
        asegurarBoletaEditable(boleta);
        Afp afp = afpRepository.findById(request.getAfpId())
                .orElseThrow(() -> new IllegalArgumentException("AFP inválida"));
        boleta.setDiasTrabajados(request.getDiasTrabajados());
        boleta.setAplicaAsigFamiliar(Boolean.TRUE.equals(request.getAplicaAsigFamiliar()));
        boleta.setAfp(afp);
        boleta.getConceptos().removeIf(concepto -> concepto.getOrigen() == OrigenConcepto.MANUAL);
        List<BoletaConceptoExtraRequest> extras = request.getExtras() == null ? List.of() : request.getExtras();
        for (BoletaConceptoExtraRequest extra : extras) {
            if (extra.getCategoria() == null || extra.getLabel() == null || extra.getLabel().isBlank()) {
                throw new IllegalArgumentException("Cada concepto extra requiere categoría y nombre");
            }
            BoletaConcepto concepto = new BoletaConcepto();
            concepto.setCategoria(extra.getCategoria());
            concepto.setCodigo(CodigoConcepto.EXTRA);
            concepto.setLabel(extra.getLabel().trim());
            concepto.setMonto(extra.getMonto() == null ? BigDecimal.ZERO : extra.getMonto());
            concepto.setOrden(boleta.getConceptos().size() + 1);
            concepto.setOrigen(OrigenConcepto.MANUAL);
            boleta.addConcepto(concepto);
        }
        calculoNominaService.calcular(boleta);
        boletaPagoRepository.saveAndFlush(boleta);
        return toBoletaDto(boleta);
    }

    @Override
    @Transactional
    public BoletaPagoDTO recalcularBoleta(Long id) {
        BoletaPago boleta = cargarBoleta(id);
        asegurarBoletaEditable(boleta);
        calculoNominaService.calcular(boleta);
        boletaPagoRepository.saveAndFlush(boleta);
        return toBoletaDto(boleta);
    }

    @Override
    @Transactional(readOnly = true)
    public BoletaDocumentoHtml documentoBoleta(Long id, boolean descarga, boolean imprimir) {
        BoletaPago boleta = cargarBoleta(id);
        PlantillaBoleta plantilla = plantillaBoletaRepository.findByCodigoAndActivoTrue(PLANTILLA_BOLETA)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la plantilla de boleta"));
        String html = boletaPlantillaService.render(boleta, plantilla);
        if (imprimir) {
            html = OnboardingHtmlPacket.conImpresionAutomatica(html);
        }
        return new BoletaDocumentoHtml(html, nombreArchivo(boleta));
    }

    private PeriodoNomina cargarPeriodo(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Debe indicar el periodo");
        }
        return periodoNominaRepository.findById(id)
                .orElseThrow(() -> new PeriodoNominaNoEncontradoException("No se encontró el periodo"));
    }

    private BoletaPago cargarBoleta(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Debe indicar la boleta");
        }
        BoletaPago boleta = boletaPagoRepository.findWithDetalleById(id)
                .orElseThrow(() -> new BoletaPagoNoEncontradaException("No se encontró la boleta"));
        boleta.getPersona().getCuentasBancarias().size();
        return boleta;
    }

    private static void asegurarBorrador(PeriodoNomina periodo) {
        if (periodo.getEstado() != EstadoPeriodoNomina.BORRADOR) {
            throw new IllegalArgumentException("El periodo ya está cerrado");
        }
    }

    private static void asegurarBoletaEditable(BoletaPago boleta) {
        if (boleta.getPeriodo().getEstado() != EstadoPeriodoNomina.BORRADOR
                || boleta.getEstado() != EstadoBoletaPago.BORRADOR) {
            throw new IllegalArgumentException("La boleta no se puede editar porque el periodo está cerrado");
        }
    }

    private PeriodoNominaDTO toPeriodoDto(PeriodoNomina periodo) {
        long cantidad = boletaPagoRepository.countByPeriodoId(periodo.getId());
        BigDecimal totalNeto = boletaPagoRepository.sumNetoByPeriodoId(periodo.getId());
        return new PeriodoNominaDTO(
                periodo.getId(),
                periodo.getAnio(),
                periodo.getMes(),
                periodo.getEstado(),
                periodo.getObservaciones(),
                periodo.getCreatedAt(),
                periodo.getClosedAt(),
                cantidad,
                totalNeto == null ? BigDecimal.ZERO : totalNeto);
    }

    private static BoletaPagoResumenDTO toResumenDto(BoletaPago boleta) {
        Persona persona = boleta.getPersona();
        Contrato contrato = boleta.getContrato();
        return new BoletaPagoResumenDTO(
                boleta.getId(),
                persona.getId(),
                persona.getNombres(),
                persona.getApellidos(),
                persona.getNumeroDocumento(),
                contrato == null ? null : contrato.getCargo(),
                boleta.getAfp() == null ? null : boleta.getAfp().getLabel(),
                boleta.getDiasTrabajados(),
                boleta.getTotalHaberes(),
                boleta.getTotalDescuentos(),
                boleta.getNeto(),
                boleta.getEstado());
    }

    private static BoletaPagoDTO toBoletaDto(BoletaPago boleta) {
        Persona persona = boleta.getPersona();
        Contrato contrato = boleta.getContrato();
        PersonaCuentaBancaria cuenta = cuentaPrincipal(persona);
        PeriodoNomina periodo = boleta.getPeriodo();
        Afp afp = boleta.getAfp();
        return new BoletaPagoDTO(
                boleta.getId(),
                periodo.getId(),
                periodo.getAnio(),
                periodo.getMes(),
                persona.getId(),
                persona.getNombres(),
                persona.getApellidos(),
                persona.getNumeroDocumento(),
                contrato == null ? null : contrato.getCargo(),
                cuenta == null || cuenta.getBanco() == null ? null : cuenta.getBanco().name(),
                cuenta == null ? null : cuenta.getCci(),
                afp == null ? null : afp.getId(),
                afp == null ? null : afp.getCodigo(),
                afp == null ? null : afp.getLabel(),
                boleta.getDiasTrabajados(),
                boleta.isAplicaAsigFamiliar(),
                boleta.getTotalHaberes(),
                boleta.getTotalDescuentos(),
                boleta.getTotalAportesEmpleador(),
                boleta.getNeto(),
                boleta.getEstado(),
                boleta.getConceptos().stream().map(NominaServiceImpl::toConceptoDto).toList(),
                boleta.getCreatedAt(),
                boleta.getUpdatedAt());
    }

    private static BoletaConceptoDTO toConceptoDto(BoletaConcepto concepto) {
        return new BoletaConceptoDTO(
                concepto.getId(),
                concepto.getCategoria(),
                concepto.getCodigo(),
                concepto.getLabel(),
                concepto.getMonto(),
                concepto.getOrden(),
                concepto.getOrigen());
    }

    private static AfpDTO toAfpDto(Afp afp) {
        return new AfpDTO(
                afp.getId(),
                afp.getCodigo(),
                afp.getLabel(),
                afp.getAporteObligatorio(),
                afp.getComisionFlujo(),
                afp.getPrimaSeguro(),
                afp.isEsOnp());
    }

    private static PersonaCuentaBancaria cuentaPrincipal(Persona persona) {
        if (persona.getCuentasBancarias() == null || persona.getCuentasBancarias().isEmpty()) {
            return null;
        }
        return persona.getCuentasBancarias().stream()
                .filter(PersonaCuentaBancaria::isPrincipal)
                .findFirst()
                .orElse(persona.getCuentasBancarias().get(0));
    }

    private static String nombreArchivo(BoletaPago boleta) {
        String dni = boleta.getPersona().getNumeroDocumento() == null
                ? "persona"
                : boleta.getPersona().getNumeroDocumento().replaceAll("[^A-Za-z0-9]", "");
        if (dni.isBlank()) {
            dni = "persona";
        }
        PeriodoNomina periodo = boleta.getPeriodo();
        String mes = String.format("%02d", periodo.getMes());
        return "boleta-" + dni + "-" + periodo.getAnio() + mes + ".html";
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
