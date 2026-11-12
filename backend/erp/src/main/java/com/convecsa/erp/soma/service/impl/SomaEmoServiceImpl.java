package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.common.exception.DocumentoObraNoEncontradoException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.domain.AccionAuditoriaObra;
import com.convecsa.erp.construccion.domain.TipoDocumentoObra;
import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
import com.convecsa.erp.persona.domain.AptitudMedica;
import com.convecsa.erp.persona.domain.EstadoDocumentoChecklist;
import com.convecsa.erp.persona.domain.TipoDocumentoChecklist;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.AptitudEmo;
import com.convecsa.erp.soma.dto.SomaEmoDTO;
import com.convecsa.erp.soma.dto.SomaEmoRequest;
import com.convecsa.erp.soma.entity.SomaEmoExamen;
import com.convecsa.erp.soma.repository.SomaEmoExamenRepository;
import com.convecsa.erp.soma.service.InterfazSomaEmoService;
import com.convecsa.erp.soma.support.ChecklistPersonaActualizador;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaEmoServiceImpl implements InterfazSomaEmoService {

    private final SomaEmoExamenRepository somaEmoExamenRepository;
    private final PersonaRepository personaRepository;
    private final ProyectoRepository proyectoRepository;
    private final DocumentoObraRepository documentoObraRepository;
    private final AuditoriaObraRegistrar auditoriaObraRegistrar;

    @Override
    @Transactional
    public SomaEmoDTO registrar(Long proyectoId, SomaEmoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El examen médico es obligatorio");
        }
        if (request.getPersonaId() == null) {
            throw new IllegalArgumentException("La persona es obligatoria");
        }
        if (request.getTipoEmo() == null) {
            throw new IllegalArgumentException("El tipo de EMO es obligatorio");
        }
        if (request.getAptitud() == null) {
            throw new IllegalArgumentException("La aptitud es obligatoria");
        }
        if (request.getFechaExamen() == null) {
            throw new IllegalArgumentException("La fecha de examen es obligatoria");
        }
        if (request.getFechaVencimiento() == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria");
        }
        if (request.getFechaVencimiento().isBefore(request.getFechaExamen())) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser menor que la de examen");
        }
        String clinicaNombre = exigirTexto(request.getClinicaNombre(), "El nombre de la clínica es obligatorio", 160);

        Persona persona = personaRepository.findByIdForUpdate(request.getPersonaId())
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
        Proyecto proyecto = null;
        if (proyectoId != null) {
            proyecto = proyectoRepository.findById(proyectoId)
                    .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
        }

        DocumentoObra documento = resolverDocumento(proyectoId, request.getDocumentoObraId());

        SomaEmoExamen examen = new SomaEmoExamen();
        examen.setPersona(persona);
        examen.setProyecto(proyecto);
        examen.setTipoEmo(request.getTipoEmo());
        examen.setClinicaNombre(clinicaNombre);
        examen.setClinicaRuc(limitar(request.getClinicaRuc(), 11, "El RUC de la clínica"));
        examen.setMedicoEvaluador(limitar(request.getMedicoEvaluador(), 160, "El médico evaluador"));
        examen.setCmpMedico(limitar(request.getCmpMedico(), 30, "El CMP del médico"));
        examen.setFechaExamen(request.getFechaExamen());
        examen.setFechaVencimiento(request.getFechaVencimiento());
        examen.setAptitud(request.getAptitud());
        examen.setRestricciones(limitar(request.getRestricciones(), 2000, "Las restricciones"));
        examen.setInterconsultas(limitar(request.getInterconsultas(), 1000, "Las interconsultas"));
        examen.setObservaciones(limitar(request.getObservaciones(), 1000, "Las observaciones"));
        examen.setDocumentoObra(documento);

        SomaEmoExamen guardado = somaEmoExamenRepository.save(examen);
        SomaEmoExamen ultimo = somaEmoExamenRepository
                .findFirstByPersonaIdOrderByFechaExamenDescIdDesc(persona.getId())
                .orElse(guardado);
        if (guardado.getId() != null && ultimo.getId() != null && guardado.getId().equals(ultimo.getId())) {
            sincronizarPersona(persona, guardado);
        }

        if (proyectoId != null) {
            auditoriaObraRegistrar.registrar(
                    proyectoId,
                    AccionAuditoriaObra.REGISTRAR_EMO_SOMA,
                    AuditoriaObraRegistrar.ENTIDAD_EMO_SOMA,
                    guardado.getId(),
                    "persona " + persona.getId() + ", " + guardado.getTipoEmo() + " " + guardado.getAptitud());
        }
        return toDto(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SomaEmoDTO> listarCorporativo(Long personaId, LocalDate vencenAntesDe) {
        return somaEmoExamenRepository.buscarCorporativo(personaId, vencenAntesDe).stream()
                .map(SomaEmoServiceImpl::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SomaEmoDTO> listarPorObra(Long proyectoId) {
        if (!proyectoRepository.existsById(proyectoId)) {
            throw new ProyectoNoEncontradoException("No se encontró el proyecto");
        }
        return somaEmoExamenRepository.findByProyectoIdOrderByFechaExamenDescIdDesc(proyectoId).stream()
                .map(SomaEmoServiceImpl::toDto)
                .toList();
    }

    public static Mapeo mapear(AptitudEmo aptitud, LocalDate fechaVencimiento, LocalDate hoy) {
        if (fechaVencimiento.isBefore(hoy)) {
            return new Mapeo(AptitudMedica.VENCIDO, EstadoDocumentoChecklist.PENDIENTE);
        }
        return switch (aptitud) {
            case APTO -> new Mapeo(AptitudMedica.APTO, EstadoDocumentoChecklist.CONFORME);
            case APTO_CON_RESTRICCION -> new Mapeo(AptitudMedica.OBSERVADO, EstadoDocumentoChecklist.CONFORME);
            case OBSERVADO, NO_APTO -> new Mapeo(AptitudMedica.OBSERVADO, EstadoDocumentoChecklist.OBSERVADO);
        };
    }

    private void sincronizarPersona(Persona persona, SomaEmoExamen ultimo) {
        Mapeo mapeo = mapear(ultimo.getAptitud(), ultimo.getFechaVencimiento(), LocalDate.now());
        persona.setFechaVigenciaEmo(ultimo.getFechaVencimiento());
        persona.setAptitudMedica(mapeo.aptitudMedica());
        persona.setRestriccionesMedicas(blankToNull(ultimo.getRestricciones()));
        ChecklistPersonaActualizador.aplicar(
                persona, TipoDocumentoChecklist.EMO_APTO_VIGENTE, mapeo.estadoChecklist());
    }

    private DocumentoObra resolverDocumento(Long proyectoId, Long documentoObraId) {
        if (documentoObraId == null) {
            return null;
        }
        if (proyectoId == null) {
            throw new IllegalArgumentException("El certificado CAMO requiere una obra");
        }
        DocumentoObra documento = documentoObraRepository.findByIdAndProyectoId(documentoObraId, proyectoId)
                .orElseThrow(() -> new DocumentoObraNoEncontradoException("No se encontró el documento de obra"));
        if (documento.getTipo() != TipoDocumentoObra.CERTIFICADO_EMO) {
            throw new IllegalArgumentException("El adjunto debe ser un certificado EMO");
        }
        return documento;
    }

    private static SomaEmoDTO toDto(SomaEmoExamen examen) {
        Persona persona = examen.getPersona();
        Proyecto proyecto = examen.getProyecto();
        DocumentoObra documento = examen.getDocumentoObra();
        return SomaEmoDTO.builder()
                .id(examen.getId())
                .personaId(persona == null ? null : persona.getId())
                .personaNombre(nombreCompleto(persona))
                .proyectoId(proyecto == null ? null : proyecto.getId())
                .tipoEmo(examen.getTipoEmo())
                .clinicaNombre(examen.getClinicaNombre())
                .clinicaRuc(examen.getClinicaRuc())
                .medicoEvaluador(examen.getMedicoEvaluador())
                .cmpMedico(examen.getCmpMedico())
                .fechaExamen(examen.getFechaExamen())
                .fechaVencimiento(examen.getFechaVencimiento())
                .aptitud(examen.getAptitud())
                .restricciones(examen.getRestricciones())
                .interconsultas(examen.getInterconsultas())
                .observaciones(examen.getObservaciones())
                .documentoObraId(documento == null ? null : documento.getId())
                .createdAt(examen.getCreatedAt())
                .build();
    }

    private static String nombreCompleto(Persona persona) {
        if (persona == null) {
            return null;
        }
        String apellidos = persona.getApellidos() == null ? "" : persona.getApellidos().trim();
        String nombres = persona.getNombres() == null ? "" : persona.getNombres().trim();
        String combinado = (apellidos + (apellidos.isEmpty() || nombres.isEmpty() ? "" : ", ") + nombres).trim();
        return combinado.isEmpty() ? null : combinado;
    }

    private static String exigirTexto(String valor, String mensaje, int maximo) {
        String limpio = blankToNull(valor);
        if (limpio == null) {
            throw new IllegalArgumentException(mensaje);
        }
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException("El texto no puede superar " + maximo + " caracteres");
        }
        return limpio;
    }

    private static String limitar(String valor, int maximo, String etiqueta) {
        String limpio = blankToNull(valor);
        if (limpio == null) {
            return null;
        }
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException(etiqueta + " no puede superar " + maximo + " caracteres");
        }
        return limpio;
    }

    private static String blankToNull(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record Mapeo(AptitudMedica aptitudMedica, EstadoDocumentoChecklist estadoChecklist) {}
}
