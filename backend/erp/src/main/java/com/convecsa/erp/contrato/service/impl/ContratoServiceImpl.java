package com.convecsa.erp.contrato.service.impl;

import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.exception.PlantillaCodigoDuplicadoException;
import com.convecsa.erp.common.exception.PlantillaNoEncontradaException;
import com.convecsa.erp.common.exception.PlantillaPinInvalidoException;
import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.contrato.catalogo.CausaObjetiva;
import com.convecsa.erp.contrato.catalogo.CategoriaContrato;
import com.convecsa.erp.contrato.catalogo.Jornada;
import com.convecsa.erp.contrato.catalogo.MetodoPago;
import com.convecsa.erp.contrato.catalogo.PlantillaCampoEsquema;
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
import com.convecsa.erp.contrato.exception.ContratoNoEncontrado;
import com.convecsa.erp.contrato.repository.CausaObjetivaRepository;
import com.convecsa.erp.contrato.repository.CategoriaContratoRepository;
import com.convecsa.erp.contrato.repository.ContratoRepository;
import com.convecsa.erp.contrato.repository.JornadaRepository;
import com.convecsa.erp.contrato.repository.MetodoPagoRepository;
import com.convecsa.erp.contrato.repository.PlantillaContratoRepository;
import com.convecsa.erp.contrato.service.InterfazContratoService;
import com.convecsa.erp.contrato.support.ContratoPlantillaService;
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
    private final CategoriaContratoRepository categoriaContratoRepository;
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
        long conContratoActivo = contratoRepository.countDistinctPersonaIdByActivoTrue();
        long totalPersonas = personaRepository.count();
        long sinContratoActivo = Math.max(0, totalPersonas - conContratoActivo);
        return new ContratosResumeDTO(conContratoActivo, sinContratoActivo);
    }

    @Override
    @Transactional(readOnly = true)
    public ContratosCatalogosDTO catalogos() {
        return new ContratosCatalogosDTO(
                categoriaContratoRepository.findByActivoTrueOrderByIdAsc().stream()
                        .map(item -> new CatalogoItemDTO(item.getId(), item.getCodigo(), item.getLabel()))
                        .toList(),
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
    public PagedResponse<ContratoDTO> listar(int page, int size, Boolean activo) {
        int safePage = Math.max(page, 0);
        int safeSize = size < 1 ? 20 : Math.min(size, 100);
        var pageable = PageRequest.of(safePage, safeSize);
        var resultado = activo == null
                ? contratoRepository.findAllByOrderByIdDesc(pageable)
                : contratoRepository.findByActivoOrderByIdDesc(activo, pageable);
        return PagedResponse.from(resultado.map(ContratoServiceImpl::toDto));
    }

    @Override
    @Transactional
    public ContratoDTO crear(ContratoRequest request) {
        Persona persona = personaRepository.findById(request.getPersonaId())
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
        CategoriaContrato categoria = categoriaContratoRepository.findById(request.getCategoriaContratoId())
                .filter(CategoriaContrato::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("Categoría de contrato inválida"));
        PlantillaContrato plantilla = plantillaContratoRepository.findById(request.getTipoPlantillaId())
                .filter(PlantillaContrato::isActivo)
                .filter(item -> item.getUso() == UsoPlantilla.CONTRATO)
                .orElseThrow(() -> new IllegalArgumentException("Plantilla inválida"));

        Contrato contrato = new Contrato();
        contrato.setTitulo(request.getTitulo().trim());
        contrato.setCategoriaContrato(categoria);
        contrato.setPersona(persona);
        contrato.setActivo(request.getActivo() == null || request.getActivo());
        contrato.setTipoPlantilla(plantilla);
        contrato.setFechaInicio(request.getFechaInicio());
        contrato.setFechaFin(request.getFechaFin());
        contrato.setSueldo(request.getSueldo());
        contrato.setFechaCorte(request.getFechaCorte());
        contrato.setObservaciones(blankToNull(request.getObservaciones()));
        contrato.setMetadata(aplicarExtras(esquemaDe(plantilla), request.getMetadata()));
        Contrato saved = contratoRepository.save(contrato);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public String documento(Long id) {
        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ContratoNoEncontrado("No se encontró el contrato"));
        contrato.getPersona().getNombres();
        if (contrato.getTipoPlantilla() != null) {
            contrato.getTipoPlantilla().getPlantilla();
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

    @Override
    @Transactional(readOnly = true)
    public ContratoDTO obtenerUltimoContrato(Long personaId) {
        return contratoRepository.findUltimoContratoActivoPorPersonaId(personaId)
                .map(ContratoServiceImpl::toDto)
                .orElseThrow(() -> new ContratoNoEncontrado(
                        "No se encontró un contrato activo para la persona"));
    }

    private PlantillaContrato buscarPlantilla(Long id) {
        return plantillaContratoRepository.findById(id)
                .filter(PlantillaContrato::isActivo)
                .orElseThrow(() -> new PlantillaNoEncontradaException("No se encontró la plantilla"));
    }

    private Map<String, String> aplicarExtras(
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
                valor = resolverCatalogo(campo.getCatalogo(), valor);
            }
            guardados.put(clave, valor);
        }
        return guardados;
    }

    private String resolverCatalogo(String catalogo, String idCrudo) {
        Long id;
        try {
            id = Long.valueOf(idCrudo);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Valor de catálogo inválido");
        }
        if ("jornadas".equals(catalogo)) {
            Jornada jornada = jornadaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Jornada inválida"));
            return jornada.getLabel();
        }
        if ("causasObjetivas".equals(catalogo)) {
            CausaObjetiva causa = causaObjetivaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Causa objetiva inválida"));
            return causa.getLabel();
        }
        if ("metodosPago".equals(catalogo)) {
            MetodoPago metodoPago = metodoPagoRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Método de pago inválido"));
            return metodoPago.getLabel();
        }
        throw new IllegalArgumentException("Catálogo no soportado");
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
        PlantillaContrato plantilla = contrato.getTipoPlantilla();
        CategoriaContrato categoria = contrato.getCategoriaContrato();
        return ContratoDTO.builder()
                .id(contrato.getId())
                .titulo(contrato.getTitulo())
                .categoriaContratoId(categoria.getId())
                .categoriaContratoCodigo(categoria.getCodigo())
                .categoriaContratoNombre(categoria.getLabel())
                .personaId(persona.getId())
                .personaNombreCompleto(persona.getApellidos() + ", " + persona.getNombres())
                .personaNumeroDocumento(persona.getNumeroDocumento())
                .fechaInicio(contrato.getFechaInicio())
                .fechaFin(contrato.getFechaFin())
                .sueldo(contrato.getSueldo())
                .activo(contrato.isActivo())
                .fechaCorte(contrato.getFechaCorte())
                .tipoPlantillaId(plantilla.getId())
                .tipoPlantillaCodigo(plantilla.getCodigo())
                .tipoPlantillaNombre(plantilla.getNombre())
                .observaciones(contrato.getObservaciones())
                .metadata(contrato.getMetadata())
                .build();
    }

    private static String blankToNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
