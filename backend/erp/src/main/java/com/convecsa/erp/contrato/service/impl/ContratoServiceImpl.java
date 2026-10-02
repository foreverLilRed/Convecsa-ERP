package com.convecsa.erp.contrato.service.impl;

import com.convecsa.erp.common.exception.ContratoNoEncontradoException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.contrato.catalogo.CausaObjetiva;
import com.convecsa.erp.contrato.catalogo.Jornada;
import com.convecsa.erp.contrato.catalogo.MetodoPago;
import com.convecsa.erp.contrato.catalogo.TipoPlantilla;
import com.convecsa.erp.contrato.domain.EstadoContrato;
import com.convecsa.erp.contrato.dto.CatalogoItemDTO;
import com.convecsa.erp.contrato.dto.ContratoDTO;
import com.convecsa.erp.contrato.dto.ContratoRequest;
import com.convecsa.erp.contrato.dto.ContratosCatalogosDTO;
import com.convecsa.erp.contrato.dto.ContratosResumeDTO;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.repository.CausaObjetivaRepository;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.contrato.repository.JornadaRepository;
import com.convecsa.erp.contrato.repository.MetodoPagoRepository;
import com.convecsa.erp.contrato.repository.TipoPlantillaRepository;
import com.convecsa.erp.contrato.service.InterfazContratoService;
import com.convecsa.erp.contrato.support.ContratoPlantillaService;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContratoServiceImpl implements InterfazContratoService {

    private final ContratoRepository contratoRepository;
    private final PersonaRepository personaRepository;
    private final JornadaRepository jornadaRepository;
    private final CausaObjetivaRepository causaObjetivaRepository;
    private final TipoPlantillaRepository tipoPlantillaRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final ContratoPlantillaService contratoPlantillaService;

    @Override
    @Transactional(readOnly = true)
    public ContratosResumeDTO resume() {
        long conContratoActivo = contratoRepository.countDistinctPersonaIdByEstado(EstadoContrato.ACTIVO);
        long totalPersonas = personaRepository.count();
        long sinContratoActivo = Math.max(0, totalPersonas - conContratoActivo);
        return new ContratosResumeDTO(conContratoActivo, sinContratoActivo);
    }

    @Override
    @Transactional(readOnly = true)
    public ContratosCatalogosDTO catalogos() {
        return new ContratosCatalogosDTO(
                jornadaRepository.findByActivoTrueOrderByIdAsc().stream()
                        .map(item -> new CatalogoItemDTO(item.getId(), item.getCodigo(), item.getLabel()))
                        .toList(),
                causaObjetivaRepository.findByActivoTrueOrderByIdAsc().stream()
                        .map(item -> new CatalogoItemDTO(item.getId(), item.getCodigo(), item.getLabel()))
                        .toList(),
                tipoPlantillaRepository.findByActivoTrueOrderByIdAsc().stream()
                        .map(item -> new CatalogoItemDTO(item.getId(), item.getCodigo(), item.getLabel()))
                        .toList(),
                metodoPagoRepository.findByActivoTrueOrderByIdAsc().stream()
                        .map(item -> new CatalogoItemDTO(item.getId(), item.getCodigo(), item.getLabel()))
                        .toList());
    }

    @Override
    @Transactional
    public ContratoDTO crear(ContratoRequest request) {
        Persona persona = personaRepository.findById(request.getPersonaId())
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
        TipoPlantilla plantilla = tipoPlantillaRepository.findById(request.getTipoPlantillaId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de plantilla inválido"));
        CausaObjetiva causa = causaObjetivaRepository.findById(request.getCausaObjetivaId())
                .orElseThrow(() -> new IllegalArgumentException("Causa objetiva inválida"));
        Jornada jornada = jornadaRepository.findById(request.getJornadaId())
                .orElseThrow(() -> new IllegalArgumentException("Jornada inválida"));
        MetodoPago metodoPago = metodoPagoRepository.findById(request.getMetodoPagoId())
                .orElseThrow(() -> new IllegalArgumentException("Método de pago inválido"));

        Contrato contrato = new Contrato();
        contrato.setPersona(persona);
        contrato.setEstado(EstadoContrato.ACTIVO);
        contrato.setTipoPlantilla(plantilla);
        contrato.setCausaObjetiva(causa);
        contrato.setCargo(request.getCargo().trim());
        contrato.setArea(request.getArea().trim());
        contrato.setProyecto(request.getProyecto().trim());
        contrato.setUbicacionObra(request.getUbicacionObra().trim());
        contrato.setFechaInicio(request.getFechaInicio());
        contrato.setFechaFin(request.getFechaFin());
        contrato.setFechaCorte(request.getFechaCorte());
        contrato.setJornada(jornada);
        contrato.setSueldo(request.getSueldo());
        contrato.setMetodoPago(metodoPago);
        contrato.setCiudadFirma(request.getCiudadFirma().trim());
        contrato.setNumero(blankToNull(request.getNumero()));
        contrato.setObservaciones(blankToNull(request.getObservaciones()));
        Contrato saved = contratoRepository.save(contrato);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public String documento(Long id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ContratoNoEncontradoException("No se encontró el contrato"));
        contrato.getPersona().getNombres();
        if (contrato.getCausaObjetiva() != null) {
            contrato.getCausaObjetiva().getLabel();
        }
        if (contrato.getJornada() != null) {
            contrato.getJornada().getLabel();
        }
        if (contrato.getMetodoPago() != null) {
            contrato.getMetodoPago().getLabel();
        }
        return contratoPlantillaService.render(contrato);
    }

    private static ContratoDTO toDto(Contrato contrato) {
        return ContratoDTO.builder()
                .id(contrato.getId())
                .personaId(contrato.getPersona().getId())
                .estado(contrato.getEstado())
                .fechaInicio(contrato.getFechaInicio())
                .fechaFin(contrato.getFechaFin())
                .cargo(contrato.getCargo())
                .proyecto(contrato.getProyecto())
                .createdAt(contrato.getCreatedAt())
                .sueldo(contrato.getSueldo())
                .build();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
