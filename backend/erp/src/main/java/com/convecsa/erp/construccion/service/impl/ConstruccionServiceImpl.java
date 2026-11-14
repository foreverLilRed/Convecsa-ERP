package com.convecsa.erp.construccion.service.impl;

import com.convecsa.erp.common.exception.BoletaCcNoEncontradaException;
import com.convecsa.erp.common.exception.PeriodoCcDuplicadoException;
import com.convecsa.erp.common.exception.PeriodoCcNoEncontradoException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.exception.ProyectoCodigoDuplicadoException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.domain.AccionAuditoriaObra;
import com.convecsa.erp.construccion.domain.CadenciaPeriodoCc;
import com.convecsa.erp.construccion.domain.EstadoObra;
import com.convecsa.erp.construccion.domain.EstadoVersionPresupuesto;
import com.convecsa.erp.construccion.domain.RolResponsableObra;
import com.convecsa.erp.construccion.domain.TipoRecursoApu;
import com.convecsa.erp.construccion.domain.TipoVersionPresupuesto;
import com.convecsa.erp.construccion.domain.CategoriaObrera;
import com.convecsa.erp.construccion.domain.CodigoConceptoCc;
import com.convecsa.erp.construccion.domain.CodigoTareoCc;
import com.convecsa.erp.construccion.domain.EstadoPeriodoCc;
import com.convecsa.erp.construccion.domain.SemestreGrati;
import com.convecsa.erp.construccion.dto.BoletaCcConceptoDTO;
import com.convecsa.erp.construccion.dto.BoletaCcDTO;
import com.convecsa.erp.construccion.dto.BoletaCcExtrasRequest;
import com.convecsa.erp.construccion.dto.ApuPartidaDTO;
import com.convecsa.erp.construccion.dto.ApuPartidaRequest;
import com.convecsa.erp.construccion.dto.BoletaCcResumenDTO;
import com.convecsa.erp.construccion.dto.ComparativoCentroDTO;
import com.convecsa.erp.construccion.dto.ComparativoCuadrillaDTO;
import com.convecsa.erp.construccion.dto.ComparativoPresupuestoDTO;
import com.convecsa.erp.construccion.dto.ConstruccionCatalogosDTO;
import com.convecsa.erp.construccion.dto.PartidaPresupuestoDTO;
import com.convecsa.erp.construccion.dto.PartidaPresupuestoRequest;
import com.convecsa.erp.construccion.dto.PeriodoCcDTO;
import com.convecsa.erp.construccion.dto.PeriodoCcRequest;
import com.convecsa.erp.construccion.dto.PersonalObraDTO;
import com.convecsa.erp.construccion.dto.PresupuestoDTO;
import com.convecsa.erp.construccion.dto.PresupuestoVersionDTO;
import com.convecsa.erp.construccion.dto.PresupuestoVersionRequest;
import com.convecsa.erp.construccion.dto.PresupuestoVersionResumenDTO;
import com.convecsa.erp.construccion.dto.ProyectoDTO;
import com.convecsa.erp.construccion.dto.ProyectoRequest;
import com.convecsa.erp.construccion.dto.ProyectoResponsableRequest;
import com.convecsa.erp.construccion.dto.ResponsableObraDTO;
import com.convecsa.erp.construccion.dto.TareoCcDiaDTO;
import com.convecsa.erp.construccion.dto.TareoCcGrillaDTO;
import com.convecsa.erp.construccion.dto.TareoCcGuardarRequest;
import com.convecsa.erp.construccion.entity.BoletaCc;
import com.convecsa.erp.construccion.entity.BoletaCcConcepto;
import com.convecsa.erp.construccion.entity.BoletaCcEnvioCorreo;
import com.convecsa.erp.construccion.entity.CcCategoriaJornal;
import com.convecsa.erp.construccion.entity.CcEspecialidadBae;
import com.convecsa.erp.construccion.entity.CcParametroAnual;
import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.entity.CuadrillaMiembro;
import com.convecsa.erp.construccion.entity.CuadrillaObra;
import com.convecsa.erp.construccion.entity.ApuPartida;
import com.convecsa.erp.construccion.entity.ImputacionEquipo;
import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.SubcontratoAvance;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import com.convecsa.erp.construccion.entity.PresupuestoVersion;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.ProyectoResponsable;
import com.convecsa.erp.construccion.entity.TareoCcDia;
import com.convecsa.erp.construccion.entity.ValeSalidaItem;
import com.convecsa.erp.construccion.repository.BoletaCcEnvioCorreoRepository;
import com.convecsa.erp.construccion.repository.BoletaCcRepository;
import com.convecsa.erp.construccion.repository.CcParametroAnualRepository;
import com.convecsa.erp.construccion.repository.ContratoCcRepository;
import com.convecsa.erp.construccion.repository.CuadrillaMiembroRepository;
import com.convecsa.erp.construccion.repository.CuadrillaObraRepository;
import com.convecsa.erp.construccion.repository.PartidaPresupuestoRepository;
import com.convecsa.erp.construccion.repository.PeriodoCcRepository;
import com.convecsa.erp.construccion.repository.PresupuestoVersionRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ProyectoResponsableRepository;
import com.convecsa.erp.construccion.repository.ImputacionEquipoRepository;
import com.convecsa.erp.construccion.repository.SubcontratoAvanceRepository;
import com.convecsa.erp.construccion.repository.TareoCcDiaRepository;
import com.convecsa.erp.construccion.repository.ValeSalidaItemRepository;
import com.convecsa.erp.construccion.service.InterfazConstruccionService;
import com.convecsa.erp.construccion.support.BoletaCcPlantillaService;
import com.convecsa.erp.construccion.support.CalculoNominaCcService;
import com.convecsa.erp.construccion.support.CcLiquidacionInput;
import com.convecsa.erp.construccion.support.CcLiquidacionResult;
import com.convecsa.erp.construccion.support.CcParametros;
import com.convecsa.erp.construccion.support.DominicalCc;
import com.convecsa.erp.construccion.support.RentaQuintaProyeccionService;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
import com.convecsa.erp.construccion.support.PresupuestoExcelExporter;
import com.convecsa.erp.construccion.support.PresupuestoExcelParser;
import com.convecsa.erp.construccion.support.UitObraAlerta;
import com.convecsa.erp.contrato.dto.CatalogoItemDTO;
import com.convecsa.erp.contrato.entity.Contrato;
import com.convecsa.erp.nomina.catalogo.Afp;
import com.convecsa.erp.nomina.declaracion.domain.BancoDispersion;
import com.convecsa.erp.nomina.declaracion.dto.ArchivoDescargaDTO;
import com.convecsa.erp.nomina.declaracion.support.DispersionArchivoService;
import com.convecsa.erp.nomina.domain.CategoriaConcepto;
import com.convecsa.erp.nomina.domain.OrigenConcepto;
import com.convecsa.erp.nomina.domain.TipoComisionAfp;
import com.convecsa.erp.nomina.dto.BoletaConceptoExtraRequest;
import com.convecsa.erp.nomina.dto.BoletaEnvioResumenDTO;
import com.convecsa.erp.nomina.dto.BoletaLoteRequest;
import com.convecsa.erp.nomina.entity.AfpTasa;
import com.convecsa.erp.nomina.entity.NominaDocumentoConfiguracion;
import com.convecsa.erp.nomina.repository.AfpRepository;
import com.convecsa.erp.nomina.repository.AfpTasaRepository;
import com.convecsa.erp.nomina.repository.NominaDocumentoConfiguracionRepository;
import com.convecsa.erp.nomina.support.BoletaCorreoService;
import com.convecsa.erp.nomina.support.BoletaPdfService;
import com.convecsa.erp.onboarding.support.OnboardingHtmlPacket;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ConstruccionServiceImpl implements InterfazConstruccionService {

    private static final BigDecimal CUOTA_SINDICAL_DEFAULT = new BigDecimal("40.00");

    private final ProyectoRepository proyectoRepository;
    private final ProyectoResponsableRepository proyectoResponsableRepository;
    private final PresupuestoVersionRepository presupuestoVersionRepository;
    private final PartidaPresupuestoRepository partidaPresupuestoRepository;
    private final PersonaRepository personaRepository;
    private final CcParametroAnualRepository parametroRepository;
    private final ContratoCcRepository contratoCcRepository;
    private final PeriodoCcRepository periodoCcRepository;
    private final TareoCcDiaRepository tareoCcDiaRepository;
    private final BoletaCcRepository boletaCcRepository;
    private final ValeSalidaItemRepository valeSalidaItemRepository;
    private final PresupuestoExcelParser presupuestoExcelParser;
    private final PresupuestoExcelExporter presupuestoExcelExporter;
    private final ImputacionEquipoRepository imputacionEquipoRepository;
    private final SubcontratoAvanceRepository subcontratoAvanceRepository;
    private final CuadrillaObraRepository cuadrillaObraRepository;
    private final CuadrillaMiembroRepository cuadrillaMiembroRepository;
    private final AuditoriaObraRegistrar auditoriaObraRegistrar;
    private final BoletaCcEnvioCorreoRepository boletaCcEnvioCorreoRepository;
    private final CalculoNominaCcService calculoNominaCcService;
    private final RentaQuintaProyeccionService rentaQuintaProyeccionService;
    private final BoletaCcPlantillaService boletaCcPlantillaService;
    private final BoletaPdfService boletaPdfService;
    private final BoletaCorreoService boletaCorreoService;
    private final DispersionArchivoService dispersionArchivoService;
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
        aplicarCampos(proyecto, request, codigo);
        return toProyectoDto(proyectoRepository.save(proyecto), parametrosVigentes(LocalDate.now().getYear()));
    }

    @Override
    @Transactional
    public ProyectoDTO actualizarProyecto(Long id, ProyectoRequest request) {
        Proyecto proyecto = buscarProyecto(id);
        String codigo = request.getCodigo().trim().toUpperCase();
        if (proyectoRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new ProyectoCodigoDuplicadoException("Ya existe un proyecto con el código " + codigo);
        }
        aplicarCampos(proyecto, request, codigo);
        return toProyectoDto(proyectoRepository.save(proyecto), parametrosVigentes(LocalDate.now().getYear()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponsableObraDTO> listarResponsables(Long proyectoId) {
        buscarProyecto(proyectoId);
        return proyectoResponsableRepository.findByProyectoIdOrderByRolAsc(proyectoId).stream()
                .map(ConstruccionServiceImpl::toResponsableDto)
                .toList();
    }

    @Override
    @Transactional
    public List<ResponsableObraDTO> guardarResponsables(Long proyectoId, List<ProyectoResponsableRequest> request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        List<ProyectoResponsableRequest> filas = request == null ? List.of() : request;
        Set<RolResponsableObra> vistos = new HashSet<>();
        for (ProyectoResponsableRequest fila : filas) {
            if (fila.getRol() == null || fila.getPersonaId() == null) {
                throw new IllegalArgumentException("Cada responsable requiere persona y rol");
            }
            if (!vistos.add(fila.getRol())) {
                throw new IllegalArgumentException("Hay roles de responsable duplicados");
            }
        }
        List<ProyectoResponsable> actuales =
                proyectoResponsableRepository.findByProyectoIdOrderByRolAsc(proyectoId);
        if (!actuales.isEmpty()) {
            proyectoResponsableRepository.deleteAll(actuales);
            proyectoResponsableRepository.flush();
        }
        for (ProyectoResponsableRequest fila : filas) {
            Persona persona = personaRepository.findById(fila.getPersonaId())
                    .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
            ProyectoResponsable responsable = new ProyectoResponsable();
            responsable.setProyecto(proyecto);
            responsable.setPersona(persona);
            responsable.setRol(fila.getRol());
            proyectoResponsableRepository.save(responsable);
        }
        return listarResponsables(proyectoId);
    }

    @Override
    @Transactional(readOnly = true)
    public PresupuestoDTO obtenerPresupuesto(Long proyectoId, Long versionId) {
        buscarProyecto(proyectoId);
        List<PresupuestoVersion> versiones = presupuestoVersionRepository.findByProyectoIdOrderByNumeroDesc(proyectoId);
        List<PresupuestoVersionResumenDTO> resumen = versiones.stream()
                .map(this::toVersionResumen)
                .toList();
        if (versiones.isEmpty()) {
            return PresupuestoDTO.builder().versiones(resumen).version(null).build();
        }
        PresupuestoVersion seleccionada = resolverVersion(proyectoId, versionId, versiones);
        return PresupuestoDTO.builder()
                .versiones(resumen)
                .version(toVersionDto(seleccionada))
                .build();
    }

    @Override
    @Transactional
    public PresupuestoDTO crearVersionPresupuesto(Long proyectoId, PresupuestoVersionRequest request) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        if (presupuestoVersionRepository.existsByProyectoIdAndEstado(proyectoId, EstadoVersionPresupuesto.BORRADOR)) {
            throw new IllegalArgumentException("Ya existe un presupuesto en borrador");
        }
        List<PresupuestoVersion> existentes = presupuestoVersionRepository.findByProyectoIdOrderByNumeroDesc(proyectoId);
        PresupuestoVersion version = new PresupuestoVersion();
        version.setProyecto(proyecto);
        version.setEstado(EstadoVersionPresupuesto.BORRADOR);
        if (existentes.isEmpty()) {
            version.setTipo(TipoVersionPresupuesto.INICIAL);
            version.setNumero(1);
        } else {
            PresupuestoVersion vigente = presupuestoVersionRepository
                    .findByProyectoIdAndEstado(proyectoId, EstadoVersionPresupuesto.VIGENTE)
                    .orElseThrow(() -> new IllegalArgumentException("No hay un presupuesto vigente para copiar"));
            TipoVersionPresupuesto tipo = request == null || request.getTipo() == null
                    ? TipoVersionPresupuesto.ACTUALIZADO
                    : request.getTipo();
            if (tipo == TipoVersionPresupuesto.INICIAL) {
                throw new IllegalArgumentException("Ya existe un presupuesto inicial");
            }
            version.setTipo(tipo);
            version.setNumero(existentes.get(0).getNumero() + 1);
            copiarPartidas(vigente, version);
        }
        presupuestoVersionRepository.save(version);
        auditoriaObraRegistrar.registrar(
                proyectoId,
                AccionAuditoriaObra.CREAR_VERSION_PRESUPUESTO,
                AuditoriaObraRegistrar.ENTIDAD_PRESUPUESTO,
                version.getId(),
                "v" + version.getNumero() + " " + version.getTipo());
        return obtenerPresupuesto(proyectoId, version.getId());
    }

    @Override
    @Transactional
    public PresupuestoDTO guardarPartidasPresupuesto(
            Long proyectoId, Long versionId, List<PartidaPresupuestoRequest> request) {
        PresupuestoVersion version = buscarVersion(proyectoId, versionId);
        asegurarBorrador(version);
        List<PartidaPresupuestoRequest> filas = persistirPartidas(version, request);
        auditoriaObraRegistrar.registrar(
                proyectoId,
                AccionAuditoriaObra.GUARDAR_PARTIDAS,
                AuditoriaObraRegistrar.ENTIDAD_PRESUPUESTO,
                versionId,
                "v" + version.getNumero() + ", " + filas.size() + " partidas");
        return obtenerPresupuesto(proyectoId, versionId);
    }

    @Override
    @Transactional
    public PresupuestoDTO vigentarPresupuesto(Long proyectoId, Long versionId) {
        PresupuestoVersion version = buscarVersion(proyectoId, versionId);
        asegurarBorrador(version);
        if (partidaPresupuestoRepository.countByVersionId(versionId) == 0) {
            throw new IllegalArgumentException("No se puede vigentar un presupuesto sin partidas");
        }
        presupuestoVersionRepository
                .findByProyectoIdAndEstado(proyectoId, EstadoVersionPresupuesto.VIGENTE)
                .ifPresent(vigente -> vigente.setEstado(EstadoVersionPresupuesto.HISTORICO));
        version.setEstado(EstadoVersionPresupuesto.VIGENTE);
        presupuestoVersionRepository.save(version);
        auditoriaObraRegistrar.registrar(
                proyectoId,
                AccionAuditoriaObra.VIGENTAR_PRESUPUESTO,
                AuditoriaObraRegistrar.ENTIDAD_PRESUPUESTO,
                versionId,
                "v" + version.getNumero());
        return obtenerPresupuesto(proyectoId, versionId);
    }

    @Override
    @Transactional
    public PresupuestoDTO importarPresupuesto(Long proyectoId, Long versionId, MultipartFile archivo) {
        PresupuestoVersion version = buscarVersion(proyectoId, versionId);
        asegurarBorrador(version);
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Adjunte un archivo Excel de presupuesto");
        }
        byte[] contenido;
        try {
            contenido = archivo.getBytes();
        } catch (IOException exception) {
            throw new IllegalArgumentException("No se pudo leer el archivo Excel");
        }
        List<PartidaPresupuestoRequest> filas = presupuestoExcelParser.parse(contenido, archivo.getOriginalFilename());
        persistirPartidas(version, filas);
        auditoriaObraRegistrar.registrar(
                proyectoId,
                AccionAuditoriaObra.IMPORTAR_PRESUPUESTO,
                AuditoriaObraRegistrar.ENTIDAD_PRESUPUESTO,
                versionId,
                (archivo.getOriginalFilename() == null ? "excel" : archivo.getOriginalFilename())
                        + ", " + filas.size() + " partidas");
        return obtenerPresupuesto(proyectoId, versionId);
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoDescargaDTO plantillaPresupuesto(Long proyectoId, Long versionId) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        List<PartidaPresupuestoDTO> partidas = List.of();
        String filename = "presupuesto-plantilla.xlsx";
        if (versionId != null) {
            PresupuestoVersion version = buscarVersion(proyectoId, versionId);
            partidas = toVersionDto(version).getPartidas();
            String codigo = proyecto.getCodigo() == null ? "obra" : proyecto.getCodigo();
            filename = "presupuesto-" + codigo + "-v" + version.getNumero() + ".xlsx";
        }
        return new ArchivoDescargaDTO(
                filename,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                presupuestoExcelExporter.exportar(partidas));
    }

    @Override
    @Transactional(readOnly = true)
    public ComparativoPresupuestoDTO comparativoPresupuesto(Long proyectoId) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        PresupuestoVersion vigente = presupuestoVersionRepository
                .findByProyectoIdAndEstado(proyectoId, EstadoVersionPresupuesto.VIGENTE)
                .orElse(null);
        Map<String, BigDecimal> metaPorCentro = new LinkedHashMap<>();
        Map<String, BigDecimal> metaMatPorCentro = new LinkedHashMap<>();
        Map<String, BigDecimal> metaEqPorCentro = new LinkedHashMap<>();
        Map<String, BigDecimal> metaSubPorCentro = new LinkedHashMap<>();
        if (vigente != null) {
            for (PartidaPresupuesto partida : partidaPresupuestoRepository.findByVersionIdOrderByOrdenAsc(vigente.getId())) {
                String centro = centroDe(partida.getCentroCosto(), proyecto.getCentroCosto());
                BigDecimal moUnit = partida.getApu().stream()
                        .filter(item -> item.getTipo() == TipoRecursoApu.MANO_OBRA)
                        .map(item -> nz(item.getParcial()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal matUnit = partida.getApu().stream()
                        .filter(item -> item.getTipo() == TipoRecursoApu.MATERIAL)
                        .map(item -> nz(item.getParcial()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal eqUnit = partida.getApu().stream()
                        .filter(item -> item.getTipo() == TipoRecursoApu.EQUIPO)
                        .map(item -> nz(item.getParcial()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal subUnit = partida.getApu().stream()
                        .filter(item -> item.getTipo() == TipoRecursoApu.SUBCONTRATO)
                        .map(item -> nz(item.getParcial()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                acumular(metaPorCentro, centro, money(moUnit.multiply(nz(partida.getMetrado()))));
                acumular(metaMatPorCentro, centro, money(matUnit.multiply(nz(partida.getMetrado()))));
                acumular(metaEqPorCentro, centro, money(eqUnit.multiply(nz(partida.getMetrado()))));
                acumular(metaSubPorCentro, centro, money(subUnit.multiply(nz(partida.getMetrado()))));
            }
        }
        Map<String, BigDecimal> realPorCentro = new LinkedHashMap<>();
        Map<Long, BigDecimal> realMoPorCuadrilla = new LinkedHashMap<>();
        BigDecimal realMoSinCuadrilla = BigDecimal.ZERO;
        List<CuadrillaObra> cuadrillasObra = cuadrillaObraRepository.findByProyectoIdOrderByCodigoAsc(proyectoId);
        List<CuadrillaMiembro> miembrosCuadrilla = cuadrillaMiembroRepository.findByProyectoId(proyectoId);
        Map<Long, CuadrillaObra> cuadrillaPorPersona = new HashMap<>();
        Map<Long, Integer> miembrosPorCuadrilla = new HashMap<>();
        for (CuadrillaMiembro miembro : miembrosCuadrilla) {
            Persona personaMiembro = miembro.getPersona();
            if (personaMiembro == null || personaMiembro.getId() == null) {
                continue;
            }
            CuadrillaObra cuadrilla = miembro.getCuadrilla();
            cuadrillaPorPersona.put(personaMiembro.getId(), cuadrilla);
            if (cuadrilla != null && cuadrilla.getId() != null) {
                miembrosPorCuadrilla.merge(cuadrilla.getId(), 1, Integer::sum);
            }
        }
        List<PeriodoCc> periodos = periodoCcRepository.findByProyectoIdOrderByFechaDesdeDesc(proyectoId);
        if (!periodos.isEmpty()) {
            List<Long> periodoIds = periodos.stream().map(PeriodoCc::getId).toList();
            for (BoletaCc boleta : boletaCcRepository.findByPeriodoIdIn(periodoIds)) {
                String centro = contratoCcRepository.findByContratoId(boleta.getContrato().getId())
                        .map(cc -> centroDe(cc.getCentroCosto(), proyecto.getCentroCosto()))
                        .orElseGet(() -> centroDe(null, proyecto.getCentroCosto()));
                BigDecimal costo = nz(boleta.getTotalHaberes()).add(nz(boleta.getTotalAportesEmpleador()));
                acumular(realPorCentro, centro, money(costo));
                Persona personaBoleta = boleta.getPersona();
                if (personaBoleta == null && boleta.getContrato() != null) {
                    personaBoleta = boleta.getContrato().getPersona();
                }
                CuadrillaObra cuadrilla = personaBoleta == null || personaBoleta.getId() == null
                        ? null
                        : cuadrillaPorPersona.get(personaBoleta.getId());
                if (cuadrilla == null || cuadrilla.getId() == null) {
                    realMoSinCuadrilla = realMoSinCuadrilla.add(money(costo));
                } else {
                    realMoPorCuadrilla.merge(cuadrilla.getId(), money(costo), BigDecimal::add);
                }
            }
        }
        Map<String, BigDecimal> realMatPorCentro = new LinkedHashMap<>();
        for (ValeSalidaItem item : valeSalidaItemRepository.findByValeAlmacenProyectoId(proyectoId)) {
            String centro = centroDe(item.getVale().getCentroCosto(), proyecto.getCentroCosto());
            acumular(realMatPorCentro, centro, money(item.getCostoTotal()));
        }
        Map<String, BigDecimal> realEqPorCentro = new LinkedHashMap<>();
        for (ImputacionEquipo imputacion : imputacionEquipoRepository.findByProyectoId(proyectoId)) {
            String centro = centroDe(imputacion.getCentroCosto(), proyecto.getCentroCosto());
            acumular(realEqPorCentro, centro, money(imputacion.getCostoTotal()));
        }
        Map<String, BigDecimal> realSubPorCentro = new LinkedHashMap<>();
        for (SubcontratoAvance avance : subcontratoAvanceRepository.findByProyectoId(proyectoId)) {
            String centro = centroDe(avance.getCentroCosto(), proyecto.getCentroCosto());
            acumular(realSubPorCentro, centro, money(avance.getMonto()));
        }
        List<String> centros = new ArrayList<>();
        for (Map<String, BigDecimal> mapa : List.of(
                metaPorCentro, realPorCentro, metaMatPorCentro, realMatPorCentro,
                metaEqPorCentro, realEqPorCentro, metaSubPorCentro, realSubPorCentro)) {
            for (String clave : mapa.keySet()) {
                if (!centros.contains(clave)) {
                    centros.add(clave);
                }
            }
        }
        List<ComparativoCentroDTO> filas = centros.stream()
                .map(centro -> {
                    BigDecimal meta = money(metaPorCentro.getOrDefault(centro, BigDecimal.ZERO));
                    BigDecimal real = money(realPorCentro.getOrDefault(centro, BigDecimal.ZERO));
                    BigDecimal metaMat = money(metaMatPorCentro.getOrDefault(centro, BigDecimal.ZERO));
                    BigDecimal realMat = money(realMatPorCentro.getOrDefault(centro, BigDecimal.ZERO));
                    BigDecimal metaEq = money(metaEqPorCentro.getOrDefault(centro, BigDecimal.ZERO));
                    BigDecimal realEq = money(realEqPorCentro.getOrDefault(centro, BigDecimal.ZERO));
                    BigDecimal metaSub = money(metaSubPorCentro.getOrDefault(centro, BigDecimal.ZERO));
                    BigDecimal realSub = money(realSubPorCentro.getOrDefault(centro, BigDecimal.ZERO));
                    return ComparativoCentroDTO.builder()
                            .centroCosto(centro)
                            .metaMo(meta)
                            .realMo(real)
                            .desvio(money(real.subtract(meta)))
                            .metaMaterial(metaMat)
                            .realMaterial(realMat)
                            .desvioMaterial(money(realMat.subtract(metaMat)))
                            .metaEquipo(metaEq)
                            .realEquipo(realEq)
                            .desvioEquipo(money(realEq.subtract(metaEq)))
                            .metaSubcontrato(metaSub)
                            .realSubcontrato(realSub)
                            .desvioSubcontrato(money(realSub.subtract(metaSub)))
                            .build();
                })
                .toList();
        BigDecimal metaMo = filas.stream().map(ComparativoCentroDTO::getMetaMo).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal realMo = filas.stream().map(ComparativoCentroDTO::getRealMo).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal desvio = money(realMo.subtract(metaMo));
        BigDecimal desvioPct = metaMo.signum() == 0
                ? BigDecimal.ZERO
                : desvio.multiply(new BigDecimal("100")).divide(metaMo, 2, RoundingMode.HALF_UP);
        BigDecimal metaMaterial = filas.stream().map(ComparativoCentroDTO::getMetaMaterial).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal realMaterial = filas.stream().map(ComparativoCentroDTO::getRealMaterial).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal desvioMaterial = money(realMaterial.subtract(metaMaterial));
        BigDecimal desvioPctMaterial = metaMaterial.signum() == 0
                ? BigDecimal.ZERO
                : desvioMaterial.multiply(new BigDecimal("100")).divide(metaMaterial, 2, RoundingMode.HALF_UP);
        BigDecimal metaEquipo = filas.stream().map(ComparativoCentroDTO::getMetaEquipo).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal realEquipo = filas.stream().map(ComparativoCentroDTO::getRealEquipo).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal desvioEquipo = money(realEquipo.subtract(metaEquipo));
        BigDecimal desvioPctEquipo = metaEquipo.signum() == 0
                ? BigDecimal.ZERO
                : desvioEquipo.multiply(new BigDecimal("100")).divide(metaEquipo, 2, RoundingMode.HALF_UP);
        BigDecimal metaSubcontrato = filas.stream().map(ComparativoCentroDTO::getMetaSubcontrato).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal realSubcontrato = filas.stream().map(ComparativoCentroDTO::getRealSubcontrato).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal desvioSubcontrato = money(realSubcontrato.subtract(metaSubcontrato));
        BigDecimal desvioPctSubcontrato = metaSubcontrato.signum() == 0
                ? BigDecimal.ZERO
                : desvioSubcontrato.multiply(new BigDecimal("100")).divide(metaSubcontrato, 2, RoundingMode.HALF_UP);
        List<ComparativoCuadrillaDTO> filasCuadrilla = new ArrayList<>();
        for (CuadrillaObra cuadrilla : cuadrillasObra) {
            filasCuadrilla.add(ComparativoCuadrillaDTO.builder()
                    .cuadrillaId(cuadrilla.getId())
                    .codigo(cuadrilla.getCodigo())
                    .nombre(cuadrilla.getNombre())
                    .centroCosto(cuadrilla.getCentroCosto())
                    .realMo(money(realMoPorCuadrilla.getOrDefault(cuadrilla.getId(), BigDecimal.ZERO)))
                    .miembros(miembrosPorCuadrilla.getOrDefault(cuadrilla.getId(), 0))
                    .build());
        }
        if (realMoSinCuadrilla.signum() > 0) {
            filasCuadrilla.add(ComparativoCuadrillaDTO.builder()
                    .cuadrillaId(null)
                    .codigo(null)
                    .nombre("Sin cuadrilla")
                    .centroCosto(null)
                    .realMo(money(realMoSinCuadrilla))
                    .miembros(0)
                    .build());
        }
        return ComparativoPresupuestoDTO.builder()
                .metaMo(money(metaMo))
                .realMo(money(realMo))
                .desvio(desvio)
                .desvioPct(desvioPct)
                .metaMaterial(money(metaMaterial))
                .realMaterial(money(realMaterial))
                .desvioMaterial(desvioMaterial)
                .desvioPctMaterial(desvioPctMaterial)
                .metaEquipo(money(metaEquipo))
                .realEquipo(money(realEquipo))
                .desvioEquipo(desvioEquipo)
                .desvioPctEquipo(desvioPctEquipo)
                .metaSubcontrato(money(metaSubcontrato))
                .realSubcontrato(money(realSubcontrato))
                .desvioSubcontrato(desvioSubcontrato)
                .desvioPctSubcontrato(desvioPctSubcontrato)
                .centros(filas)
                .cuadrillas(filasCuadrilla)
                .build();
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
        periodo.setCadencia(request.getCadencia() == null ? CadenciaPeriodoCc.SEMANAL : request.getCadencia());
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
        int filas = request.getDias() == null ? 0 : request.getDias().size();
        auditoriaObraRegistrar.registrar(
                proyectoId,
                AccionAuditoriaObra.GUARDAR_TAREO,
                AuditoriaObraRegistrar.ENTIDAD_TAREO,
                periodoId,
                periodo.getFechaDesde() + "-" + periodo.getFechaHasta() + ", " + filas + " filas");
        return armarGrilla(periodo);
    }

    @Override
    @Transactional
    public List<BoletaCcResumenDTO> liquidar(Long proyectoId, Long periodoId) {
        PeriodoCc periodo = buscarPeriodo(proyectoId, periodoId);
        exigirBorrador(periodo);
        asegurarTareoInicial(periodo);
        CcParametros parametros = toParametros(parametrosVigentes(periodo.getFechaHasta().getYear()))
                .toBuilder()
                .sctrSalud(nz(periodo.getProyecto().getTasaSctrSalud()))
                .sctrPension(nz(periodo.getProyecto().getTasaSctrPension()))
                .build();
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
            List<BoletaCcConcepto> manuals = copiarManuales(boleta);
            boleta.setPeriodo(periodo);
            boleta.setPersona(contratoCc.getContrato().getPersona());
            boleta.setContrato(contratoCc.getContrato());
            boleta.setTarifaDia(resultado.getTarifaDia());
            boleta.setTarifaHora(resultado.getTarifaHora());
            List<BoletaCcConcepto> conceptos = new ArrayList<>();
            resultado.getConceptos().forEach(item -> {
                BoletaCcConcepto concepto = new BoletaCcConcepto();
                concepto.setCategoria(item.categoria());
                concepto.setCodigo(item.codigo());
                concepto.setLabel(item.label());
                concepto.setMonto(item.monto());
                concepto.setOrigen(OrigenConcepto.CALCULADO);
                conceptos.add(concepto);
            });
            conceptos.addAll(manuals);
            boleta.reemplazarConceptos(conceptos);
            aplicarTotales(boleta, resultado);
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
        boleta.getPersona().getCuspp();
        boleta.getContrato().getFechaInicio();
        if (boleta.getContrato().getConstruccionCivil() != null) {
            boleta.getContrato().getConstruccionCivil().getCategoria();
        }
        boleta.getConceptos().size();
        NominaDocumentoConfiguracion configuracion = documentoConfiguracionRepository
                .findById(NominaDocumentoConfiguracion.ID_UNICO)
                .orElse(null);
        List<TareoCcDia> dias = tareoCcDiaRepository.findByPeriodoIdAndPersonaIdOrderByFechaAsc(
                periodoId, boleta.getPersona().getId());
        BigDecimal[] tarifas = tarifasDe(boleta);
        return OnboardingHtmlPacket.conEstiloImpresion(
                boletaCcPlantillaService.render(boleta, configuracion, dias, tarifas[0], tarifas[1]));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] documentoBoletaPdf(Long proyectoId, Long periodoId, Long boletaId) {
        return boletaPdfService.convertir(documentoBoletaHtml(proyectoId, periodoId, boletaId));
    }

    @Override
    @Transactional
    public BoletaCcDTO guardarExtras(
            Long proyectoId, Long periodoId, Long boletaId, BoletaCcExtrasRequest request) {
        PeriodoCc periodo = buscarPeriodo(proyectoId, periodoId);
        exigirBorrador(periodo);
        BoletaCc boleta = buscarBoleta(proyectoId, periodoId, boletaId);
        boleta.getConceptos().removeIf(item -> item.getOrigen() == OrigenConcepto.MANUAL);
        List<BoletaConceptoExtraRequest> extras = request == null || request.getExtras() == null
                ? List.of()
                : request.getExtras();
        int orden = boleta.getConceptos().size();
        for (BoletaConceptoExtraRequest extra : extras) {
            if (extra.getCategoria() == null || extra.getLabel() == null || extra.getLabel().isBlank()) {
                throw new IllegalArgumentException("Cada reintegro requiere categoría y nombre");
            }
            if (extra.getCategoria() == CategoriaConcepto.APORTE_EMPLEADOR) {
                throw new IllegalArgumentException("Los reintegros no pueden ser aportes del empleador");
            }
            BoletaCcConcepto concepto = new BoletaCcConcepto();
            concepto.setBoleta(boleta);
            concepto.setCategoria(extra.getCategoria());
            concepto.setCodigo(extra.getCategoria() == CategoriaConcepto.DESCUENTO
                    ? CodigoConceptoCc.DESCUENTO_MANUAL
                    : CodigoConceptoCc.REINTEGRO);
            concepto.setLabel(extra.getLabel().trim());
            concepto.setMonto(nz(extra.getMonto()));
            concepto.setOrigen(OrigenConcepto.MANUAL);
            concepto.setOrden(orden++);
            boleta.getConceptos().add(concepto);
        }
        recalcularTotalesDesdeConceptos(boleta);
        return toBoletaDto(boletaCcRepository.save(boleta));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] documentoBoletasPdf(Long proyectoId, Long periodoId, BoletaLoteRequest request) {
        List<BoletaCc> boletas = cargarBoletasLote(proyectoId, periodoId, request);
        return boletaPdfService.combinar(boletas.stream()
                .map(boleta -> boletaPdfService.convertir(
                        documentoBoletaHtml(proyectoId, periodoId, boleta.getId())))
                .toList());
    }

    @Override
    @Transactional
    public BoletaEnvioResumenDTO enviarBoletas(Long proyectoId, Long periodoId, BoletaLoteRequest request) {
        PeriodoCc periodo = buscarPeriodo(proyectoId, periodoId);
        List<BoletaCc> boletas = cargarBoletasLote(proyectoId, periodoId, request);
        NominaDocumentoConfiguracion configuracion = documentoConfiguracionRepository
                .findById(NominaDocumentoConfiguracion.ID_UNICO)
                .orElse(null);
        String empresa = configuracion == null || configuracion.getRazonSocial() == null
                ? "CONVECSA S.A.C."
                : configuracion.getRazonSocial();
        int anio = periodo.getFechaHasta().getYear();
        int mes = periodo.getFechaHasta().getMonthValue();
        List<String> detalles = new ArrayList<>();
        int enviados = 0;
        int errores = 0;
        for (BoletaCc boleta : boletas) {
            Persona persona = boleta.getPersona();
            String destinatario = persona.getCorreo() == null || persona.getCorreo().isBlank()
                    ? null
                    : persona.getCorreo().trim();
            String nombre = persona.getNombres() + " " + persona.getApellidos();
            BoletaCcEnvioCorreo auditoria = new BoletaCcEnvioCorreo();
            auditoria.setBoleta(boleta);
            auditoria.setDestinatario(destinatario == null ? "SIN_CORREO" : destinatario);
            try {
                if (destinatario == null) {
                    throw new IllegalArgumentException("La persona no tiene correo registrado");
                }
                byte[] pdf = documentoBoletaPdf(proyectoId, periodoId, boleta.getId());
                boletaCorreoService.enviar(
                        destinatario,
                        nombre,
                        empresa,
                        anio,
                        mes,
                        "boleta-rcc-" + boleta.getId() + ".pdf",
                        pdf);
                auditoria.setEstado("ENVIADO");
                enviados++;
            } catch (Exception ex) {
                auditoria.setEstado("ERROR");
                auditoria.setDetalle(ex.getMessage() == null ? "Error de envío" : ex.getMessage());
                if (auditoria.getDetalle().length() > 1000) {
                    auditoria.setDetalle(auditoria.getDetalle().substring(0, 1000));
                }
                detalles.add(nombre + ": " + auditoria.getDetalle());
                errores++;
            }
            boletaCcEnvioCorreoRepository.save(auditoria);
        }
        return new BoletaEnvioResumenDTO(enviados, errores, detalles);
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoDescargaDTO descargarDispersion(Long proyectoId, Long periodoId, BancoDispersion banco) {
        PeriodoCc periodo = buscarPeriodo(proyectoId, periodoId);
        List<BoletaCc> boletas = boletaCcRepository.findByPeriodoIdOrderByIdAsc(periodoId);
        if (boletas.isEmpty()) {
            throw new IllegalArgumentException("Liquide la planilla antes de generar la dispersión");
        }
        for (BoletaCc boleta : boletas) {
            boleta.getPersona().getCuentasBancarias().size();
        }
        BancoDispersion formato = banco == null ? BancoDispersion.CCI : banco;
        String contenido = dispersionArchivoService.generarBoletas(boletas, formato);
        String nombre = "dispersion-" + periodo.getProyecto().getCodigo() + "-"
                + periodo.getFechaDesde() + "-" + periodo.getFechaHasta() + "-"
                + formato.name().toLowerCase() + ".txt";
        return new ArchivoDescargaDTO(nombre, "text/plain; charset=UTF-8", contenido.getBytes(java.nio.charset.StandardCharsets.UTF_8));
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
                .horasDominical(DominicalCc.horasDominical(dias))
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
                .sctrSalud(BigDecimal.ZERO)
                .sctrPension(BigDecimal.ZERO)
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

    private PresupuestoVersion buscarVersion(Long proyectoId, Long versionId) {
        buscarProyecto(proyectoId);
        return presupuestoVersionRepository.findByIdAndProyectoId(versionId, proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la versión de presupuesto"));
    }

    private PresupuestoVersion resolverVersion(
            Long proyectoId, Long versionId, List<PresupuestoVersion> versiones) {
        if (versionId != null) {
            return buscarVersion(proyectoId, versionId);
        }
        return presupuestoVersionRepository
                .findByProyectoIdAndEstado(proyectoId, EstadoVersionPresupuesto.VIGENTE)
                .orElse(versiones.get(0));
    }

    private List<PartidaPresupuestoRequest> persistirPartidas(
            PresupuestoVersion version, List<PartidaPresupuestoRequest> request) {
        List<PartidaPresupuestoRequest> filas = request == null ? List.of() : request;
        validarPartidas(filas);
        List<PartidaPresupuesto> actuales = partidaPresupuestoRepository.findByVersionIdOrderByOrdenAsc(version.getId());
        if (!actuales.isEmpty()) {
            partidaPresupuestoRepository.deleteAll(actuales);
            partidaPresupuestoRepository.flush();
        }
        int orden = 0;
        for (PartidaPresupuestoRequest fila : filas) {
            PartidaPresupuesto partida = new PartidaPresupuesto();
            partida.setVersion(version);
            partida.setCodigo(fila.getCodigo().trim());
            partida.setCapitulo(blankToNull(fila.getCapitulo()));
            partida.setDescripcion(fila.getDescripcion().trim());
            partida.setUnidad(blankToNull(fila.getUnidad()));
            partida.setMetrado(qty(fila.getMetrado()));
            partida.setCentroCosto(blankToNull(fila.getCentroCosto()));
            partida.setOrden(orden++);
            aplicarApu(partida, fila.getApu(), fila.getPrecioUnitario());
            partidaPresupuestoRepository.save(partida);
        }
        return filas;
    }

    private static void asegurarBorrador(PresupuestoVersion version) {
        if (version.getEstado() != EstadoVersionPresupuesto.BORRADOR) {
            throw new IllegalArgumentException("Solo se puede editar un presupuesto en borrador");
        }
    }

    private static void validarPartidas(List<PartidaPresupuestoRequest> filas) {
        Set<String> codigos = new HashSet<>();
        for (PartidaPresupuestoRequest fila : filas) {
            if (fila.getCodigo() == null || fila.getCodigo().isBlank()) {
                throw new IllegalArgumentException("Cada partida requiere código");
            }
            if (fila.getDescripcion() == null || fila.getDescripcion().isBlank()) {
                throw new IllegalArgumentException("Cada partida requiere descripción");
            }
            String codigo = fila.getCodigo().trim();
            if (!codigos.add(codigo)) {
                throw new IllegalArgumentException("Hay códigos de partida duplicados");
            }
            if (nz(fila.getMetrado()).signum() < 0 || nz(fila.getPrecioUnitario()).signum() < 0) {
                throw new IllegalArgumentException("Metrado y precio unitario no pueden ser negativos");
            }
            for (ApuPartidaRequest recurso : fila.getApu() == null ? List.<ApuPartidaRequest>of() : fila.getApu()) {
                if (recurso.getTipo() == null || recurso.getDescripcion() == null || recurso.getDescripcion().isBlank()) {
                    throw new IllegalArgumentException("Cada recurso APU requiere tipo y descripción");
                }
                if (nz(recurso.getCantidad()).signum() < 0 || nz(recurso.getPrecioUnitario()).signum() < 0) {
                    throw new IllegalArgumentException("Cantidad y precio APU no pueden ser negativos");
                }
            }
        }
    }

    private static void aplicarApu(
            PartidaPresupuesto partida, List<ApuPartidaRequest> recursos, BigDecimal precioManual) {
        List<ApuPartidaRequest> filas = recursos == null ? List.of() : recursos;
        int orden = 0;
        BigDecimal sumaApu = BigDecimal.ZERO;
        for (ApuPartidaRequest fila : filas) {
            ApuPartida apu = new ApuPartida();
            apu.setPartida(partida);
            apu.setTipo(fila.getTipo());
            apu.setCodigo(blankToNull(fila.getCodigo()));
            apu.setDescripcion(fila.getDescripcion().trim());
            apu.setUnidad(blankToNull(fila.getUnidad()));
            apu.setCantidad(qty(fila.getCantidad()));
            apu.setPrecioUnitario(money(fila.getPrecioUnitario()));
            apu.setParcial(money(apu.getCantidad().multiply(apu.getPrecioUnitario())));
            apu.setOrden(orden++);
            partida.getApu().add(apu);
            sumaApu = sumaApu.add(apu.getParcial());
        }
        BigDecimal pu = filas.isEmpty() ? money(precioManual) : money(sumaApu);
        partida.setPrecioUnitario(pu);
        partida.setTotal(money(qty(partida.getMetrado()).multiply(pu)));
    }

    private void copiarPartidas(PresupuestoVersion origen, PresupuestoVersion destino) {
        for (PartidaPresupuesto partida : partidaPresupuestoRepository.findByVersionIdOrderByOrdenAsc(origen.getId())) {
            PartidaPresupuesto copia = new PartidaPresupuesto();
            copia.setVersion(destino);
            copia.setCodigo(partida.getCodigo());
            copia.setCapitulo(partida.getCapitulo());
            copia.setDescripcion(partida.getDescripcion());
            copia.setUnidad(partida.getUnidad());
            copia.setMetrado(partida.getMetrado());
            copia.setPrecioUnitario(partida.getPrecioUnitario());
            copia.setTotal(partida.getTotal());
            copia.setCentroCosto(partida.getCentroCosto());
            copia.setOrden(partida.getOrden());
            for (ApuPartida apu : partida.getApu()) {
                ApuPartida copiaApu = new ApuPartida();
                copiaApu.setPartida(copia);
                copiaApu.setTipo(apu.getTipo());
                copiaApu.setCodigo(apu.getCodigo());
                copiaApu.setDescripcion(apu.getDescripcion());
                copiaApu.setUnidad(apu.getUnidad());
                copiaApu.setCantidad(apu.getCantidad());
                copiaApu.setPrecioUnitario(apu.getPrecioUnitario());
                copiaApu.setParcial(apu.getParcial());
                copiaApu.setOrden(apu.getOrden());
                copia.getApu().add(copiaApu);
            }
            destino.getPartidas().add(copia);
        }
    }

    private PresupuestoVersionResumenDTO toVersionResumen(PresupuestoVersion version) {
        return PresupuestoVersionResumenDTO.builder()
                .id(version.getId())
                .tipo(version.getTipo())
                .estado(version.getEstado())
                .numero(version.getNumero())
                .createdAt(version.getCreatedAt())
                .cantidadPartidas(partidaPresupuestoRepository.countByVersionId(version.getId()))
                .build();
    }

    private PartidaPresupuestoDTO toPartidaDto(PartidaPresupuesto partida) {
        return PartidaPresupuestoDTO.builder()
                .id(partida.getId())
                .codigo(partida.getCodigo())
                .capitulo(partida.getCapitulo())
                .descripcion(partida.getDescripcion())
                .unidad(partida.getUnidad())
                .metrado(partida.getMetrado())
                .precioUnitario(partida.getPrecioUnitario())
                .total(partida.getTotal())
                .centroCosto(partida.getCentroCosto())
                .orden(partida.getOrden())
                .apu(partida.getApu().stream().map(ConstruccionServiceImpl::toApuDto).toList())
                .build();
    }

    private PresupuestoVersionDTO toVersionDto(PresupuestoVersion version) {
        List<PartidaPresupuesto> partidas = partidaPresupuestoRepository.findByVersionIdOrderByOrdenAsc(version.getId());
        return PresupuestoVersionDTO.builder()
                .id(version.getId())
                .proyectoId(version.getProyecto().getId())
                .tipo(version.getTipo())
                .estado(version.getEstado())
                .numero(version.getNumero())
                .createdAt(version.getCreatedAt())
                .partidas(partidas.stream().map(this::toPartidaDto).toList())
                .build();
    }

    private static ApuPartidaDTO toApuDto(ApuPartida apu) {
        return ApuPartidaDTO.builder()
                .id(apu.getId())
                .tipo(apu.getTipo())
                .codigo(apu.getCodigo())
                .descripcion(apu.getDescripcion())
                .unidad(apu.getUnidad())
                .cantidad(apu.getCantidad())
                .precioUnitario(apu.getPrecioUnitario())
                .parcial(apu.getParcial())
                .orden(apu.getOrden())
                .build();
    }

    private static void acumular(Map<String, BigDecimal> destinos, String clave, BigDecimal monto) {
        destinos.merge(clave, monto, BigDecimal::add);
    }

    private static String centroDe(String propio, String fallback) {
        String valor = blankToNull(propio);
        if (valor != null) {
            return valor;
        }
        String obra = blankToNull(fallback);
        return obra == null ? "SIN_CENTRO" : obra;
    }

    private static BigDecimal money(BigDecimal valor) {
        return nz(valor).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal qty(BigDecimal valor) {
        return nz(valor).setScale(4, RoundingMode.HALF_UP);
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
                .nivelRiesgo(proyecto.getNivelRiesgo())
                .tasaSctrSalud(proyecto.getTasaSctrSalud())
                .tasaSctrPension(proyecto.getTasaSctrPension())
                .clienteRazon(proyecto.getClienteRazon())
                .clienteRuc(proyecto.getClienteRuc())
                .fechaInicio(proyecto.getFechaInicio())
                .fechaFin(proyecto.getFechaFin())
                .estadoObra(proyecto.getEstadoObra())
                .porcentajeRetencion(proyecto.getPorcentajeRetencion())
                .activo(proyecto.isActivo())
                .alertaUit(UitObraAlerta.supera(proyecto.getValorObra(), parametros.getUit()))
                .uitAnio(parametros.getAnio())
                .build();
    }

    private static ResponsableObraDTO toResponsableDto(ProyectoResponsable responsable) {
        Persona persona = responsable.getPersona();
        return ResponsableObraDTO.builder()
                .id(responsable.getId())
                .personaId(persona.getId())
                .nombres(persona.getNombres())
                .apellidos(persona.getApellidos())
                .numeroDocumento(persona.getNumeroDocumento())
                .rol(responsable.getRol())
                .build();
    }

    private void aplicarCampos(Proyecto proyecto, ProyectoRequest request, String codigo) {
        validarVigencia(request.getFechaInicio(), request.getFechaFin());
        proyecto.setCodigo(codigo);
        proyecto.setNombre(request.getNombre().trim());
        proyecto.setUbicacion(blankToNull(request.getUbicacion()));
        proyecto.setCentroCosto(blankToNull(request.getCentroCosto()));
        proyecto.setValorObra(request.getValorObra());
        proyecto.setNivelRiesgo(request.getNivelRiesgo());
        proyecto.setTasaSctrSalud(nz(request.getTasaSctrSalud()));
        proyecto.setTasaSctrPension(nz(request.getTasaSctrPension()));
        proyecto.setClienteRazon(blankToNull(request.getClienteRazon()));
        proyecto.setClienteRuc(normalizarRuc(request.getClienteRuc()));
        proyecto.setFechaInicio(request.getFechaInicio());
        proyecto.setFechaFin(request.getFechaFin());
        proyecto.setEstadoObra(request.getEstadoObra() == null ? EstadoObra.EN_EJECUCION : request.getEstadoObra());
        BigDecimal retencion = nz(request.getPorcentajeRetencion());
        if (retencion.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("La retención no puede superar 100%");
        }
        proyecto.setPorcentajeRetencion(retencion.setScale(2, RoundingMode.HALF_UP));
        if (request.getActivo() != null) {
            proyecto.setActivo(request.getActivo());
        } else if (proyecto.getId() == null) {
            proyecto.setActivo(true);
        }
    }

    private static void validarVigencia(LocalDate inicio, LocalDate fin) {
        if (inicio != null && fin != null && fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser menor que la de inicio");
        }
    }

    private static String normalizarRuc(String ruc) {
        String valor = blankToNull(ruc);
        if (valor == null) {
            return null;
        }
        if (!valor.matches("\\d{11}")) {
            throw new IllegalArgumentException("El RUC del cliente debe tener 11 dígitos");
        }
        return valor;
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
                .centroCosto(contratoCc.getCentroCosto())
                .fechaIngreso(contrato.getFechaInicio())
                .fechaCese(contrato.getFechaFin())
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
                .cadencia(periodo.getCadencia() == null ? CadenciaPeriodoCc.SEMANAL : periodo.getCadencia())
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
                                item.getOrigen() == null ? OrigenConcepto.CALCULADO : item.getOrigen(),
                                item.getOrden()))
                        .toList())
                .build();
    }

    private List<BoletaCc> cargarBoletasLote(Long proyectoId, Long periodoId, BoletaLoteRequest request) {
        buscarPeriodo(proyectoId, periodoId);
        List<BoletaCc> todas = boletaCcRepository.findByPeriodoIdOrderByIdAsc(periodoId);
        List<Long> ids = request == null || request.getBoletaIds() == null
                ? List.of()
                : request.getBoletaIds().stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            if (todas.isEmpty()) {
                throw new IllegalArgumentException("No hay boletas para emitir");
            }
            return todas;
        }
        Map<Long, BoletaCc> porId = todas.stream().collect(Collectors.toMap(BoletaCc::getId, item -> item));
        List<BoletaCc> seleccion = new ArrayList<>();
        for (Long id : ids) {
            BoletaCc boleta = porId.get(id);
            if (boleta == null) {
                throw new IllegalArgumentException("Una o más boletas no pertenecen al periodo seleccionado");
            }
            seleccion.add(boleta);
        }
        return seleccion;
    }

    private static List<BoletaCcConcepto> copiarManuales(BoletaCc boleta) {
        List<BoletaCcConcepto> manuals = new ArrayList<>();
        if (boleta.getId() == null || boleta.getConceptos() == null) {
            return manuals;
        }
        for (BoletaCcConcepto item : boleta.getConceptos()) {
            if (item.getOrigen() != OrigenConcepto.MANUAL) {
                continue;
            }
            BoletaCcConcepto copia = new BoletaCcConcepto();
            copia.setCategoria(item.getCategoria());
            copia.setCodigo(item.getCodigo());
            copia.setLabel(item.getLabel());
            copia.setMonto(item.getMonto());
            copia.setOrigen(OrigenConcepto.MANUAL);
            manuals.add(copia);
        }
        return manuals;
    }

    private static void aplicarTotales(BoletaCc boleta, CcLiquidacionResult resultado) {
        BigDecimal extraHaber = BigDecimal.ZERO;
        BigDecimal extraDesc = BigDecimal.ZERO;
        for (BoletaCcConcepto concepto : boleta.getConceptos()) {
            if (concepto.getOrigen() != OrigenConcepto.MANUAL) {
                continue;
            }
            if (concepto.getCategoria() == CategoriaConcepto.HABER) {
                extraHaber = extraHaber.add(nz(concepto.getMonto()));
            } else if (concepto.getCategoria() == CategoriaConcepto.DESCUENTO) {
                extraDesc = extraDesc.add(nz(concepto.getMonto()));
            }
        }
        boleta.setTotalHaberes(resultado.getTotalHaberes().add(extraHaber));
        boleta.setTotalDescuentos(resultado.getTotalDescuentos().add(extraDesc));
        boleta.setTotalAportesEmpleador(resultado.getTotalAportes());
        boleta.setTotalAfecto(resultado.getTotalAfecto());
        boleta.setNeto(resultado.getNeto().add(extraHaber).subtract(extraDesc));
    }

    private static void recalcularTotalesDesdeConceptos(BoletaCc boleta) {
        BigDecimal haberes = BigDecimal.ZERO;
        BigDecimal descuentos = BigDecimal.ZERO;
        BigDecimal aportes = BigDecimal.ZERO;
        for (BoletaCcConcepto concepto : boleta.getConceptos()) {
            BigDecimal monto = nz(concepto.getMonto());
            if (concepto.getCategoria() == CategoriaConcepto.HABER) {
                haberes = haberes.add(monto);
            } else if (concepto.getCategoria() == CategoriaConcepto.DESCUENTO) {
                descuentos = descuentos.add(monto);
            } else if (concepto.getCategoria() == CategoriaConcepto.APORTE_EMPLEADOR) {
                aportes = aportes.add(monto);
            }
        }
        boleta.setTotalHaberes(haberes);
        boleta.setTotalDescuentos(descuentos);
        boleta.setTotalAportesEmpleador(aportes);
        boleta.setNeto(haberes.subtract(descuentos));
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
