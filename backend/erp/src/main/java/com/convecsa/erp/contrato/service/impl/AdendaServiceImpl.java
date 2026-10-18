package com.convecsa.erp.contrato.service.impl;

import com.convecsa.erp.common.exception.AdendaNoEncontradaException;
import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.contrato.catalogo.PlantillaCampoEsquema;
import com.convecsa.erp.contrato.domain.UsoPlantilla;
import com.convecsa.erp.contrato.dto.AdendaDTO;
import com.convecsa.erp.contrato.dto.AdendaRequest;
import com.convecsa.erp.contrato.entity.Adenda;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.entity.PlantillaContrato;
import com.convecsa.erp.contrato.exception.ContratoNoEncontrado;
import com.convecsa.erp.contrato.repository.AdendaRepository;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.contrato.repository.PlantillaContratoRepository;
import com.convecsa.erp.contrato.service.InterfazAdendaService;
import com.convecsa.erp.contrato.support.ContratoPlantillaService;
import com.convecsa.erp.persona.entity.Persona;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdendaServiceImpl implements InterfazAdendaService {

    private final AdendaRepository adendaRepository;
    private final ContratoRepository contratoRepository;
    private final PlantillaContratoRepository plantillaContratoRepository;
    private final ContratoPlantillaService contratoPlantillaService;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdendaDTO> listar(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? 20 : Math.min(size, 100);
        return PagedResponse.from(
                adendaRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(safePage, safeSize))
                        .map(AdendaServiceImpl::toDto));
    }

    @Override
    @Transactional
    public AdendaDTO crear(AdendaRequest request) {
        Contrato contrato = contratoRepository.findById(request.getContratoId())
                .orElseThrow(() -> new ContratoNoEncontrado("No se encontró el contrato"));
        if (!contrato.isActivo()) {
            throw new IllegalArgumentException("Solo se puede prorrogar un contrato activo.");
        }
        PlantillaContrato plantilla = plantillaContratoRepository.findById(request.getPlantillaContratoId())
                .orElseThrow(() -> new IllegalArgumentException("Plantilla inválida"));
        if (plantilla.getUso() != UsoPlantilla.ADENDA) {
            throw new IllegalArgumentException("La plantilla no es de adenda.");
        }
        LocalDate finAnterior = contrato.getFechaFin();
        if (finAnterior == null) {
            throw new IllegalArgumentException("El contrato no tiene fecha de fin para prorrogar.");
        }
        if (!request.getFechaFinNueva().isAfter(finAnterior)) {
            throw new IllegalArgumentException("La nueva fecha de término debe ser posterior al vencimiento actual.");
        }
        String motivo = request.getMotivo().trim();
        Adenda adenda = new Adenda();
        adenda.setContrato(contrato);
        adenda.setPlantillaContrato(plantilla);
        adenda.setNumero((int) adendaRepository.countByContratoId(contrato.getId()) + 1);
        adenda.setFechaFinAnterior(finAnterior);
        adenda.setFechaFinNueva(request.getFechaFinNueva());
        adenda.setSueldo(request.getSueldo());
        adenda.setMotivo(motivo);
        adenda.setCiudadEmision(blankToNull(request.getCiudadEmision()));
        adenda.setFechaEmision(request.getFechaEmision());
        adenda.setMetadata(copiarMetadata(esquemaDe(plantilla), request.getMetadata()));
        Adenda saved = adendaRepository.save(adenda);
        contrato.setFechaFin(request.getFechaFinNueva());
        contrato.setSueldo(request.getSueldo());
        if (saved.getCiudadEmision() != null) {
            Map<String, String> metadata = new LinkedHashMap<>(contrato.getMetadata());
            metadata.put("ciudad_firma", saved.getCiudadEmision());
            contrato.setMetadata(metadata);
        }
        contratoRepository.save(contrato);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public String documento(Long id) {
        Adenda adenda = adendaRepository.findById(id)
                .orElseThrow(() -> new AdendaNoEncontradaException("No se encontró la adenda"));
        adenda.getContrato().getPersona().getNombres();
        if (adenda.getPlantillaContrato() != null) {
            adenda.getPlantillaContrato().getPlantilla();
        }
        return contratoPlantillaService.render(adenda);
    }

    private static Map<String, String> copiarMetadata(
            List<PlantillaCampoEsquema> esquema, Map<String, String> enviados) {
        Map<String, String> origen = enviados == null ? Map.of() : enviados;
        Map<String, String> guardados = new LinkedHashMap<>();
        if (esquema == null || esquema.isEmpty()) {
            return guardados;
        }
        for (PlantillaCampoEsquema campo : esquema) {
            if (campo == null || campo.getClave() == null || campo.getClave().isBlank()) {
                continue;
            }
            String clave = campo.getClave().trim();
            String crudo = origen.get(clave);
            if (crudo != null && !crudo.isBlank()) {
                guardados.put(clave, crudo.trim());
            }
        }
        return guardados;
    }

    private static List<PlantillaCampoEsquema> esquemaDe(PlantillaContrato plantilla) {
        List<PlantillaCampoEsquema> guardado = plantilla.getMetadata();
        if (guardado != null && !guardado.isEmpty()) {
            return guardado;
        }
        return ContratoPlantillaService.esquemaDesdeHtml(plantilla.getPlantilla());
    }

    private static AdendaDTO toDto(Adenda adenda) {
        Contrato contrato = adenda.getContrato();
        Persona persona = contrato.getPersona();
        PlantillaContrato plantilla = adenda.getPlantillaContrato();
        return AdendaDTO.builder()
                .id(adenda.getId())
                .numero(adenda.getNumero())
                .contratoId(contrato.getId())
                .personaId(persona.getId())
                .personaNombres(persona.getNombres())
                .personaApellidos(persona.getApellidos())
                .numeroDocumento(persona.getNumeroDocumento())
                .contratoFechaInicio(contrato.getFechaInicio())
                .fechaFinAnterior(adenda.getFechaFinAnterior())
                .fechaFinNueva(adenda.getFechaFinNueva())
                .sueldo(adenda.getSueldo())
                .motivo(adenda.getMotivo())
                .ciudadEmision(adenda.getCiudadEmision())
                .fechaEmision(adenda.getFechaEmision())
                .plantillaCodigo(plantilla == null ? null : plantilla.getCodigo())
                .plantillaNombre(plantilla == null ? null : plantilla.getNombre())
                .createdAt(adenda.getCreatedAt())
                .build();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
