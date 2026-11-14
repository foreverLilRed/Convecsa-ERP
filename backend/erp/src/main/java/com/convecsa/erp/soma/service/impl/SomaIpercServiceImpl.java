package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.PartidaPresupuestoRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.soma.domain.NivelRiesgoIperc;
import com.convecsa.erp.soma.dto.SomaIpercFilaDTO;
import com.convecsa.erp.soma.dto.SomaIpercFilaRequest;
import com.convecsa.erp.soma.entity.SomaIpercFila;
import com.convecsa.erp.soma.repository.SomaIpercFilaRepository;
import com.convecsa.erp.soma.service.InterfazSomaIpercService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaIpercServiceImpl implements InterfazSomaIpercService {

    private final SomaIpercFilaRepository somaIpercFilaRepository;
    private final ProyectoRepository proyectoRepository;
    private final PartidaPresupuestoRepository partidaPresupuestoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SomaIpercFilaDTO> listar(Long proyectoId, Long partidaId, Boolean incluirInactivos) {
        exigirProyecto(proyectoId);
        boolean inactivos = Boolean.TRUE.equals(incluirInactivos);
        return somaIpercFilaRepository.findByProyectoIdOrderByIdDesc(proyectoId).stream()
                .filter(fila -> inactivos || fila.isActivo())
                .filter(fila -> partidaId == null
                        || (fila.getPartida() != null && partidaId.equals(fila.getPartida().getId())))
                .map(SomaIpercServiceImpl::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SomaIpercFilaDTO crear(Long proyectoId, SomaIpercFilaRequest request) {
        Proyecto proyecto = exigirProyecto(proyectoId);
        SomaIpercFila fila = new SomaIpercFila();
        fila.setProyecto(proyecto);
        fila.setActivo(true);
        aplicar(fila, request, proyectoId);
        return toDto(somaIpercFilaRepository.save(fila));
    }

    @Override
    @Transactional
    public SomaIpercFilaDTO actualizar(Long proyectoId, Long filaId, SomaIpercFilaRequest request) {
        exigirProyecto(proyectoId);
        SomaIpercFila fila = exigirFila(proyectoId, filaId);
        aplicar(fila, request, proyectoId);
        return toDto(somaIpercFilaRepository.save(fila));
    }

    @Override
    @Transactional
    public SomaIpercFilaDTO desactivar(Long proyectoId, Long filaId) {
        exigirProyecto(proyectoId);
        SomaIpercFila fila = exigirFila(proyectoId, filaId);
        fila.setActivo(false);
        return toDto(somaIpercFilaRepository.save(fila));
    }

    private void aplicar(SomaIpercFila fila, SomaIpercFilaRequest request, Long proyectoId) {
        if (request == null) {
            throw new IllegalArgumentException("La fila IPERC es obligatoria");
        }
        fila.setPartida(resolverPartida(proyectoId, request.getPartidaId()));
        fila.setActividadTarea(exigirTexto(request.getActividadTarea(), "La actividad o tarea es obligatoria", 240));
        fila.setPuestoTrabajo(exigirTexto(request.getPuestoTrabajo(), "El puesto de trabajo es obligatorio", 120));
        fila.setRutinario(request.getRutinario() == null || request.getRutinario());
        if (request.getTipoPeligro() == null) {
            throw new IllegalArgumentException("El tipo de peligro es obligatorio");
        }
        fila.setTipoPeligro(request.getTipoPeligro());
        fila.setPeligro(exigirTexto(request.getPeligro(), "El peligro es obligatorio", 400));
        fila.setRiesgo(exigirTexto(request.getRiesgo(), "El riesgo es obligatorio", 400));
        fila.setConsecuencia(exigirTexto(request.getConsecuencia(), "La consecuencia es obligatoria", 400));
        fila.setRequisitoLegal(limitar(request.getRequisitoLegal(), 200, "El requisito legal"));

        int pInicial = exigirEscala(request.getProbabilidadInicial(), "La probabilidad inicial");
        int sInicial = exigirEscala(request.getSeveridadInicial(), "La severidad inicial");
        int pResidual = exigirEscala(request.getProbabilidadResidual(), "La probabilidad residual");
        int sResidual = exigirEscala(request.getSeveridadResidual(), "La severidad residual");

        String eliminacion = limitar(request.getControlEliminacion(), 500, "El control de eliminación");
        String sustitucion = limitar(request.getControlSustitucion(), 500, "El control de sustitución");
        String ingenieria = limitar(request.getControlIngenieria(), 500, "El control de ingeniería");
        String administrativo = limitar(request.getControlAdministrativo(), 500, "El control administrativo");
        String epp = limitar(request.getControlEpp(), 500, "El control de EPP");
        if (eliminacion == null
                && sustitucion == null
                && ingenieria == null
                && administrativo == null
                && epp == null) {
            throw new IllegalArgumentException("Debe indicar al menos un control");
        }
        fila.setControlEliminacion(eliminacion);
        fila.setControlSustitucion(sustitucion);
        fila.setControlIngenieria(ingenieria);
        fila.setControlAdministrativo(administrativo);
        fila.setControlEpp(epp);

        NivelRiesgoIperc nivelInicial = NivelRiesgoIperc.desdePuntaje(pInicial * sInicial);
        NivelRiesgoIperc nivelResidual = NivelRiesgoIperc.desdePuntaje(pResidual * sResidual);
        if (nivelResidual.ordinal() > nivelInicial.ordinal()) {
            throw new IllegalArgumentException("El nivel residual no puede ser mayor que el inicial");
        }
        fila.setProbabilidadInicial(pInicial);
        fila.setSeveridadInicial(sInicial);
        fila.setNivelRiesgoInicial(nivelInicial);
        fila.setProbabilidadResidual(pResidual);
        fila.setSeveridadResidual(sResidual);
        fila.setNivelRiesgoResidual(nivelResidual);
    }

    private PartidaPresupuesto resolverPartida(Long proyectoId, Long partidaId) {
        if (partidaId == null) {
            return null;
        }
        PartidaPresupuesto partida = partidaPresupuestoRepository
                .findById(partidaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la partida"));
        Long obraId = partida.getVersion() == null || partida.getVersion().getProyecto() == null
                ? null
                : partida.getVersion().getProyecto().getId();
        if (!proyectoId.equals(obraId)) {
            throw new IllegalArgumentException("La partida no pertenece a esta obra");
        }
        return partida;
    }

    private SomaIpercFila exigirFila(Long proyectoId, Long filaId) {
        if (filaId == null) {
            throw new IllegalArgumentException("La fila IPERC es obligatoria");
        }
        return somaIpercFilaRepository
                .findByIdAndProyectoId(filaId, proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la fila IPERC"));
    }

    private Proyecto exigirProyecto(Long proyectoId) {
        return proyectoRepository
                .findById(proyectoId)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private static SomaIpercFilaDTO toDto(SomaIpercFila fila) {
        PartidaPresupuesto partida = fila.getPartida();
        return SomaIpercFilaDTO.builder()
                .id(fila.getId())
                .proyectoId(fila.getProyecto() == null ? null : fila.getProyecto().getId())
                .partidaId(partida == null ? null : partida.getId())
                .partidaCodigo(partida == null ? null : partida.getCodigo())
                .partidaDescripcion(partida == null ? null : partida.getDescripcion())
                .actividadTarea(fila.getActividadTarea())
                .puestoTrabajo(fila.getPuestoTrabajo())
                .rutinario(fila.isRutinario())
                .tipoPeligro(fila.getTipoPeligro())
                .peligro(fila.getPeligro())
                .riesgo(fila.getRiesgo())
                .consecuencia(fila.getConsecuencia())
                .requisitoLegal(fila.getRequisitoLegal())
                .probabilidadInicial(fila.getProbabilidadInicial())
                .severidadInicial(fila.getSeveridadInicial())
                .nivelRiesgoInicial(fila.getNivelRiesgoInicial())
                .controlEliminacion(fila.getControlEliminacion())
                .controlSustitucion(fila.getControlSustitucion())
                .controlIngenieria(fila.getControlIngenieria())
                .controlAdministrativo(fila.getControlAdministrativo())
                .controlEpp(fila.getControlEpp())
                .probabilidadResidual(fila.getProbabilidadResidual())
                .severidadResidual(fila.getSeveridadResidual())
                .nivelRiesgoResidual(fila.getNivelRiesgoResidual())
                .activo(fila.isActivo())
                .createdAt(fila.getCreatedAt())
                .build();
    }

    private static int exigirEscala(Integer valor, String etiqueta) {
        if (valor == null || valor < 1 || valor > 5) {
            throw new IllegalArgumentException(etiqueta + " debe estar entre 1 y 5");
        }
        return valor;
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
}
