package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.UsuarioProyectoRepository;
import com.convecsa.erp.construccion.support.AccesoObraService;
import com.convecsa.erp.nomina.declaracion.dto.ArchivoDescargaDTO;
import com.convecsa.erp.nomina.entity.NominaDocumentoConfiguracion;
import com.convecsa.erp.nomina.repository.NominaDocumentoConfiguracionRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.construccion.entity.Material;
import com.convecsa.erp.soma.dto.SomaEstadisticaAnioDTO;
import com.convecsa.erp.soma.dto.SomaEstadisticaMesDTO;
import com.convecsa.erp.soma.dto.SomaSunafilDocumentoDTO;
import com.convecsa.erp.soma.entity.SomaEmoExamen;
import com.convecsa.erp.soma.entity.SomaEventoSst;
import com.convecsa.erp.soma.entity.SomaHallazgo;
import com.convecsa.erp.soma.entity.SomaInspeccion;
import com.convecsa.erp.soma.entity.SomaCapacitacion;
import com.convecsa.erp.soma.entity.SomaCapacitacionAsistente;
import com.convecsa.erp.soma.entity.SomaEntregaEpp;
import com.convecsa.erp.soma.entity.SomaEntregaEppItem;
import com.convecsa.erp.soma.entity.SomaMonitoreo;
import com.convecsa.erp.soma.repository.SomaCapacitacionRepository;
import com.convecsa.erp.soma.repository.SomaEmoExamenRepository;
import com.convecsa.erp.soma.repository.SomaEntregaEppRepository;
import com.convecsa.erp.soma.repository.SomaEventoSstRepository;
import com.convecsa.erp.soma.repository.SomaHallazgoRepository;
import com.convecsa.erp.soma.repository.SomaInspeccionRepository;
import com.convecsa.erp.soma.repository.SomaMonitoreoRepository;
import com.convecsa.erp.soma.service.InterfazSomaEstadisticaService;
import com.convecsa.erp.soma.service.InterfazSomaSunafilService;
import com.convecsa.erp.soma.support.SomaAcceso;
import com.convecsa.erp.soma.support.SomaSunafilExcelExporter;
import com.convecsa.erp.soma.support.SomaSunafilPdfService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaSunafilServiceImpl implements InterfazSomaSunafilService {

    static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    static final String MIME_PDF = "application/pdf";
    static final String MIME_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final List<String> COLUMNAS_EVENTO = List.of(
            "N°",
            "Fecha y hora",
            "Tipo",
            "Gravedad",
            "Trabajador",
            "DNI",
            "Lugar",
            "Descripción",
            "Días DM",
            "Estado investigación",
            "Notificado MTPE",
            "Obra");
    private static final List<String> COLUMNAS_EMO = List.of(
            "Trabajador",
            "DNI",
            "Tipo",
            "Fecha examen",
            "Vencimiento",
            "Aptitud",
            "Clínica",
            "RUC",
            "Médico",
            "CMP",
            "Obra");
    private static final List<String> COLUMNAS_MONITOREO = List.of(
            "Fecha",
            "Tipo agente",
            "Parámetro",
            "Punto",
            "Valor",
            "LMP",
            "Unidad",
            "Cumple LMP",
            "Laboratorio",
            "Obra");
    private static final List<String> COLUMNAS_INSPECCION = List.of(
            "Fecha inspección",
            "Tipo",
            "Área",
            "Inspector",
            "Puntaje",
            "Fecha detección",
            "Clasificación",
            "Potencial",
            "Estado",
            "Descripción",
            "Responsable",
            "Fecha límite",
            "Obra");
    private static final List<String> COLUMNAS_ESTADISTICA = List.of(
            "Mes",
            "HHT",
            "Accidentes",
            "Días perdidos",
            "IF",
            "IS",
            "IA",
            "Días sin accidente");
    private static final List<String> COLUMNAS_EPP = List.of(
            "Fecha",
            "Trabajador",
            "DNI",
            "Motivo",
            "Material",
            "Cantidad",
            "Talla",
            "Estado",
            "Vencimiento",
            "Firmado",
            "Entregado por",
            "Obra");
    private static final List<String> COLUMNAS_CAPACITACION = List.of(
            "Fecha",
            "Tipo",
            "Tema",
            "Horas",
            "Expositor",
            "Entidad",
            "Puntaje simulacro",
            "Trabajador",
            "DNI",
            "Nota",
            "Aprobado",
            "Obra");
    private static final List<String> COLUMNAS_AUDITORIA = List.of(
            "Fecha",
            "Tipo",
            "Alcance",
            "Auditor líder",
            "Empresa auditora",
            "Resultado",
            "No conformidades",
            "Observaciones");

    private final AccesoObraService accesoObraService;
    private final SomaAcceso somaAcceso;
    private final ProyectoRepository proyectoRepository;
    private final UsuarioProyectoRepository usuarioProyectoRepository;
    private final NominaDocumentoConfiguracionRepository configuracionRepository;
    private final SomaEventoSstRepository somaEventoSstRepository;
    private final SomaEmoExamenRepository somaEmoExamenRepository;
    private final SomaMonitoreoRepository somaMonitoreoRepository;
    private final SomaInspeccionRepository somaInspeccionRepository;
    private final SomaHallazgoRepository somaHallazgoRepository;
    private final InterfazSomaEstadisticaService somaEstadisticaService;
    private final SomaEntregaEppRepository somaEntregaEppRepository;
    private final SomaCapacitacionRepository somaCapacitacionRepository;
    private final SomaSunafilPdfService somaSunafilPdfService;
    private final SomaSunafilExcelExporter somaSunafilExcelExporter;

    @Override
    @Transactional(readOnly = true)
    public ArchivoDescargaDTO exportar(int formato, Long proyectoId, Integer anio, Integer mes, String tipo) {
        validarConsulta(formato, anio, mes, tipo);
        Proyecto proyecto = exigirAlcance(proyectoId);
        Integer anioEstadistica = anio;
        if (formato == 5 && anioEstadistica == null) {
            anioEstadistica = LocalDate.now(ZONA_LIMA).getYear();
        }
        SomaSunafilDocumentoDTO documento = switch (formato) {
            case 1 -> documentoEventos(proyecto, anio, mes);
            case 2 -> documentoEmos(proyecto, anio, mes);
            case 3 -> documentoMonitoreos(proyecto, anio, mes);
            case 4 -> documentoInspecciones(proyecto, anio, mes);
            case 5 -> documentoEstadisticas(proyecto, anioEstadistica, mes);
            case 6 -> documentoEpp(proyecto, anio, mes);
            case 7 -> documentoCapacitaciones(proyecto, anio, mes);
            case 8 -> documentoAuditorias(proyecto, anio, mes);
            default -> throw new IllegalArgumentException("Formato " + formato + " aún no implementado");
        };
        boolean pdf = "pdf".equals(tipo.trim().toLowerCase(Locale.ROOT));
        byte[] contenido = pdf
                ? somaSunafilPdfService.generar(documento)
                : somaSunafilExcelExporter.exportar(documento);
        Integer anioArchivo = formato == 5 ? anioEstadistica : anio;
        return new ArchivoDescargaDTO(nombre(formato, proyecto, anioArchivo, mes, pdf), pdf ? MIME_PDF : MIME_XLSX, contenido);
    }

    private void validarConsulta(int formato, Integer anio, Integer mes, String tipo) {
        if (formato < 1 || formato > 8) {
            throw new IllegalArgumentException("Formato inválido");
        }
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo debe ser pdf o xlsx");
        }
        String normalizado = tipo.trim().toLowerCase(Locale.ROOT);
        if (!"pdf".equals(normalizado) && !"xlsx".equals(normalizado)) {
            throw new IllegalArgumentException("El tipo debe ser pdf o xlsx");
        }
        if (mes != null && anio == null) {
            throw new IllegalArgumentException("El mes exige el año");
        }
        if (anio != null && (anio < 2000 || anio > 2100)) {
            throw new IllegalArgumentException("El año debe ser válido");
        }
        if (mes != null && (mes < 1 || mes > 12)) {
            throw new IllegalArgumentException("El mes debe ser válido");
        }
    }

    private Proyecto exigirAlcance(Long proyectoId) {
        if (proyectoId != null) {
            accesoObraService.exigirObra(proyectoId);
            return proyectoRepository.findById(proyectoId)
                    .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
        }
        somaAcceso.exigirSomaCorporativo();
        return null;
    }

    private SomaSunafilDocumentoDTO documentoEventos(Proyecto proyecto, Integer anio, Integer mes) {
        List<SomaEventoSst> eventos = cargarEventos(proyecto).stream()
                .filter(evento -> enPeriodo(
                        evento.getFechaHora() == null
                                ? null
                                : evento.getFechaHora().atZone(ZONA_LIMA).toLocalDate(),
                        anio,
                        mes))
                .toList();
        List<List<String>> filas = new ArrayList<>();
        for (SomaEventoSst evento : eventos) {
            Persona persona = evento.getPersonaAfectada();
            filas.add(List.of(
                    String.valueOf(evento.getNumero()),
                    evento.getFechaHora() == null
                            ? ""
                            : FECHA_HORA.format(evento.getFechaHora().atZone(ZONA_LIMA)),
                    evento.getTipoEvento() == null ? "" : evento.getTipoEvento().name(),
                    evento.getGravedadIncapacidad() == null ? "" : evento.getGravedadIncapacidad().name(),
                    nombrePersona(persona),
                    persona == null ? "" : nulo(persona.getNumeroDocumento()),
                    nulo(evento.getLugarExacto()),
                    nulo(evento.getDescripcionEvento()),
                    String.valueOf(evento.getDiasDescansoMedico()),
                    evento.getEstadoInvestigacion() == null ? "" : evento.getEstadoInvestigacion().name(),
                    evento.isNotificadoMtpe() ? "Sí" : "No",
                    codigoObra(evento.getProyecto(), proyecto)));
        }
        return documento(
                1,
                "Registro de accidentes, enfermedades ocupacionales, incidentes peligrosos y otros incidentes (R.M. 050-2013-TR)",
                proyecto,
                anio,
                mes,
                COLUMNAS_EVENTO,
                filas);
    }

    private SomaSunafilDocumentoDTO documentoEmos(Proyecto proyecto, Integer anio, Integer mes) {
        List<SomaEmoExamen> emos = cargarEmos(proyecto).stream()
                .filter(emo -> enPeriodo(emo.getFechaExamen(), anio, mes))
                .toList();
        List<List<String>> filas = new ArrayList<>();
        for (SomaEmoExamen emo : emos) {
            Persona persona = emo.getPersona();
            filas.add(List.of(
                    nombrePersona(persona),
                    persona == null ? "" : nulo(persona.getNumeroDocumento()),
                    emo.getTipoEmo() == null ? "" : emo.getTipoEmo().name(),
                    fecha(emo.getFechaExamen()),
                    fecha(emo.getFechaVencimiento()),
                    emo.getAptitud() == null ? "" : emo.getAptitud().name(),
                    nulo(emo.getClinicaNombre()),
                    nulo(emo.getClinicaRuc()),
                    nulo(emo.getMedicoEvaluador()),
                    nulo(emo.getCmpMedico()),
                    codigoObra(emo.getProyecto(), proyecto)));
        }
        return documento(
                2,
                "Registro de exámenes médico ocupacionales (R.M. 050-2013-TR)",
                proyecto,
                anio,
                mes,
                COLUMNAS_EMO,
                filas);
    }

    private SomaSunafilDocumentoDTO documentoMonitoreos(Proyecto proyecto, Integer anio, Integer mes) {
        List<List<String>> filas = new ArrayList<>();
        for (SomaMonitoreo item : cargarMonitoreos(proyecto)) {
            if (!enPeriodo(item.getFecha(), anio, mes)) {
                continue;
            }
            filas.add(List.of(
                    fecha(item.getFecha()),
                    item.getTipoAgente() == null ? "" : item.getTipoAgente().name(),
                    nulo(item.getParametroMedido()),
                    nulo(item.getPuntoMedicion()),
                    decimal(item.getValorMedido()),
                    decimal(item.getLimitePermisible()),
                    nulo(item.getUnidad()),
                    item.isCumpleLmp() ? "Sí" : "No",
                    nulo(item.getLaboratorioNombre()),
                    codigoObra(item.getProyecto(), proyecto)));
        }
        return documento(
                3,
                "Registro de monitoreo de agentes (R.M. 050-2013-TR)",
                proyecto,
                anio,
                mes,
                COLUMNAS_MONITOREO,
                filas);
    }

    private SomaSunafilDocumentoDTO documentoInspecciones(Proyecto proyecto, Integer anio, Integer mes) {
        List<SomaInspeccion> inspecciones = cargarInspecciones(proyecto).stream()
                .filter(item -> enPeriodo(item.getFecha(), anio, mes))
                .toList();
        List<SomaHallazgo> hallazgos = cargarHallazgos(proyecto);
        Map<Long, List<SomaHallazgo>> porInspeccion = new LinkedHashMap<>();
        for (SomaHallazgo hallazgo : hallazgos) {
            if (hallazgo.getInspeccion() == null || hallazgo.getInspeccion().getId() == null) {
                continue;
            }
            porInspeccion
                    .computeIfAbsent(hallazgo.getInspeccion().getId(), clave -> new ArrayList<>())
                    .add(hallazgo);
        }
        List<List<String>> filas = new ArrayList<>();
        for (SomaInspeccion inspeccion : inspecciones) {
            List<SomaHallazgo> deInspeccion = inspeccion.getId() == null
                    ? List.of()
                    : porInspeccion.getOrDefault(inspeccion.getId(), List.of());
            if (deInspeccion.isEmpty()) {
                filas.add(filaInspeccion(inspeccion, null, proyecto));
            } else {
                for (SomaHallazgo hallazgo : deInspeccion) {
                    filas.add(filaInspeccion(inspeccion, hallazgo, proyecto));
                }
            }
        }
        for (SomaHallazgo hallazgo : hallazgos) {
            if (hallazgo.getInspeccion() != null) {
                continue;
            }
            if (!enPeriodo(hallazgo.getFechaDeteccion(), anio, mes)) {
                continue;
            }
            filas.add(filaInspeccion(null, hallazgo, proyecto));
        }
        return documento(
                4,
                "Registro de inspecciones internas de SST (R.M. 050-2013-TR)",
                proyecto,
                anio,
                mes,
                COLUMNAS_INSPECCION,
                filas);
    }

    private List<String> filaInspeccion(SomaInspeccion inspeccion, SomaHallazgo hallazgo, Proyecto alcance) {
        Proyecto obra = inspeccion != null ? inspeccion.getProyecto() : (hallazgo == null ? null : hallazgo.getProyecto());
        return List.of(
                inspeccion == null ? "" : fecha(inspeccion.getFecha()),
                inspeccion == null || inspeccion.getTipo() == null ? "" : inspeccion.getTipo().name(),
                inspeccion == null ? "" : nulo(inspeccion.getAreaFrente()),
                inspeccion == null ? "" : nombrePersona(inspeccion.getInspector()),
                inspeccion == null ? "" : decimal(inspeccion.getPuntajeCumplimiento()),
                hallazgo == null ? "" : fecha(hallazgo.getFechaDeteccion()),
                hallazgo == null || hallazgo.getClasificacion() == null ? "" : hallazgo.getClasificacion().name(),
                hallazgo == null || hallazgo.getPotencialPerdida() == null ? "" : hallazgo.getPotencialPerdida().name(),
                hallazgo == null || hallazgo.getEstado() == null ? "" : hallazgo.getEstado().name(),
                hallazgo == null ? "" : nulo(hallazgo.getDescripcion()),
                hallazgo == null ? "" : nombrePersona(hallazgo.getResponsable()),
                hallazgo == null ? "" : fecha(hallazgo.getFechaLimite()),
                codigoObra(obra, alcance));
    }

    private SomaSunafilDocumentoDTO documentoEstadisticas(Proyecto proyecto, Integer anio, Integer mes) {
        SomaEstadisticaAnioDTO stats = proyecto == null
                ? somaEstadisticaService.corporativo(anio)
                : somaEstadisticaService.porObra(proyecto.getId(), anio);
        List<List<String>> filas = new ArrayList<>();
        List<SomaEstadisticaMesDTO> meses = stats.getMeses() == null ? List.of() : stats.getMeses();
        if (mes != null) {
            if (mes >= 1 && mes <= meses.size()) {
                filas.add(filaEstadistica(meses.get(mes - 1), null));
            }
        } else {
            for (SomaEstadisticaMesDTO fila : meses) {
                filas.add(filaEstadistica(fila, null));
            }
        }
        if (stats.getAcumulado() != null) {
            filas.add(filaEstadistica(stats.getAcumulado(), stats.getDiasSinAccidente()));
        }
        return documento(
                5,
                "Estadísticas de SST — HHT, IF, IS, IA (R.M. 050-2013-TR)",
                proyecto,
                anio,
                mes,
                COLUMNAS_ESTADISTICA,
                filas);
    }

    private static List<String> filaEstadistica(SomaEstadisticaMesDTO fila, Integer diasSinAccidente) {
        String mes = fila.getMes() == null ? "Acumulado" : String.valueOf(fila.getMes());
        return List.of(
                mes,
                decimal(fila.getHht()),
                String.valueOf(fila.getAccidentes()),
                String.valueOf(fila.getDiasPerdidos()),
                decimal(fila.getIndiceFrecuencia()),
                decimal(fila.getIndiceSeveridad()),
                decimal(fila.getIndiceAccidentabilidad()),
                diasSinAccidente == null ? "" : String.valueOf(diasSinAccidente));
    }

    private SomaSunafilDocumentoDTO documentoEpp(Proyecto proyecto, Integer anio, Integer mes) {
        List<List<String>> filas = new ArrayList<>();
        for (SomaEntregaEpp entrega : cargarEpp(proyecto)) {
            if (!enPeriodo(entrega.getFechaEntrega(), anio, mes)) {
                continue;
            }
            List<SomaEntregaEppItem> items = entrega.getItems() == null ? List.of() : entrega.getItems();
            if (items.isEmpty()) {
                continue;
            }
            for (SomaEntregaEppItem item : items) {
                Material material = item.getMaterial();
                String materialTxt = material == null
                        ? ""
                        : (nulo(material.getCodigo()) + " — " + nulo(material.getDescripcion())).trim();
                Persona persona = entrega.getPersona();
                filas.add(List.of(
                        fecha(entrega.getFechaEntrega()),
                        nombrePersona(persona),
                        persona == null ? "" : nulo(persona.getNumeroDocumento()),
                        entrega.getMotivo() == null ? "" : entrega.getMotivo().name(),
                        materialTxt,
                        decimal(item.getCantidad()),
                        nulo(item.getTalla()),
                        item.getEstadoItem() == null ? "" : item.getEstadoItem().name(),
                        fecha(item.getFechaVencimientoEstimada()),
                        entrega.isFirmadoTrabajador() ? "Sí" : "No",
                        nulo(entrega.getEntregadoPor()),
                        codigoObra(entrega.getProyecto(), proyecto)));
            }
        }
        return documento(
                6,
                "Registro de equipos de seguridad o emergencia (R.M. 050-2013-TR)",
                proyecto,
                anio,
                mes,
                COLUMNAS_EPP,
                filas);
    }

    private SomaSunafilDocumentoDTO documentoCapacitaciones(Proyecto proyecto, Integer anio, Integer mes) {
        List<List<String>> filas = new ArrayList<>();
        for (SomaCapacitacion sesion : cargarCapacitaciones(proyecto)) {
            if (!enPeriodo(sesion.getFecha(), anio, mes)) {
                continue;
            }
            List<SomaCapacitacionAsistente> asistentes =
                    sesion.getAsistentes() == null ? List.of() : sesion.getAsistentes();
            if (asistentes.isEmpty()) {
                filas.add(filaCapacitacion(sesion, null, proyecto));
            } else {
                for (SomaCapacitacionAsistente asistente : asistentes) {
                    filas.add(filaCapacitacion(sesion, asistente, proyecto));
                }
            }
        }
        return documento(
                7,
                "Registro de inducción, capacitación, entrenamiento y simulacros (R.M. 050-2013-TR)",
                proyecto,
                anio,
                mes,
                COLUMNAS_CAPACITACION,
                filas);
    }

    private List<String> filaCapacitacion(
            SomaCapacitacion sesion, SomaCapacitacionAsistente asistente, Proyecto alcance) {
        Persona persona = asistente == null ? null : asistente.getPersona();
        return List.of(
                fecha(sesion.getFecha()),
                sesion.getTipo() == null ? "" : sesion.getTipo().name(),
                nulo(sesion.getTema()),
                decimal(sesion.getHorasDuracion()),
                nulo(sesion.getExpositorNombre()),
                nulo(sesion.getExpositorEntidad()),
                decimal(sesion.getPuntajeSimulacro()),
                nombrePersona(persona),
                persona == null ? "" : nulo(persona.getNumeroDocumento()),
                asistente == null ? "" : decimal(asistente.getNota()),
                asistente == null ? "" : (asistente.isAprobado() ? "Sí" : "No"),
                codigoObra(sesion.getProyecto(), alcance));
    }

    private SomaSunafilDocumentoDTO documentoAuditorias(Proyecto proyecto, Integer anio, Integer mes) {
        return documento(
                8,
                "Registro de auditorías del sistema de gestión de SST (R.M. 050-2013-TR)",
                proyecto,
                anio,
                mes,
                COLUMNAS_AUDITORIA,
                List.of());
    }

    private List<SomaEventoSst> cargarEventos(Proyecto proyecto) {
        if (proyecto != null) {
            return somaEventoSstRepository.findByProyectoIdOrderByFechaHoraDescNumeroDesc(proyecto.getId());
        }
        if (AccesoObraService.esGerencia(accesoObraService.usuarioActual().getRol())) {
            return somaEventoSstRepository.findAllByOrderByFechaHoraDescNumeroDesc();
        }
        List<Long> obras = usuarioProyectoRepository.findProyectoIdsByUsuarioId(
                accesoObraService.usuarioActual().getId());
        if (obras.isEmpty()) {
            return List.of();
        }
        return somaEventoSstRepository.findByProyectoIdInOrderByFechaHoraDescNumeroDesc(obras);
    }

    private List<SomaEmoExamen> cargarEmos(Proyecto proyecto) {
        if (proyecto != null) {
            return somaEmoExamenRepository.findByProyectoIdOrderByFechaExamenDescIdDesc(proyecto.getId());
        }
        List<SomaEmoExamen> todos = somaEmoExamenRepository.buscarCorporativo(null, null);
        if (AccesoObraService.esGerencia(accesoObraService.usuarioActual().getRol())) {
            return todos;
        }
        Set<Long> obras = new HashSet<>(usuarioProyectoRepository.findProyectoIdsByUsuarioId(
                accesoObraService.usuarioActual().getId()));
        if (obras.isEmpty()) {
            return List.of();
        }
        return todos.stream()
                .filter(emo -> emo.getProyecto() != null && obras.contains(emo.getProyecto().getId()))
                .toList();
    }

    private List<SomaMonitoreo> cargarMonitoreos(Proyecto proyecto) {
        if (proyecto != null) {
            return somaMonitoreoRepository.findByProyectoIdOrderByFechaDescIdDesc(proyecto.getId());
        }
        if (AccesoObraService.esGerencia(accesoObraService.usuarioActual().getRol())) {
            return somaMonitoreoRepository.findAllByOrderByFechaDescIdDesc();
        }
        List<Long> obras = obrasAsignadas();
        if (obras.isEmpty()) {
            return List.of();
        }
        return somaMonitoreoRepository.findByProyectoIdInOrderByFechaDescIdDesc(obras);
    }

    private List<SomaInspeccion> cargarInspecciones(Proyecto proyecto) {
        if (proyecto != null) {
            return somaInspeccionRepository.findByProyectoIdOrderByFechaDescIdDesc(proyecto.getId());
        }
        if (AccesoObraService.esGerencia(accesoObraService.usuarioActual().getRol())) {
            return somaInspeccionRepository.findAllByOrderByFechaDescIdDesc();
        }
        List<Long> obras = obrasAsignadas();
        if (obras.isEmpty()) {
            return List.of();
        }
        return somaInspeccionRepository.findByProyectoIdInOrderByFechaDescIdDesc(obras);
    }

    private List<SomaHallazgo> cargarHallazgos(Proyecto proyecto) {
        if (proyecto != null) {
            return somaHallazgoRepository.findByProyectoIdOrderByFechaDeteccionDescIdDesc(proyecto.getId());
        }
        if (AccesoObraService.esGerencia(accesoObraService.usuarioActual().getRol())) {
            return somaHallazgoRepository.findAllByOrderByFechaDeteccionDescIdDesc();
        }
        List<Long> obras = obrasAsignadas();
        if (obras.isEmpty()) {
            return List.of();
        }
        return somaHallazgoRepository.findByProyectoIdInOrderByFechaDeteccionDescIdDesc(obras);
    }

    private List<SomaEntregaEpp> cargarEpp(Proyecto proyecto) {
        if (proyecto != null) {
            return somaEntregaEppRepository.findByProyectoIdOrderByFechaEntregaDescIdDesc(proyecto.getId());
        }
        if (AccesoObraService.esGerencia(accesoObraService.usuarioActual().getRol())) {
            return somaEntregaEppRepository.findAllByOrderByFechaEntregaDescIdDesc();
        }
        List<Long> obras = obrasAsignadas();
        if (obras.isEmpty()) {
            return List.of();
        }
        return somaEntregaEppRepository.findByProyectoIdInOrderByFechaEntregaDescIdDesc(obras);
    }

    private List<SomaCapacitacion> cargarCapacitaciones(Proyecto proyecto) {
        if (proyecto != null) {
            return somaCapacitacionRepository.findByProyectoIdOrderByFechaDescIdDesc(proyecto.getId());
        }
        if (AccesoObraService.esGerencia(accesoObraService.usuarioActual().getRol())) {
            return somaCapacitacionRepository.findAllByOrderByFechaDescIdDesc();
        }
        List<Long> obras = obrasAsignadas();
        List<SomaCapacitacion> deObras = obras.isEmpty()
                ? new ArrayList<>()
                : new ArrayList<>(somaCapacitacionRepository.findByProyectoIdInOrderByFechaDescIdDesc(obras));
        deObras.addAll(somaCapacitacionRepository.findByProyectoIsNullOrderByFechaDescIdDesc());
        deObras.sort(Comparator
                .comparing(SomaCapacitacion::getFecha, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(SomaCapacitacion::getId, Comparator.nullsLast(Comparator.reverseOrder())));
        return deObras;
    }

    private List<Long> obrasAsignadas() {
        return usuarioProyectoRepository.findProyectoIdsByUsuarioId(accesoObraService.usuarioActual().getId());
    }

    private SomaSunafilDocumentoDTO documento(
            int formato,
            String titulo,
            Proyecto proyecto,
            Integer anio,
            Integer mes,
            List<String> columnas,
            List<List<String>> filas) {
        NominaDocumentoConfiguracion cfg =
                configuracionRepository.findById(NominaDocumentoConfiguracion.ID_UNICO).orElse(null);
        return SomaSunafilDocumentoDTO.builder()
                .formato(formato)
                .titulo(titulo)
                .razonSocial(cfg == null ? "" : nulo(cfg.getRazonSocial()))
                .ruc(cfg == null ? "" : nulo(cfg.getRuc()))
                .domicilio(cfg == null ? "" : nulo(cfg.getDomicilio()))
                .logoDataUrl(cfg == null ? null : cfg.getLogoDataUrl())
                .alcance(alcance(proyecto))
                .periodo(periodo(anio, mes))
                .columnas(columnas)
                .filas(filas)
                .build();
    }

    private static boolean enPeriodo(LocalDate fecha, Integer anio, Integer mes) {
        if (anio == null) {
            return true;
        }
        if (fecha == null || fecha.getYear() != anio) {
            return false;
        }
        return mes == null || fecha.getMonthValue() == mes;
    }

    private static String alcance(Proyecto proyecto) {
        if (proyecto == null) {
            return "Corporativo";
        }
        String nombre = nulo(proyecto.getNombre());
        if (nombre.isEmpty()) {
            return nulo(proyecto.getCodigo());
        }
        return nulo(proyecto.getCodigo()) + " — " + nombre;
    }

    private static String periodo(Integer anio, Integer mes) {
        if (anio == null) {
            return "Histórico";
        }
        if (mes == null) {
            return String.valueOf(anio);
        }
        return anio + "-" + String.format("%02d", mes);
    }

    private static String nombre(int formato, Proyecto proyecto, Integer anio, Integer mes, boolean pdf) {
        StringBuilder sb = new StringBuilder("sunafil-formato-")
                .append(formato)
                .append("-")
                .append(proyecto == null || proyecto.getCodigo() == null || proyecto.getCodigo().isBlank()
                        ? "corp"
                        : proyecto.getCodigo());
        if (anio != null) {
            sb.append("-").append(anio);
            if (mes != null) {
                sb.append("-").append(String.format("%02d", mes));
            }
        }
        return sb.append(pdf ? ".pdf" : ".xlsx").toString();
    }

    private static String codigoObra(Proyecto deFila, Proyecto alcance) {
        if (deFila != null && deFila.getCodigo() != null) {
            return deFila.getCodigo();
        }
        return alcance == null ? "" : nulo(alcance.getCodigo());
    }

    private static String nombrePersona(Persona persona) {
        if (persona == null) {
            return "";
        }
        String apellidos = persona.getApellidos() == null ? "" : persona.getApellidos().trim();
        String nombres = persona.getNombres() == null ? "" : persona.getNombres().trim();
        return (apellidos + " " + nombres).trim();
    }

    private static String fecha(LocalDate valor) {
        return valor == null ? "" : FECHA.format(valor);
    }

    private static String decimal(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return valor.stripTrailingZeros().toPlainString();
    }

    private static String nulo(String valor) {
        return valor == null ? "" : valor;
    }
}
