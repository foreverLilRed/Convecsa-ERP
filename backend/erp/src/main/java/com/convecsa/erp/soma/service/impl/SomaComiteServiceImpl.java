package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.common.exception.DocumentoObraNoEncontradoException;
import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.entity.ContratoCc;
import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.CargoComiteSoma;
import com.convecsa.erp.soma.domain.RepresentacionComiteSoma;
import com.convecsa.erp.soma.domain.TipoRequerimientoComiteSoma;
import com.convecsa.erp.soma.dto.SomaComiteActaDTO;
import com.convecsa.erp.soma.dto.SomaComiteActaRequest;
import com.convecsa.erp.soma.dto.SomaComiteMiembroDTO;
import com.convecsa.erp.soma.dto.SomaComiteMiembroRequest;
import com.convecsa.erp.soma.dto.SomaComiteRequerimientoDTO;
import com.convecsa.erp.soma.entity.SomaComiteActa;
import com.convecsa.erp.soma.entity.SomaComiteMiembro;
import com.convecsa.erp.soma.repository.SomaComiteActaRepository;
import com.convecsa.erp.soma.repository.SomaComiteMiembroRepository;
import com.convecsa.erp.soma.repository.SomaTrabajadorObraRepository;
import com.convecsa.erp.soma.service.InterfazSomaComiteService;
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
public class SomaComiteServiceImpl implements InterfazSomaComiteService {

    static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");

    private final SomaComiteMiembroRepository somaComiteMiembroRepository;
    private final SomaComiteActaRepository somaComiteActaRepository;
    private final SomaTrabajadorObraRepository somaTrabajadorObraRepository;
    private final ProyectoRepository proyectoRepository;
    private final PersonaRepository personaRepository;
    private final DocumentoObraRepository documentoObraRepository;

    @Override
    @Transactional(readOnly = true)
    public SomaComiteRequerimientoDTO requerimiento(Long proyectoId) {
        exigirProyecto(proyectoId);
        LocalDate hoy = LocalDate.now(ZONA_LIMA);
        int trabajadoresActivos = deduplicarActivos(
                        somaTrabajadorObraRepository.buscarActivos(proyectoId, hoy, hoy))
                .size();
        TipoRequerimientoComiteSoma tipo = TipoRequerimientoComiteSoma.desdeActivos(trabajadoresActivos);
        List<SomaComiteMiembro> miembros =
                somaComiteMiembroRepository.findByProyectoIdOrderByActivoDescFechaInicioDescIdDesc(proyectoId);

        int titularesEmpleador = contarTitulares(miembros, RepresentacionComiteSoma.EMPLEADOR);
        int titularesTrabajadores = contarTitulares(miembros, RepresentacionComiteSoma.TRABAJADORES);
        int supervisoresActivos = (int) miembros.stream()
                .filter(SomaComiteMiembro::isActivo)
                .filter(miembro -> miembro.getCargoComite() == CargoComiteSoma.SUPERVISOR_SST)
                .count();

        List<String> avisos = new ArrayList<>();
        if (tipo == TipoRequerimientoComiteSoma.SUBCOMITE) {
            if (titularesEmpleador != titularesTrabajadores) {
                avisos.add("La representación no es paritaria");
            }
            if (!hayCargoActivo(miembros, CargoComiteSoma.PRESIDENTE)) {
                avisos.add("Falta un presidente activo");
            }
            if (!hayCargoActivo(miembros, CargoComiteSoma.SECRETARIO)) {
                avisos.add("Falta un secretario activo");
            }
        } else if (supervisoresActivos != 1) {
            avisos.add("Se espera un único supervisor SST activo");
        }

        return SomaComiteRequerimientoDTO.builder()
                .tipoRequerido(tipo)
                .trabajadoresActivos(trabajadoresActivos)
                .titularesEmpleador(titularesEmpleador)
                .titularesTrabajadores(titularesTrabajadores)
                .supervisoresActivos(supervisoresActivos)
                .avisos(List.copyOf(avisos))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SomaComiteMiembroDTO> listarMiembros(Long proyectoId) {
        exigirProyecto(proyectoId);
        return somaComiteMiembroRepository.findByProyectoIdOrderByActivoDescFechaInicioDescIdDesc(proyectoId).stream()
                .map(SomaComiteServiceImpl::toMiembroDto)
                .toList();
    }

    @Override
    @Transactional
    public SomaComiteMiembroDTO crearMiembro(Long proyectoId, SomaComiteMiembroRequest request) {
        Proyecto proyecto = exigirProyecto(proyectoId);
        if (request == null) {
            throw new IllegalArgumentException("El miembro es obligatorio");
        }
        if (request.getPersonaId() == null) {
            throw new IllegalArgumentException("La persona es obligatoria");
        }
        if (request.getRepresentacion() == null) {
            throw new IllegalArgumentException("La representación es obligatoria");
        }
        if (request.getCargoComite() == null) {
            throw new IllegalArgumentException("El cargo es obligatorio");
        }
        if (request.getFechaInicio() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }
        if (request.getFechaFin() != null && request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser menor que la de inicio");
        }
        Persona persona = personaRepository
                .findById(request.getPersonaId())
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));

