package com.convecsa.erp.onboarding.service.impl;

import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.contrato.domain.EstadoContrato;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.contrato.support.ContratoPlantillaService;
import com.convecsa.erp.onboarding.dto.PlantillaOnboardingDTO;
import com.convecsa.erp.onboarding.entity.PlantillaOnboarding;
import com.convecsa.erp.onboarding.repository.PlantillaOnboardingRepository;
import com.convecsa.erp.onboarding.service.InterfazOnboardingService;
import com.convecsa.erp.onboarding.support.OnboardingHtmlPacket;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingServiceImpl implements InterfazOnboardingService {

    private final PlantillaOnboardingRepository plantillaOnboardingRepository;
    private final PersonaRepository personaRepository;
    private final ContratoRepository contratoRepository;
    private final ContratoPlantillaService contratoPlantillaService;

    @Override
    @Transactional(readOnly = true)
    public List<PlantillaOnboardingDTO> listarPlantillas() {
        return plantillaOnboardingRepository.findByActivoTrueOrderByCodigoAsc().stream()
                .map(OnboardingServiceImpl::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public String documento(Long personaId, String codigosParam) {
        if (personaId == null) {
            throw new IllegalArgumentException("Debe indicar la persona");
        }
        List<String> codigos = parseCodigos(codigosParam);
        if (codigos.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un código de plantilla");
        }

        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
        persona.getCelulares().size();
        persona.getExperienciasLaborales().size();

        Contrato contrato = contratoRepository
                .findFirstByPersonaIdAndEstadoOrderByCreatedAtDesc(personaId, EstadoContrato.ACTIVO)
                .orElseThrow(() -> new IllegalArgumentException("La persona no tiene un contrato activo"));
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

        Map<String, PlantillaOnboarding> porCodigo = plantillaOnboardingRepository
                .findByCodigoInAndActivoTrue(codigos)
                .stream()
                .collect(Collectors.toMap(PlantillaOnboarding::getCodigo, Function.identity()));
        List<String> htmls = new ArrayList<>();
        for (String codigo : codigos) {
            PlantillaOnboarding plantilla = porCodigo.get(codigo);
            if (plantilla == null) {
                throw new IllegalArgumentException("Plantilla inválida: " + codigo);
            }
            htmls.add(contratoPlantillaService.render(plantilla.getPlantilla(), persona, contrato));
        }
        return OnboardingHtmlPacket.unir(htmls);
    }

    static List<String> parseCodigos(String codigosParam) {
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

    private static PlantillaOnboardingDTO toDto(PlantillaOnboarding plantilla) {
        return new PlantillaOnboardingDTO(
                plantilla.getId(),
                plantilla.getCodigo(),
                plantilla.getCategoria(),
                plantilla.getTitulo(),
                plantilla.getDescripcion(),
                plantilla.getBaseLegal());
    }
}
