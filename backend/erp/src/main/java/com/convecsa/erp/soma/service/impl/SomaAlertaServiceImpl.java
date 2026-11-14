package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.construccion.domain.EstadoObra;
import com.convecsa.erp.construccion.domain.SeveridadAlertaObra;
import com.convecsa.erp.construccion.dto.AlertaObraDTO;
import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.soma.domain.EstadoHallazgoSoma;
import com.convecsa.erp.soma.domain.EstadoInvestigacionSst;
import com.convecsa.erp.soma.domain.EstadoSctrPoliza;
import com.convecsa.erp.soma.domain.PotencialPerdidaSoma;
import com.convecsa.erp.soma.domain.TipoRequerimientoComiteSoma;
import com.convecsa.erp.soma.dto.SomaComiteMiembroDTO;
import com.convecsa.erp.soma.dto.SomaComiteRequerimientoDTO;
import com.convecsa.erp.soma.dto.SomaPetarFaltanteTareoDTO;
import com.convecsa.erp.soma.entity.SomaEmoExamen;
import com.convecsa.erp.soma.entity.SomaEventoSst;
import com.convecsa.erp.soma.entity.SomaHallazgo;
import com.convecsa.erp.soma.repository.SomaEmoExamenRepository;
import com.convecsa.erp.soma.repository.SomaEventoSstRepository;
import com.convecsa.erp.soma.repository.SomaHallazgoRepository;
import com.convecsa.erp.soma.repository.SomaSctrPolizaRepository;
import com.convecsa.erp.soma.repository.SomaTrabajadorObraRepository;
import com.convecsa.erp.soma.service.InterfazSomaAlertaService;
import com.convecsa.erp.soma.service.InterfazSomaComiteService;
import com.convecsa.erp.soma.service.InterfazSomaPetarService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaAlertaServiceImpl implements InterfazSomaAlertaService {

    static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private final ProyectoRepository proyectoRepository;
    private final SomaTrabajadorObraRepository somaTrabajadorObraRepository;
    private final SomaEmoExamenRepository somaEmoExamenRepository;
    private final SomaSctrPolizaRepository somaSctrPolizaRepository;
    private final SomaHallazgoRepository somaHallazgoRepository;
    private final SomaEventoSstRepository somaEventoSstRepository;
    private final InterfazSomaComiteService somaComiteService;
    private final InterfazSomaPetarService somaPetarService;

    @Override
    @Transactional(readOnly = true)
    public List<AlertaObraDTO> alertas(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId).orElse(null);
        if (proyecto == null) {
            return List.of();
        }
        LocalDate hoy = LocalDate.now(ZONA_LIMA);
        List<Persona> activos = deduplicarActivos(somaTrabajadorObraRepository.buscarActivos(proyectoId, hoy, hoy));
        List<AlertaObraDTO> alertas = new ArrayList<>();
        agregarEmo(alertas, activos, hoy);
        agregarSctr(alertas, proyecto, activos, hoy);
        agregarComite(alertas, proyectoId);
        agregarHallazgos(alertas, proyectoId, hoy);
        agregarPetar(alertas, proyectoId);
        agregarEventos(alertas, proyectoId, hoy);
        return List.copyOf(alertas);
    }

    private void agregarEmo(List<AlertaObraDTO> alertas, List<Persona> activos, LocalDate hoy) {
        int vencidos = 0;
        int porVencer = 0;
        LocalDate limite = hoy.plusDays(30);
        for (Persona persona : activos) {
            if (persona.getId() == null) {
                continue;
            }
            SomaEmoExamen emo = somaEmoExamenRepository
                    .findFirstByPersonaIdOrderByFechaExamenDescIdDesc(persona.getId())
                    .orElse(null);
            if (emo == null || emo.getFechaVencimiento() == null || emo.getFechaVencimiento().isBefore(hoy)) {
                vencidos++;
            } else if (!emo.getFechaVencimiento().isAfter(limite)) {
                porVencer++;
            }
        }
        if (vencidos > 0) {
            alertas.add(alerta(
                    "SOMA_EMO_VENCIDO",
                    SeveridadAlertaObra.CRITICAL,
                    vencidos + (vencidos == 1
                            ? " trabajador con EMO vencido o sin EMO"
                            : " trabajadores con EMO vencido o sin EMO")));
        }
        if (porVencer > 0) {
            alertas.add(alerta(
                    "SOMA_EMO_POR_VENCER",
                    SeveridadAlertaObra.WARNING,
                    porVencer + (porVencer == 1
                            ? " trabajador con EMO por vencer"
                            : " trabajadores con EMO por vencer")));
        }
    }

    private void agregarSctr(List<AlertaObraDTO> alertas, Proyecto proyecto, List<Persona> activos, LocalDate hoy) {
        if (proyecto.getEstadoObra() != EstadoObra.EN_EJECUCION || activos.isEmpty()) {
            return;
        }
        boolean vigente = somaSctrPolizaRepository
                .findByProyectoIdAndAnioAndMesAndEstado(
                        proyecto.getId(), hoy.getYear(), hoy.getMonthValue(), EstadoSctrPoliza.VIGENTE)
                .isPresent();
        if (!vigente) {
            alertas.add(alerta(
                    "SOMA_SCTR_SIN_POLIZA",
                    SeveridadAlertaObra.CRITICAL,
                    "Obra en ejecución sin póliza SCTR vigente del mes"));
        }
    }

    private void agregarComite(List<AlertaObraDTO> alertas, Long proyectoId) {
        SomaComiteRequerimientoDTO req = somaComiteService.requerimiento(proyectoId);
        if (req.getTipoRequerido() == TipoRequerimientoComiteSoma.SUBCOMITE) {
            long activos = somaComiteService.listarMiembros(proyectoId).stream()
                    .filter(SomaComiteMiembroDTO::isActivo)
                    .count();
            if (activos == 0) {
                alertas.add(alerta(
                        "SOMA_COMITE_REQUERIDO",
                        SeveridadAlertaObra.WARNING,
                        "Falta constituir el subcomité SST"));
            }
            return;
        }
        if (req.getSupervisoresActivos() == 0) {
            alertas.add(alerta(
                    "SOMA_COMITE_REQUERIDO",
                    SeveridadAlertaObra.WARNING,
                    "Falta un supervisor SST activo"));
        }
    }

    private void agregarHallazgos(List<AlertaObraDTO> alertas, Long proyectoId, LocalDate hoy) {
        List<SomaHallazgo> vencidos = somaHallazgoRepository
                .findByProyectoIdOrderByFechaDeteccionDescIdDesc(proyectoId)
                .stream()
                .filter(hallazgo -> hallazgo.getEstado() != EstadoHallazgoSoma.LEVANTADO)
                .filter(hallazgo -> hallazgo.getFechaLimite() != null && hallazgo.getFechaLimite().isBefore(hoy))
                .toList();
        if (vencidos.isEmpty()) {
            return;
        }
        boolean grave = vencidos.stream().anyMatch(h -> h.getPotencialPerdida() == PotencialPerdidaSoma.ALTO_GRAVE);
        int n = vencidos.size();
        alertas.add(alerta(
                "SOMA_HALLAZGO_VENCIDO",
                grave ? SeveridadAlertaObra.CRITICAL : SeveridadAlertaObra.WARNING,
                n + (n == 1 ? " hallazgo vencido" : " hallazgos vencidos")));
    }

    private void agregarPetar(List<AlertaObraDTO> alertas, Long proyectoId) {
        List<SomaPetarFaltanteTareoDTO> faltantes = somaPetarService.faltantesTareo(proyectoId);
        long dias = faltantes.stream().map(SomaPetarFaltanteTareoDTO::getFecha).distinct().count();
        if (dias == 0) {
            return;
        }
        alertas.add(alerta(
                "SOMA_PETAR_FALTANTE_TAREO",
                SeveridadAlertaObra.WARNING,
                dias + (dias == 1 ? " día de tareo sin PETAR autorizado" : " días de tareo sin PETAR autorizado")));
    }

    private void agregarEventos(List<AlertaObraDTO> alertas, Long proyectoId, LocalDate hoy) {
        LocalDate limite = hoy.minusDays(30);
        int abiertos = (int) somaEventoSstRepository.findByProyectoIdOrderByFechaHoraDescNumeroDesc(proyectoId).stream()
                .filter(evento -> evento.getEstadoInvestigacion() == EstadoInvestigacionSst.EN_INVESTIGACION)
                .filter(evento -> evento.getFechaHora() != null)
                .filter(evento -> evento.getFechaHora().atZone(ZONA_LIMA).toLocalDate().isBefore(limite))
                .count();
        if (abiertos == 0) {
            return;
        }
        alertas.add(alerta(
                "SOMA_EVENTO_ABIERTO",
                SeveridadAlertaObra.INFO,
                abiertos + (abiertos == 1
                        ? " investigación SST abierta más de 30 días"
                        : " investigaciones SST abiertas más de 30 días")));
    }

    private static List<Persona> deduplicarActivos(List<ContratoCc> contratos) {
        Map<Long, Persona> unicos = new LinkedHashMap<>();
        for (ContratoCc cc : contratos) {
            if (cc.getContrato() == null || cc.getContrato().getPersona() == null) {
                continue;
            }
            Persona persona = cc.getContrato().getPersona();
            if (persona.getId() != null) {
                unicos.putIfAbsent(persona.getId(), persona);
            }
        }
        return new ArrayList<>(unicos.values());
    }

    private static AlertaObraDTO alerta(String codigo, SeveridadAlertaObra severidad, String mensaje) {
        return AlertaObraDTO.builder()
                .codigo(codigo)
                .severidad(severidad)
                .mensaje(mensaje)
                .build();
    }
}
