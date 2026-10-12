package com.convecsa.erp.onboarding.service.impl;

import com.convecsa.erp.common.exception.GeneracionOnboardingNoEncontradaException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.contrato.domain.EstadoContrato;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.contrato.support.ContratoPlantillaService;
import com.convecsa.erp.offboarding.support.CeseTokens;
import com.convecsa.erp.onboarding.domain.AmbitoPlantilla;
import com.convecsa.erp.onboarding.dto.GeneracionOnboardingDTO;
import com.convecsa.erp.onboarding.dto.GeneracionOnboardingRequest;
import com.convecsa.erp.onboarding.dto.OnboardingDocumentoHtml;
import com.convecsa.erp.onboarding.dto.PlantillaOnboardingDTO;
import com.convecsa.erp.onboarding.entity.GeneracionOnboarding;
import com.convecsa.erp.onboarding.entity.PlantillaOnboarding;
import com.convecsa.erp.onboarding.repository.GeneracionOnboardingRepository;
import com.convecsa.erp.onboarding.repository.PlantillaOnboardingRepository;
import com.convecsa.erp.onboarding.service.InterfazOnboardingService;
import com.convecsa.erp.onboarding.support.OnboardingHtmlPacket;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingServiceImpl implements InterfazOnboardingService {

    private static final ZoneId ZONA_PE = ZoneId.of("America/Lima");
    private static final DateTimeFormatter FECHA_ARCHIVO = DateTimeFormatter.BASIC_ISO_DATE;

    private final PlantillaOnboardingRepository plantillaOnboardingRepository;
    private final GeneracionOnboardingRepository generacionOnboardingRepository;
    private final PersonaRepository personaRepository;
    private final ContratoRepository contratoRepository;
    private final ContratoPlantillaService contratoPlantillaService;

    @Override
    @Transactional(readOnly = true)
    public List<PlantillaOnboardingDTO> listarPlantillas() {
        return plantillaOnboardingRepository
                .findByActivoTrueAndAmbitoInOrderByCodigoAsc(
                        List.of(AmbitoPlantilla.ONBOARDING, AmbitoPlantilla.AMBOS))
                .stream()
                .map(OnboardingServiceImpl::toPlantillaDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public String documento(Long personaId, String codigosParam) {
        List<String> codigos = parseCodigos(codigosParam);
        Persona persona = cargarPersona(personaId);
        Contrato contrato = contratoActivo(personaId);
        prepararContrato(contrato, persona);
        return renderPaquete(persona, contrato, codigos);
    }

    @Override
    @Transactional
    public GeneracionOnboardingDTO crearGeneracion(GeneracionOnboardingRequest request) {
        List<String> codigos = parseCodigos(String.join(",", request.getCodigos()));
        if (codigos.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un código de plantilla");
        }
        validarPlantillas(codigos);
        Persona persona = cargarPersona(request.getPersonaId());
        Contrato contrato = contratoActivo(request.getPersonaId());
        GeneracionOnboarding generacion = new GeneracionOnboarding();
        generacion.setPersona(persona);
        generacion.setContrato(contrato);
        generacion.setCodigos(String.join(",", codigos));
        return toGeneracionDto(generacionOnboardingRepository.save(generacion));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<GeneracionOnboardingDTO> listarGeneraciones(int page, int size) {
        return PagedResponse.from(
                generacionOnboardingRepository
                        .findByCeseIsNullOrderByCreatedAtDesc(PageRequest.of(page, size))
                        .map(OnboardingServiceImpl::toGeneracionDto));
    }

    @Override
    @Transactional(readOnly = true)
    public OnboardingDocumentoHtml documentoGeneracion(Long id, boolean descarga, boolean imprimir) {
        GeneracionOnboarding generacion = generacionOnboardingRepository.findById(id)
                .orElseThrow(() -> new GeneracionOnboardingNoEncontradaException("No se encontró la generación"));
        Persona persona = generacion.getPersona();
        persona.getCelulares().size();
        persona.getExperienciasLaborales().size();
        Contrato contrato = generacion.getContrato();
        prepararContrato(contrato, persona);
        List<String> codigos = parseCodigos(generacion.getCodigos());
        Map<String, String> extras = generacion.getCese() == null
                ? Map.of()
                : CeseTokens.de(generacion.getCese());
        String html = renderPaquete(persona, contrato, codigos, extras);
        if (imprimir) {
            html = OnboardingHtmlPacket.conImpresionAutomatica(html);
        }
        return new OnboardingDocumentoHtml(html, nombreArchivo(persona));
    }

    private Persona cargarPersona(Long personaId) {
        if (personaId == null) {
            throw new IllegalArgumentException("Debe indicar la persona");
        }
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
        persona.getCelulares().size();
        persona.getExperienciasLaborales().size();
        return persona;
    }

    private Contrato contratoActivo(Long personaId) {
        return contratoRepository
                .findFirstByPersonaIdAndEstadoOrderByCreatedAtDesc(personaId, EstadoContrato.ACTIVO)
                .orElseThrow(() -> new IllegalArgumentException("La persona no tiene un contrato activo"));
    }

    private void prepararContrato(Contrato contrato, Persona persona) {
        contrato.setPersona(persona);
        if (contrato.getCausaObjetiva() != null) {
            contrato.getCausaObjetiva().getLabel();
        }
        if (contrato.getJornada() != null) {
            contrato.getJornada().getLabel();
        }
        if (contrato.getMetodoPago() != null) {
            contrato.getMetodoPago().getLabel();
        }
    }

    private String renderPaquete(Persona persona, Contrato contrato, List<String> codigos) {
        return renderPaquete(persona, contrato, codigos, Map.of());
    }

    private String renderPaquete(
            Persona persona, Contrato contrato, List<String> codigos, Map<String, String> extras) {
        if (codigos.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un código de plantilla");
        }
        Map<String, PlantillaOnboarding> porCodigo = validarPlantillas(codigos);
        List<String> htmls = new ArrayList<>();
        for (String codigo : codigos) {
            htmls.add(contratoPlantillaService.render(
                    porCodigo.get(codigo).getPlantilla(), persona, contrato, extras));
        }
        return OnboardingHtmlPacket.unir(htmls);
    }

    private Map<String, PlantillaOnboarding> validarPlantillas(List<String> codigos) {
        Map<String, PlantillaOnboarding> porCodigo = plantillaOnboardingRepository
                .findByCodigoInAndActivoTrue(codigos)
                .stream()
                .collect(Collectors.toMap(PlantillaOnboarding::getCodigo, Function.identity()));
        for (String codigo : codigos) {
            if (!porCodigo.containsKey(codigo)) {
                throw new IllegalArgumentException("Plantilla inválida: " + codigo);
            }
        }
        return porCodigo;
    }

    public static List<String> parseCodigos(String codigosParam) {
        if (codigosParam == null || codigosParam.isBlank()) {
            return List.of();
        }
        LinkedHashSet<String> codigos = new LinkedHashSet<>();
        for (String parte : codigosParam.split(",")) {
            String codigo = parte.trim().toUpperCase();
            if (!codigo.isEmpty()) {
                codigos.add(codigo);
            }
        }
        return new ArrayList<>(codigos);
    }

    private static String nombreArchivo(Persona persona) {
        String dni = persona.getNumeroDocumento() == null
                ? "persona"
                : persona.getNumeroDocumento().replaceAll("[^A-Za-z0-9]", "");
        if (dni.isBlank()) {
            dni = "persona";
        }
        String fecha = LocalDate.now(ZONA_PE).format(FECHA_ARCHIVO);
        return "expediente-" + dni + "-" + fecha + ".html";
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

    private static GeneracionOnboardingDTO toGeneracionDto(GeneracionOnboarding generacion) {
        Persona persona = generacion.getPersona();
        return new GeneracionOnboardingDTO(
                generacion.getId(),
                persona.getId(),
                persona.getNombres(),
                persona.getApellidos(),
                persona.getNumeroDocumento(),
                parseCodigos(generacion.getCodigos()),
                generacion.getCreatedAt());
    }
}
