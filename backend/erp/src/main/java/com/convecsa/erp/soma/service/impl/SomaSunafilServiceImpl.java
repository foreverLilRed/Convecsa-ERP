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
import com.convecsa.erp.soma.dto.SomaSunafilDocumentoDTO;
import com.convecsa.erp.soma.entity.SomaEmoExamen;
import com.convecsa.erp.soma.entity.SomaEventoSst;
import com.convecsa.erp.soma.repository.SomaEmoExamenRepository;
import com.convecsa.erp.soma.repository.SomaEventoSstRepository;
import com.convecsa.erp.soma.service.InterfazSomaSunafilService;
import com.convecsa.erp.soma.support.SomaAcceso;
import com.convecsa.erp.soma.support.SomaSunafilExcelExporter;
import com.convecsa.erp.soma.support.SomaSunafilPdfService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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

    private final AccesoObraService accesoObraService;
    private final SomaAcceso somaAcceso;
    private final ProyectoRepository proyectoRepository;
    private final UsuarioProyectoRepository usuarioProyectoRepository;
    private final NominaDocumentoConfiguracionRepository configuracionRepository;
    private final SomaEventoSstRepository somaEventoSstRepository;
    private final SomaEmoExamenRepository somaEmoExamenRepository;
    private final SomaSunafilPdfService somaSunafilPdfService;
    private final SomaSunafilExcelExporter somaSunafilExcelExporter;

    @Override
    @Transactional(readOnly = true)
    public ArchivoDescargaDTO exportar(int formato, Long proyectoId, Integer anio, Integer mes, String tipo) {
        validarConsulta(formato, anio, mes, tipo);
        Proyecto proyecto = exigirAlcance(proyectoId);
        SomaSunafilDocumentoDTO documento = switch (formato) {
            case 1 -> documentoEventos(proyecto, anio, mes);
            case 2 -> documentoEmos(proyecto, anio, mes);
            default -> throw new IllegalArgumentException("Formato " + formato + " aún no implementado");
        };
        boolean pdf = "pdf".equals(tipo.trim().toLowerCase(Locale.ROOT));
        byte[] contenido = pdf
                ? somaSunafilPdfService.generar(documento)
                : somaSunafilExcelExporter.exportar(documento);
        return new ArchivoDescargaDTO(nombre(formato, proyecto, anio, mes, pdf), pdf ? MIME_PDF : MIME_XLSX, contenido);
    }

    private void validarConsulta(int formato, Integer anio, Integer mes, String tipo) {
        if (formato < 1 || formato > 8) {
            throw new IllegalArgumentException("Formato inválido");
        }
        if (formato > 2) {
            throw new IllegalArgumentException("Formato " + formato + " aún no implementado");
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

    private static String nulo(String valor) {
        return valor == null ? "" : valor;
    }
}
