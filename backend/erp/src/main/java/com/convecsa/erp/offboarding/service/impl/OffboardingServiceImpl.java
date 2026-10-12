package com.convecsa.erp.offboarding.service.impl;

import com.convecsa.erp.common.exception.CeseNoEncontradoException;
import com.convecsa.erp.common.exception.ContratoYaCesadoException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.contrato.domain.EstadoContrato;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.offboarding.domain.LiquidacionSnapshot;
import com.convecsa.erp.offboarding.dto.CeseDTO;
import com.convecsa.erp.offboarding.dto.CeseRequest;
import com.convecsa.erp.offboarding.dto.GeneracionOffboardingRequest;
import com.convecsa.erp.offboarding.dto.LiquidacionPreviewDTO;
import com.convecsa.erp.offboarding.entity.Cese;
import com.convecsa.erp.offboarding.repository.CeseRepository;
import com.convecsa.erp.offboarding.service.InterfazOffboardingService;
import com.convecsa.erp.offboarding.support.CalculoLiquidacionService;
import com.convecsa.erp.onboarding.domain.AmbitoPlantilla;
import com.convecsa.erp.onboarding.dto.GeneracionOnboardingDTO;
import com.convecsa.erp.onboarding.dto.PlantillaOnboardingDTO;
import com.convecsa.erp.onboarding.entity.GeneracionOnboarding;
import com.convecsa.erp.onboarding.entity.PlantillaOnboarding;
import com.convecsa.erp.onboarding.repository.GeneracionOnboardingRepository;
import com.convecsa.erp.onboarding.repository.PlantillaOnboardingRepository;
import com.convecsa.erp.onboarding.service.impl.OnboardingServiceImpl;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OffboardingServiceImpl implements InterfazOffboardingService {

    private static final List<AmbitoPlantilla> AMBITOS_OFFBOARDING =
            List.of(AmbitoPlantilla.OFFBOARDING, AmbitoPlantilla.AMBOS);

    private final CeseRepository ceseRepository;
    private final ContratoRepository contratoRepository;
    private final PersonaRepository personaRepository;
    private final PlantillaOnboardingRepository plantillaOnboardingRepository;
    private final GeneracionOnboardingRepository generacionOnboardingRepository;
    private final CalculoLiquidacionService calculoLiquidacionService;

    @Override
    @Transactional(readOnly = true)
    public LiquidacionPreviewDTO previewLiquidacion(Long personaId, LocalDate fechaCese) {
        Contrato contrato = contratoActivo(personaId);
        LiquidacionPreviewDTO dto = toLiquidacionDto(calculoLiquidacionService.calcular(
                contrato.getFechaInicio(), fechaCese, contrato.getSueldo()));
        // #region agent log
        debugLog(
                "H5",
                "OffboardingServiceImpl.previewLiquidacion",
                "preview ok without JSONB persist",
                "{\"personaId\":" + personaId + ",\"fechaCese\":\"" + fechaCese + "\",\"total\":\"" + dto.getTotal() + "\"}");
        // #endregion
        return dto;
    }

    @Override
    @Transactional
    public CeseDTO registrarCese(CeseRequest request) {
        Contrato contrato = contratoActivo(request.getPersonaId());
        if (ceseRepository.existsByContratoId(contrato.getId())) {
            throw new ContratoYaCesadoException("El contrato ya tiene un cese registrado");
        }
        LiquidacionSnapshot calculado = calculoLiquidacionService.calcular(
                contrato.getFechaInicio(), request.getFechaCese(), contrato.getSueldo());
        aplicarMontosEditados(calculado, request);

        Cese cese = new Cese();
        cese.setContrato(contrato);
        cese.setPersona(contrato.getPersona());
        cese.setFechaCese(request.getFechaCese());
        cese.setTipoCese(request.getTipoCese());
        cese.setMotivo(request.getMotivo() == null ? null : request.getMotivo().trim());
        cese.setLiquidacion(calculado);
        // #region agent log
        debugLog(
                "H3",
                "OffboardingServiceImpl.registrarCese:beforeSave",
                "snapshot before JSONB persist",
                "{\"personaId\":"
                        + request.getPersonaId()
                        + ",\"fechaInicio\":\""
                        + calculado.getFechaInicio()
                        + "\",\"fechaCese\":\""
                        + calculado.getFechaCese()
                        + "\",\"sueldo\":\""
                        + calculado.getSueldo()
                        + "\",\"cts\":\""
                        + calculado.getCts()
                        + "\",\"hasGetters\":"
                        + (calculado.getClass().getMethods().length)
                        + "}");
        // #endregion
        Cese guardado;
        try {
            guardado = ceseRepository.save(cese);
            // #region agent log
            debugLog(
                    "H3",
                    "OffboardingServiceImpl.registrarCese:afterSave",
                    "save succeeded",
                    "{\"ceseId\":" + guardado.getId() + "}");
            // #endregion
        } catch (Exception ex) {
            // #region agent log
            debugLog(
                    "H1",
                    "OffboardingServiceImpl.registrarCese:saveFailed",
                    "save threw",
                    "{\"exClass\":\""
                            + ex.getClass().getName()
                            + "\",\"msg\":\""
                            + sanitize(ex.getMessage())
                            + "\",\"causeChain\":\""
                            + sanitize(causeChain(ex))
                            + "\"}");
            // #endregion
            throw ex;
        }

        contrato.setEstado(EstadoContrato.RESCINDIDO);
        contrato.setFechaCorte(request.getFechaCese());
        contratoRepository.save(contrato);
        return toCeseDto(guardado);
    }

    // #region agent log
    private static void debugLog(String hypothesisId, String location, String message, String dataJson) {
        try (PrintWriter out = new PrintWriter(new FileWriter(
                System.getProperty("user.dir") + "/../../debug-f22012.log", true))) {
            out.println(
                    "{\"sessionId\":\"f22012\",\"runId\":\"post-fix\",\"hypothesisId\":\""
                            + hypothesisId
                            + "\",\"location\":\""
                            + location
                            + "\",\"message\":\""
                            + message
                            + "\",\"data\":"
                            + dataJson
                            + ",\"timestamp\":"
                            + System.currentTimeMillis()
                            + "}");
        } catch (Exception ignored) {
            try (PrintWriter out = new PrintWriter(new FileWriter("D:/Proyectos/Convecsa ERP/debug-f22012.log", true))) {
                out.println(
                        "{\"sessionId\":\"f22012\",\"runId\":\"post-fix\",\"hypothesisId\":\""
                                + hypothesisId
                                + "\",\"location\":\""
                                + location
                                + "\",\"message\":\""
                                + message
                                + "\",\"data\":"
                                + dataJson
                                + ",\"timestamp\":"
                                + System.currentTimeMillis()
                                + "}");
            } catch (Exception ignored2) {
                // no-op
            }
        }
    }

    private static String causeChain(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        Throwable cur = ex;
        int depth = 0;
        while (cur != null && depth < 8) {
            if (depth > 0) {
                sb.append(" | ");
            }
            sb.append(cur.getClass().getName()).append(": ").append(cur.getMessage());
            cur = cur.getCause();
            depth++;
        }
        return sb.toString();
    }

    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\\', '/').replace('"', '\'');
    }
    // #endregion

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CeseDTO> listarCeses(int page, int size) {
        return PagedResponse.from(
                ceseRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)).map(OffboardingServiceImpl::toCeseDto));
    }

    @Override
    @Transactional(readOnly = true)
    public CeseDTO obtenerCese(Long id) {
        return toCeseDto(buscarCese(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlantillaOnboardingDTO> listarPlantillas() {
        return plantillaOnboardingRepository
                .findByActivoTrueAndAmbitoInOrderByCodigoAsc(AMBITOS_OFFBOARDING)
                .stream()
                .map(OffboardingServiceImpl::toPlantillaDto)
                .toList();
    }

    @Override
    @Transactional
    public GeneracionOnboardingDTO crearGeneracion(Long ceseId, GeneracionOffboardingRequest request) {
        Cese cese = buscarCese(ceseId);
        List<String> codigos = parseCodigos(request.getCodigos());
        if (codigos.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un código de plantilla");
        }
        validarPlantillas(codigos);
        GeneracionOnboarding generacion = new GeneracionOnboarding();
        generacion.setPersona(cese.getPersona());
        generacion.setContrato(cese.getContrato());
        generacion.setCese(cese);
        generacion.setCodigos(String.join(",", codigos));
        GeneracionOnboarding guardada = generacionOnboardingRepository.save(generacion);
        return new GeneracionOnboardingDTO(
                guardada.getId(),
                cese.getPersona().getId(),
                cese.getPersona().getNombres(),
                cese.getPersona().getApellidos(),
                cese.getPersona().getNumeroDocumento(),
                OnboardingServiceImpl.parseCodigos(guardada.getCodigos()),
                guardada.getCreatedAt());
    }

    private Contrato contratoActivo(Long personaId) {
        if (personaId == null) {
            throw new IllegalArgumentException("Debe indicar la persona");
        }
        if (!personaRepository.existsById(personaId)) {
            throw new PersonaNoEncontradaException("No se encontró la persona");
        }
        return contratoRepository
                .findFirstByPersonaIdAndEstadoOrderByCreatedAtDesc(personaId, EstadoContrato.ACTIVO)
                .orElseThrow(() -> new IllegalArgumentException("La persona no tiene un contrato activo"));
    }

    private Cese buscarCese(Long id) {
        return ceseRepository.findById(id)
                .orElseThrow(() -> new CeseNoEncontradoException("No se encontró el cese"));
    }

    private void validarPlantillas(List<String> codigos) {
        Set<String> existentes = new LinkedHashSet<>();
        for (PlantillaOnboarding plantilla : plantillaOnboardingRepository.findByCodigoInAndActivoTrue(codigos)) {
            if (plantilla.getAmbito() == AmbitoPlantilla.ONBOARDING) {
                throw new IllegalArgumentException("Plantilla no disponible para offboarding: " + plantilla.getCodigo());
            }
            existentes.add(plantilla.getCodigo());
        }
        for (String codigo : codigos) {
            if (!existentes.contains(codigo)) {
                throw new IllegalArgumentException("Plantilla inválida: " + codigo);
            }
        }
    }

    private static void aplicarMontosEditados(LiquidacionSnapshot calculado, CeseRequest request) {
        if (request.getCts() != null) {
            calculado.setCts(escala(request.getCts()));
        }
        if (request.getGratificacion() != null) {
            calculado.setGratificacion(escala(request.getGratificacion()));
        }
        if (request.getVacaciones() != null) {
            calculado.setVacaciones(escala(request.getVacaciones()));
        }
        calculado.setTotal(calculado.getCts()
                .add(calculado.getGratificacion())
                .add(calculado.getVacaciones())
                .setScale(2, RoundingMode.HALF_UP));
    }

    private static BigDecimal escala(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Los montos de liquidación no pueden ser negativos");
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private static List<String> parseCodigos(List<String> origen) {
        LinkedHashSet<String> codigos = new LinkedHashSet<>();
        if (origen == null) {
            return List.of();
        }
        for (String parte : origen) {
            if (parte == null || parte.isBlank()) {
                continue;
            }
            codigos.add(parte.trim().toUpperCase());
        }
        return new ArrayList<>(codigos);
    }

    private static CeseDTO toCeseDto(Cese cese) {
        Persona persona = cese.getPersona();
        return CeseDTO.builder()
                .id(cese.getId())
                .personaId(persona.getId())
                .personaNombres(persona.getNombres())
                .personaApellidos(persona.getApellidos())
                .numeroDocumento(persona.getNumeroDocumento())
                .contratoId(cese.getContrato().getId())
                .fechaCese(cese.getFechaCese())
                .tipoCese(cese.getTipoCese())
                .motivo(cese.getMotivo())
                .liquidacion(toLiquidacionDto(cese.getLiquidacion()))
                .createdAt(cese.getCreatedAt())
                .build();
    }

    private static LiquidacionPreviewDTO toLiquidacionDto(LiquidacionSnapshot snapshot) {
        if (snapshot == null) {
            return LiquidacionPreviewDTO.builder().build();
        }
        return LiquidacionPreviewDTO.builder()
                .sueldo(snapshot.getSueldo())
                .cts(snapshot.getCts())
                .gratificacion(snapshot.getGratificacion())
                .vacaciones(snapshot.getVacaciones())
                .total(snapshot.getTotal())
                .tiempoServicio(snapshot.getTiempoServicio())
                .fechaInicio(snapshot.getFechaInicio())
                .fechaCese(snapshot.getFechaCese())
                .build();
    }

    private static PlantillaOnboardingDTO toPlantillaDto(PlantillaOnboarding plantilla) {
        return new PlantillaOnboardingDTO(
                plantilla.getId(),
                plantilla.getCodigo(),
                plantilla.getCategoria(),
                plantilla.getTitulo(),
                plantilla.getDescripcion(),
                plantilla.getBaseLegal(),
                plantilla.getAmbito());
    }
}
