package com.convecsa.erp.contrato.service.impl;

import com.convecsa.erp.common.exception.ContratoNoEncontradoException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.exception.PlantillaCodigoDuplicadoException;
import com.convecsa.erp.common.exception.PlantillaNoEncontradaException;
import com.convecsa.erp.common.exception.PlantillaPinInvalidoException;
import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.contrato.catalogo.CausaObjetiva;
import com.convecsa.erp.contrato.catalogo.Jornada;
import com.convecsa.erp.contrato.catalogo.MetodoPago;
import com.convecsa.erp.contrato.catalogo.PlantillaCampoEsquema;
import com.convecsa.erp.contrato.domain.EstadoContrato;
import com.convecsa.erp.contrato.domain.UsoPlantilla;
import com.convecsa.erp.contrato.dto.CatalogoItemDTO;
import com.convecsa.erp.contrato.dto.ContratoDTO;
import com.convecsa.erp.contrato.dto.ContratoRequest;
import com.convecsa.erp.contrato.dto.ContratosCatalogosDTO;
import com.convecsa.erp.contrato.dto.ContratosResumeDTO;
import com.convecsa.erp.contrato.dto.PlantillaCampoDTO;
import com.convecsa.erp.contrato.dto.PlantillaContratoDTO;
import com.convecsa.erp.contrato.dto.PlantillaContratoDetalleDTO;
import com.convecsa.erp.contrato.dto.PlantillaContratoRequest;
import com.convecsa.erp.contrato.dto.PlantillaContratoUpdateRequest;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.contrato.entity.PlantillaContrato;
import com.convecsa.erp.contrato.repository.CausaObjetivaRepository;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.contrato.repository.JornadaRepository;
import com.convecsa.erp.contrato.repository.MetodoPagoRepository;
import com.convecsa.erp.contrato.repository.PlantillaContratoRepository;
import com.convecsa.erp.contrato.service.InterfazContratoService;
import com.convecsa.erp.contrato.support.ContratoPlantillaService;
import com.convecsa.erp.nomina.catalogo.Afp;
import com.convecsa.erp.nomina.repository.AfpRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContratoServiceImpl implements InterfazContratoService {

    private final ContratoRepository contratoRepository;
    private final PersonaRepository personaRepository;
    private final JornadaRepository jornadaRepository;
    private final CausaObjetivaRepository causaObjetivaRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final PlantillaContratoRepository plantillaContratoRepository;
    private final AfpRepository afpRepository;
    private final ContratoPlantillaService contratoPlantillaService;

    @Value("${app.plantillas.pin:6283}")
    private String plantillasPin;

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
                metodoPagoRepository.findByActivoTrueOrderByIdAsc().stream()
                        .map(item -> new CatalogoItemDTO(item.getId(), item.getCodigo(), item.getLabel()))
                        .toList(),
                plantillaContratoRepository.findByActivoTrueOrderByIdAsc().stream()
                        .map(ContratoServiceImpl::toPlantillaDto)
                        .toList(),
                afpRepository.findByActivoTrueOrderByIdAsc().stream()
                        .map(item -> new CatalogoItemDTO(item.getId(), item.getCodigo(), item.getLabel()))
                        .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContratoDTO> listar(int page, int size, EstadoContrato estado) {
        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? 20 : Math.min(size, 100);
        var pageable = PageRequest.of(safePage, safeSize);
        var resultado = estado == null
                ? contratoRepository.findAllByOrderByCreatedAtDesc(pageable)
                : contratoRepository.findByEstadoOrderByCreatedAtDesc(estado, pageable);
        return PagedResponse.from(resultado.map(ContratoServiceImpl::toDto));
    }

    @Override
    @Transactional
    public ContratoDTO crear(ContratoRequest request) {
        Persona persona = personaRepository.findById(request.getPersonaId())
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
        PlantillaContrato plantilla = plantillaContratoRepository.findById(request.getPlantillaContratoId())
                .orElseThrow(() -> new IllegalArgumentException("Plantilla inválida"));

        Contrato contrato = new Contrato();
        contrato.setPersona(persona);
        contrato.setEstado(EstadoContrato.ACTIVO);
        contrato.setPlantillaContrato(plantilla);
        contrato.setFechaInicio(request.getFechaInicio());
        contrato.setFechaFin(request.getFechaFin());
        contrato.setSueldo(request.getSueldo());
        contrato.setTieneAsignacionFamiliar(Boolean.TRUE.equals(request.getTieneAsignacionFamiliar()));
        if (request.getAfpId() != null) {
            Afp afp = afpRepository.findById(request.getAfpId())
                    .orElseThrow(() -> new IllegalArgumentException("AFP inválida"));
            contrato.setAfp(afp);
        }
        contrato.setMetadata(aplicarExtras(contrato, esquemaDe(plantilla), request.getMetadata()));
        Contrato saved = contratoRepository.save(contrato);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public String documento(Long id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ContratoNoEncontradoException("No se encontró el contrato"));
        contrato.getPersona().getNombres();
        if (contrato.getPlantillaContrato() != null) {
            contrato.getPlantillaContrato().getPlantilla();
        }
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

    @Override
    @Transactional(readOnly = true)
    public List<PlantillaContratoDTO> listarPlantillas() {
        return plantillaContratoRepository.findByActivoTrueOrderByIdAsc().stream()
                .map(ContratoServiceImpl::toPlantillaDto)
                .toList();
    }

    @Override
    @Transactional
    public PlantillaContratoDTO crearPlantilla(PlantillaContratoRequest request) {
        String codigo = request.getCodigo().trim().toUpperCase();
        if (plantillaContratoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new PlantillaCodigoDuplicadoException("Ya existe una plantilla con ese código");
        }
        PlantillaContrato plantilla = new PlantillaContrato();
        plantilla.setCodigo(codigo);
        plantilla.setNombre(request.getNombre().trim());
        plantilla.setUso(request.getUso() == null ? UsoPlantilla.CONTRATO : request.getUso());
        plantilla.setPlantilla(request.getPlantilla());
        plantilla.setMetadata(ContratoPlantillaService.esquemaDesdeHtml(request.getPlantilla()));
        plantilla.setActivo(true);
        return toPlantillaDto(plantillaContratoRepository.save(plantilla));
    }

    @Override
    @Transactional(readOnly = true)
    public PlantillaContratoDetalleDTO obtenerPlantilla(Long id) {
        return toPlantillaDetalleDto(buscarPlantilla(id));
    }

    @Override
    @Transactional
    public PlantillaContratoDetalleDTO actualizarPlantilla(Long id, PlantillaContratoUpdateRequest request) {
        if (request.getPin() == null || !request.getPin().trim().equals(plantillasPin)) {
            throw new PlantillaPinInvalidoException();
        }
        PlantillaContrato plantilla = buscarPlantilla(id);
        plantilla.setNombre(request.getNombre().trim());
        plantilla.setPlantilla(request.getPlantilla());
        if (request.getUso() != null) {
            plantilla.setUso(request.getUso());
        }
        plantilla.setMetadata(ContratoPlantillaService.esquemaDesdeHtml(request.getPlantilla()));
        return toPlantillaDetalleDto(plantillaContratoRepository.save(plantilla));
    }

    private PlantillaContrato buscarPlantilla(Long id) {
        return plantillaContratoRepository.findById(id)
                .filter(PlantillaContrato::isActivo)
                .orElseThrow(() -> new PlantillaNoEncontradaException("No se encontró la plantilla"));
    }

    private Map<String, String> aplicarExtras(
            Contrato contrato,
            List<PlantillaCampoEsquema> esquema,
            Map<String, String> enviados) {
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
            String valor = crudo == null ? "" : crudo.trim();
            if (campo.isRequerido() && valor.isEmpty()) {
                throw new IllegalArgumentException("Complete el campo " + labelDe(campo) + ".");
            }
            if (valor.isEmpty()) {
                continue;
            }
            if ("catalogo".equals(campo.getTipo())) {
                valor = resolverCatalogo(contrato, campo.getCatalogo(), valor);
            } else {
                copiarColumna(contrato, clave, valor);
            }
            guardados.put(clave, valor);
        }
        return guardados;
    }

    private String resolverCatalogo(Contrato contrato, String catalogo, String idCrudo) {
        Long id;
        try {
            id = Long.valueOf(idCrudo);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Valor de catálogo inválido");
        }
        if ("jornadas".equals(catalogo)) {
            Jornada jornada = jornadaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Jornada inválida"));
            contrato.setJornada(jornada);
            return jornada.getLabel();
        }
        if ("causasObjetivas".equals(catalogo)) {
            CausaObjetiva causa = causaObjetivaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Causa objetiva inválida"));
            contrato.setCausaObjetiva(causa);
            return causa.getLabel();
        }
        if ("metodosPago".equals(catalogo)) {
            MetodoPago metodoPago = metodoPagoRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Método de pago inválido"));
            contrato.setMetodoPago(metodoPago);
            return metodoPago.getLabel();
        }
        throw new IllegalArgumentException("Catálogo no soportado");
    }

    private static void copiarColumna(Contrato contrato, String clave, String valor) {
        switch (clave) {
            case "cargo" -> contrato.setCargo(valor);
            case "area" -> contrato.setArea(valor);
            case "proyecto" -> contrato.setProyecto(valor);
            case "ubicacion_obra" -> contrato.setUbicacionObra(valor);
            case "ciudad_firma" -> contrato.setCiudadFirma(valor);
            case "numero" -> contrato.setNumero(valor);
            case "observaciones" -> contrato.setObservaciones(valor);
            case "fecha_corte" -> contrato.setFechaCorte(LocalDate.parse(valor));
            default -> {
            }
        }
    }

    private static String labelDe(PlantillaCampoEsquema campo) {
        if (campo.getLabel() == null || campo.getLabel().isBlank()) {
            return campo.getClave();
        }
        return campo.getLabel();
    }

    private static List<PlantillaCampoEsquema> esquemaDe(PlantillaContrato plantilla) {
        List<PlantillaCampoEsquema> guardado = plantilla.getMetadata();
        if (guardado != null && !guardado.isEmpty()) {
            return guardado;
        }
        return ContratoPlantillaService.esquemaDesdeHtml(plantilla.getPlantilla());
    }

    private static PlantillaContratoDTO toPlantillaDto(PlantillaContrato plantilla) {
        List<PlantillaCampoDTO> campos = esquemaDe(plantilla).stream()
                .map(campo -> new PlantillaCampoDTO(
                        campo.getClave(),
                        campo.getLabel(),
                        campo.getTipo(),
                        campo.getCatalogo(),
                        campo.isRequerido()))
                .toList();
        return new PlantillaContratoDTO(
                plantilla.getId(),
                plantilla.getCodigo(),
                plantilla.getNombre(),
                plantilla.getUso(),
                campos);
    }

    private static PlantillaContratoDetalleDTO toPlantillaDetalleDto(PlantillaContrato plantilla) {
        List<PlantillaCampoDTO> campos = esquemaDe(plantilla).stream()
                .map(campo -> new PlantillaCampoDTO(
                        campo.getClave(),
                        campo.getLabel(),
                        campo.getTipo(),
                        campo.getCatalogo(),
                        campo.isRequerido()))
                .toList();
        return new PlantillaContratoDetalleDTO(
                plantilla.getId(),
                plantilla.getCodigo(),
                plantilla.getNombre(),
                plantilla.getUso(),
                plantilla.getPlantilla(),
                campos);
    }

    private static ContratoDTO toDto(Contrato contrato) {
        Persona persona = contrato.getPersona();
        PlantillaContrato plantilla = contrato.getPlantillaContrato();
        Afp afp = contrato.getAfp();
        return ContratoDTO.builder()
                .id(contrato.getId())
                .personaId(persona.getId())
                .personaNombres(persona.getNombres())
                .personaApellidos(persona.getApellidos())
                .numeroDocumento(persona.getNumeroDocumento())
                .estado(contrato.getEstado())
                .fechaInicio(contrato.getFechaInicio())
                .fechaFin(contrato.getFechaFin())
                .cargo(contrato.getCargo())
                .proyecto(contrato.getProyecto())
                .ciudadFirma(contrato.getCiudadFirma())
                .plantillaCodigo(plantilla == null ? null : plantilla.getCodigo())
                .plantillaNombre(plantilla == null ? null : plantilla.getNombre())
                .createdAt(contrato.getCreatedAt())
                .sueldo(contrato.getSueldo())
                .afpId(afp == null ? null : afp.getId())
                .afpCodigo(afp == null ? null : afp.getCodigo())
                .afpNombre(afp == null ? null : afp.getLabel())
                .tieneAsignacionFamiliar(contrato.isTieneAsignacionFamiliar())
                .build();
    }
}
