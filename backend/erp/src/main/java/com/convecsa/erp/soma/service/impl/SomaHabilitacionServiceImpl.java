package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.persona.domain.EstadoDocumentoChecklist;
import com.convecsa.erp.persona.domain.TipoDocumentoChecklist;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.entity.PersonaDocumentoChecklist;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.AptitudEmo;
import com.convecsa.erp.soma.domain.EstadoHabilitacionSoma;
import com.convecsa.erp.soma.domain.EstadoSctrPoliza;
import com.convecsa.erp.soma.domain.MotivoEntregaEpp;
import com.convecsa.erp.soma.domain.TipoCapacitacionSoma;
import com.convecsa.erp.soma.dto.SomaHabilitacionDTO;
import com.convecsa.erp.soma.dto.SomaHabilitacionFilaDTO;
import com.convecsa.erp.soma.entity.SomaEmoExamen;
import com.convecsa.erp.soma.entity.SomaSctrNomina;
import com.convecsa.erp.soma.entity.SomaSctrPoliza;
import com.convecsa.erp.soma.repository.SomaCapacitacionRepository;
import com.convecsa.erp.soma.repository.SomaEmoExamenRepository;
import com.convecsa.erp.soma.repository.SomaEntregaEppRepository;
import com.convecsa.erp.soma.repository.SomaSctrPolizaRepository;
import com.convecsa.erp.soma.repository.SomaTrabajadorObraRepository;
import com.convecsa.erp.soma.service.InterfazSomaHabilitacionService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaHabilitacionServiceImpl implements InterfazSomaHabilitacionService {

    static final String MOTIVO_SIN_EMO = "Sin examen médico ocupacional";
    static final String MOTIVO_EMO_VENCIDO = "EMO vencido";
    static final String MOTIVO_EMO_NO_APTO = "EMO no apto u observado";
    static final String MOTIVO_SIN_SCTR = "Sin cobertura SCTR del mes";
    static final String MOTIVO_SIN_INDUCCION = "Sin inducción de hombre nuevo aprobada";
    static final String MOTIVO_SIN_EPP = "Sin entrega inicial de EPP";
    static final String MOTIVO_RETCC = "Carnet RETCC no conforme";

    private final ProyectoRepository proyectoRepository;
    private final SomaTrabajadorObraRepository somaTrabajadorObraRepository;
    private final SomaEmoExamenRepository somaEmoExamenRepository;
    private final SomaSctrPolizaRepository somaSctrPolizaRepository;
    private final SomaCapacitacionRepository somaCapacitacionRepository;
    private final SomaEntregaEppRepository somaEntregaEppRepository;
    private final PersonaRepository personaRepository;

    @Override
    @Transactional(readOnly = true)
    public SomaHabilitacionDTO evaluarObra(Long proyectoId, LocalDate fecha) {
        if (!proyectoRepository.existsById(proyectoId)) {
            throw new ProyectoNoEncontradoException("No se encontró el proyecto");
        }
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        List<Persona> activos = deduplicarActivos(somaTrabajadorObraRepository.buscarActivos(proyectoId, dia, dia));
        if (activos.isEmpty()) {
            return SomaHabilitacionDTO.builder()
                    .fecha(dia)
                    .habilitados(0)
                    .bloqueados(0)
                    .emosPorVencer30Dias(0)
                    .filas(List.of())
                    .build();
        }

        Set<Long> ids = activos.stream().map(Persona::getId).collect(Collectors.toCollection(HashSet::new));
        Map<Long, Persona> personas = personaRepository.findAllWithChecklistByIdIn(ids).stream()
                .collect(Collectors.toMap(Persona::getId, Function.identity()));
        Set<Long> cubiertosSctr = cubiertosSctr(proyectoId, dia);

        List<SomaHabilitacionFilaDTO> filas = new ArrayList<>();
        int habilitados = 0;
        int bloqueados = 0;
        int emosPorVencer = 0;
        LocalDate limiteVence = dia.plusDays(30);

        for (Persona base : activos) {
            Persona persona = personas.getOrDefault(base.getId(), base);
            SomaEmoExamen emo = somaEmoExamenRepository
                    .findFirstByPersonaIdOrderByFechaExamenDescIdDesc(persona.getId())
                    .orElse(null);
            List<String> motivos = new ArrayList<>();
            evaluarEmo(emo, dia, motivos);
            if (!cubiertosSctr.contains(persona.getId())) {
                motivos.add(MOTIVO_SIN_SCTR);
            }
            if (!somaCapacitacionRepository.existsByProyectoIdAndTipoAndAsistentesPersonaIdAndAsistentesAprobadoTrue(
                    proyectoId, TipoCapacitacionSoma.INDUCCION_HOMBRE_NUEVO, persona.getId())) {
                motivos.add(MOTIVO_SIN_INDUCCION);
            }
            if (!somaEntregaEppRepository.existsByProyectoIdAndPersonaIdAndMotivo(
                    proyectoId, persona.getId(), MotivoEntregaEpp.DOTACION_INICIAL)) {
                motivos.add(MOTIVO_SIN_EPP);
            }
            if (!retccOk(persona)) {
                motivos.add(MOTIVO_RETCC);
            }

            EstadoHabilitacionSoma estado =
                    motivos.isEmpty() ? EstadoHabilitacionSoma.HABILITADO : EstadoHabilitacionSoma.BLOQUEADO;
            if (estado == EstadoHabilitacionSoma.HABILITADO) {
                habilitados++;
            } else {
                bloqueados++;
            }
            if (emoPorVencer(emo, dia, limiteVence)) {
                emosPorVencer++;
            }

            filas.add(SomaHabilitacionFilaDTO.builder()
                    .personaId(persona.getId())
                    .nombres(persona.getNombres())
                    .apellidos(persona.getApellidos())
                    .numeroDocumento(persona.getNumeroDocumento())
                    .estado(estado)
                    .motivos(List.copyOf(motivos))
                    .restricciones(restriccionesDe(emo))
                    .build());
        }

        return SomaHabilitacionDTO.builder()
                .fecha(dia)
                .habilitados(habilitados)
                .bloqueados(bloqueados)
                .emosPorVencer30Dias(emosPorVencer)
                .filas(filas)
                .build();
    }

    private Set<Long> cubiertosSctr(Long proyectoId, LocalDate fecha) {
        return somaSctrPolizaRepository
                .findByProyectoIdAndAnioAndMesAndEstado(
                        proyectoId, fecha.getYear(), fecha.getMonthValue(), EstadoSctrPoliza.VIGENTE)
                .map(SomaHabilitacionServiceImpl::idsCubiertos)
                .orElse(Set.of());
    }

    private static Set<Long> idsCubiertos(SomaSctrPoliza poliza) {
        Set<Long> ids = new HashSet<>();
        for (SomaSctrNomina fila : poliza.getNomina()) {
            if (fila.isIncluidoSalud()
                    && fila.isIncluidoPension()
                    && fila.getPersona() != null
                    && fila.getPersona().getId() != null) {
                ids.add(fila.getPersona().getId());
            }
        }
        return ids;
    }

    private static void evaluarEmo(SomaEmoExamen emo, LocalDate fecha, List<String> motivos) {
        if (emo == null) {
            motivos.add(MOTIVO_SIN_EMO);
            return;
        }
        if (emo.getFechaVencimiento() == null || emo.getFechaVencimiento().isBefore(fecha)) {
            motivos.add(MOTIVO_EMO_VENCIDO);
        }
        if (emo.getAptitud() == AptitudEmo.OBSERVADO || emo.getAptitud() == AptitudEmo.NO_APTO) {
            motivos.add(MOTIVO_EMO_NO_APTO);
        }
    }

    private static boolean emoPorVencer(SomaEmoExamen emo, LocalDate fecha, LocalDate limite) {
        if (emo == null || emo.getFechaVencimiento() == null) {
            return false;
        }
        LocalDate vencimiento = emo.getFechaVencimiento();
        return !vencimiento.isBefore(fecha) && !vencimiento.isAfter(limite);
    }

    private static String restriccionesDe(SomaEmoExamen emo) {
        if (emo == null || emo.getAptitud() != AptitudEmo.APTO_CON_RESTRICCION) {
            return null;
        }
        String texto = emo.getRestricciones();
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    private static boolean retccOk(Persona persona) {
        return persona.getDocumentosChecklist().stream()
                .filter(item -> item.getTipoDocumento() == TipoDocumentoChecklist.CARNET_RETCC)
                .map(PersonaDocumentoChecklist::getEstado)
                .anyMatch(estado ->
                        estado == EstadoDocumentoChecklist.CONFORME || estado == EstadoDocumentoChecklist.NO_APLICA);
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
}