        SomaComiteMiembro miembro = new SomaComiteMiembro();
        miembro.setProyecto(proyecto);
        miembro.setPersona(persona);
        miembro.setRepresentacion(request.getRepresentacion());
        miembro.setCargoComite(request.getCargoComite());
        miembro.setTitular(request.getTitular() == null || request.getTitular());
        miembro.setFechaInicio(request.getFechaInicio());
        miembro.setFechaFin(request.getFechaFin());
        miembro.setActivo(true);
        return toMiembroDto(somaComiteMiembroRepository.save(miembro));
    }

    @Override
    @Transactional
    public SomaComiteMiembroDTO darBaja(Long proyectoId, Long miembroId) {
        exigirProyecto(proyectoId);
        SomaComiteMiembro miembro = exigirMiembro(proyectoId, miembroId);
        if (!miembro.isActivo()) {
            throw new IllegalArgumentException("El miembro ya está dado de baja");
        }
        miembro.setActivo(false);
        miembro.setFechaFin(LocalDate.now(ZONA_LIMA));
        return toMiembroDto(somaComiteMiembroRepository.save(miembro));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SomaComiteActaDTO> listarActas(Long proyectoId) {
        exigirProyecto(proyectoId);
        return somaComiteActaRepository.findByProyectoIdOrderByFechaDescIdDesc(proyectoId).stream()
                .map(SomaComiteServiceImpl::toActaDto)
                .toList();
    }

    @Override
    @Transactional
    public SomaComiteActaDTO crearActa(Long proyectoId, SomaComiteActaRequest request) {
        Proyecto proyecto = exigirProyecto(proyectoId);
        if (request == null) {
            throw new IllegalArgumentException("El acta es obligatoria");
        }
        String numeroActa = exigirTexto(request.getNumeroActa(), "El número de acta es obligatorio", 40);
        if (somaComiteActaRepository.findByProyectoIdAndNumeroActa(proyectoId, numeroActa).isPresent()) {
            throw new IllegalArgumentException("Ya existe un acta con ese número");
        }
        if (request.getTipoSesion() == null) {
            throw new IllegalArgumentException("El tipo de sesión es obligatorio");
        }
        if (request.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }

        SomaComiteActa acta = new SomaComiteActa();
        acta.setProyecto(proyecto);
        acta.setNumeroActa(numeroActa);
        acta.setTipoSesion(request.getTipoSesion());
        acta.setFecha(request.getFecha());
        acta.setAgenda(exigirTexto(request.getAgenda(), "La agenda es obligatoria", 1000));
        acta.setAcuerdos(exigirTexto(request.getAcuerdos(), "Los acuerdos son obligatorios", 3000));
        acta.setActaDoc(resolverDocumento(proyectoId, request.getActaDocId()));
        return toActaDto(somaComiteActaRepository.save(acta));
    }

    private DocumentoObra resolverDocumento(Long proyectoId, Long documentoObraId) {
        if (documentoObraId == null) {
            return null;
        }
        return documentoObraRepository
                .findByIdAndProyectoId(documentoObraId, proyectoId)
                .orElseThrow(() -> new DocumentoObraNoEncontradoException("No se encontró el documento de obra"));
    }

    private SomaComiteMiembro exigirMiembro(Long proyectoId, Long miembroId) {
        if (miembroId == null) {
            throw new IllegalArgumentException("No se encontró el miembro");
        }
        return somaComiteMiembroRepository
                .findByIdAndProyectoId(miembroId, proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el miembro"));
    }

    private Proyecto exigirProyecto(Long proyectoId) {
        return proyectoRepository
                .findById(proyectoId)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private static int contarTitulares(List<SomaComiteMiembro> miembros, RepresentacionComiteSoma representacion) {
        return (int) miembros.stream()
                .filter(SomaComiteMiembro::isActivo)
                .filter(SomaComiteMiembro::isTitular)
                .filter(miembro -> miembro.getRepresentacion() == representacion)
                .count();
    }

    private static boolean hayCargoActivo(List<SomaComiteMiembro> miembros, CargoComiteSoma cargo) {
        return miembros.stream()
                .anyMatch(miembro -> miembro.isActivo() && miembro.getCargoComite() == cargo);
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

    private static SomaComiteMiembroDTO toMiembroDto(SomaComiteMiembro miembro) {
        Persona persona = miembro.getPersona();
        Proyecto proyecto = miembro.getProyecto();
        return SomaComiteMiembroDTO.builder()
                .id(miembro.getId())
                .proyectoId(proyecto == null ? null : proyecto.getId())
                .personaId(persona == null ? null : persona.getId())
                .personaNombre(nombreCompleto(persona))
                .representacion(miembro.getRepresentacion())
                .cargoComite(miembro.getCargoComite())
                .titular(miembro.isTitular())
                .fechaInicio(miembro.getFechaInicio())
                .fechaFin(miembro.getFechaFin())
                .activo(miembro.isActivo())
                .build();
    }

    private static SomaComiteActaDTO toActaDto(SomaComiteActa acta) {
        Proyecto proyecto = acta.getProyecto();
        DocumentoObra documento = acta.getActaDoc();
        return SomaComiteActaDTO.builder()
                .id(acta.getId())
                .proyectoId(proyecto == null ? null : proyecto.getId())
                .numeroActa(acta.getNumeroActa())
                .tipoSesion(acta.getTipoSesion())
                .fecha(acta.getFecha())
                .agenda(acta.getAgenda())
                .acuerdos(acta.getAcuerdos())
                .actaDocId(documento == null ? null : documento.getId())
                .createdAt(acta.getCreatedAt())
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

    private static String blankToNull(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
