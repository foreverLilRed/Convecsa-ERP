package com.convecsa.erp.obra.service.impl;

import com.convecsa.erp.common.exception.ObraCodigoDuplicadoException;
import com.convecsa.erp.common.exception.ObraNoEncontradaException;
import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.obra.domain.EstadoObra;
import com.convecsa.erp.obra.dto.ObraDTO;
import com.convecsa.erp.obra.dto.ObraEstadoRequest;
import com.convecsa.erp.obra.dto.ObraRequest;
import com.convecsa.erp.obra.dto.ObraResumenDTO;
import com.convecsa.erp.obra.entity.Obra;
import com.convecsa.erp.obra.repository.ObraRepository;
import com.convecsa.erp.obra.service.InterfazObraService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ObraServiceImpl implements InterfazObraService {

    private static final Map<EstadoObra, Set<EstadoObra>> TRANSICIONES = Map.of(
            EstadoObra.BORRADOR, EnumSet.of(EstadoObra.PLANIFICADA, EstadoObra.CANCELADA),
            EstadoObra.PLANIFICADA, EnumSet.of(EstadoObra.EN_EJECUCION, EstadoObra.CANCELADA),
            EstadoObra.EN_EJECUCION, EnumSet.of(EstadoObra.SUSPENDIDA, EstadoObra.CERRADA),
            EstadoObra.SUSPENDIDA, EnumSet.of(EstadoObra.EN_EJECUCION, EstadoObra.CANCELADA),
            EstadoObra.CERRADA, EnumSet.noneOf(EstadoObra.class),
            EstadoObra.CANCELADA, EnumSet.noneOf(EstadoObra.class));

    private final ObraRepository obraRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ObraDTO> listar(int page, int size, String q, EstadoObra estado) {
        int safePage = Math.max(0, page);
        int safeSize = size < 1 ? 20 : Math.min(size, 100);
        boolean filtrarTexto = q != null && !q.isBlank();
        boolean filtrarEstado = estado != null;
        String query = filtrarTexto ? q.trim() : "";
        EstadoObra estadoConsulta = filtrarEstado ? estado : EstadoObra.BORRADOR;
        return PagedResponse.from(obraRepository.buscar(
                filtrarTexto, query,
                filtrarEstado, estadoConsulta,
                PageRequest.of(safePage, safeSize)).map(this::toDto));
    }

    @Override
    @Transactional(readOnly = true)
    public ObraResumenDTO resumen() {
        return new ObraResumenDTO(
                obraRepository.count(),
                obraRepository.countByEstado(EstadoObra.BORRADOR),
                obraRepository.countByEstado(EstadoObra.PLANIFICADA),
                obraRepository.countByEstado(EstadoObra.EN_EJECUCION),
                obraRepository.countByEstado(EstadoObra.SUSPENDIDA),
                obraRepository.countByEstado(EstadoObra.CERRADA),
                obraRepository.countByEstado(EstadoObra.CANCELADA));
    }

    @Override
    @Transactional(readOnly = true)
    public ObraDTO obtener(Long id) {
        return toDto(cargar(id));
    }

    @Override
    @Transactional
    public ObraDTO crear(ObraRequest request) {
        validarFechasPlanificadas(request);
        String codigo = normalizarCodigo(request.getCodigo());
        asegurarCodigoLibre(codigo, null);

        Obra obra = new Obra();
        aplicarDatos(obra, request, codigo);
        obra.setEstado(EstadoObra.BORRADOR);
        return toDto(obraRepository.save(obra));
    }

    @Override
    @Transactional
    public ObraDTO actualizar(Long id, ObraRequest request) {
        Obra obra = cargar(id);
        if (obra.getEstado() == EstadoObra.CERRADA || obra.getEstado() == EstadoObra.CANCELADA) {
            throw new IllegalArgumentException(
                    "No se puede editar una obra en estado " + obra.getEstado());
        }
        validarFechasPlanificadas(request);
        String codigo = normalizarCodigo(request.getCodigo());
        asegurarCodigoLibre(codigo, id);
        aplicarDatos(obra, request, codigo);
        return toDto(obraRepository.save(obra));
    }

    @Override
    @Transactional
    public ObraDTO cambiarEstado(Long id, ObraEstadoRequest request) {
        Obra obra = cargar(id);
        EstadoObra actual = obra.getEstado();
        EstadoObra destino = request.getEstado();
        if (destino == null) {
            throw new IllegalArgumentException("El estado destino es obligatorio");
        }
        if (destino == actual) {
            return toDto(obra);
        }

        Set<EstadoObra> permitidos = TRANSICIONES.getOrDefault(actual, EnumSet.noneOf(EstadoObra.class));
        if (!permitidos.contains(destino)) {
            throw new IllegalArgumentException(
                    "Transición no permitida: " + actual + " → " + destino);
        }

        String justificacion = trimToNull(request.getJustificacion());
        if ((destino == EstadoObra.SUSPENDIDA || destino == EstadoObra.CANCELADA)
                && justificacion == null) {
            throw new IllegalArgumentException(
                    "La justificación es obligatoria para suspender o cancelar la obra");
        }

        if (destino == EstadoObra.PLANIFICADA) {
            validarListoParaPlanificar(obra);
        }
        if (destino == EstadoObra.EN_EJECUCION) {
            if (obra.getFechaInicioReal() == null) {
                obra.setFechaInicioReal(LocalDate.now());
            }
        }
        if (destino == EstadoObra.CERRADA) {
            if (obra.getFechaInicioReal() == null) {
                throw new IllegalArgumentException(
                        "No se puede cerrar una obra sin fecha de inicio real");
            }
            if (obra.getFechaFinReal() == null) {
                obra.setFechaFinReal(LocalDate.now());
            }
        }

        obra.setEstado(destino);
        return toDto(obraRepository.save(obra));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Obra obra = cargar(id);
        if (obra.getEstado() != EstadoObra.BORRADOR) {
            throw new IllegalArgumentException("Solo se puede eliminar una obra en estado BORRADOR");
        }
        obraRepository.delete(obra);
    }

    private Obra cargar(Long id) {
        return obraRepository.findById(id)
                .orElseThrow(() -> new ObraNoEncontradaException("No se encontró la obra"));
    }

    private void asegurarCodigoLibre(String codigo, Long idActual) {
        boolean duplicado = idActual == null
                ? obraRepository.existsByCodigoIgnoreCase(codigo)
                : obraRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, idActual);
        if (duplicado) {
            throw new ObraCodigoDuplicadoException("Ya existe una obra con el código " + codigo);
        }
    }

    private void aplicarDatos(Obra obra, ObraRequest request, String codigo) {
        obra.setCodigo(codigo);
        obra.setNombre(request.getNombre().trim());
        obra.setClienteNombre(request.getClienteNombre().trim());
        obra.setContratistaPrincipal(trimToNull(request.getContratistaPrincipal()));
        obra.setUbicacion(trimToNull(request.getUbicacion()));
        obra.setFechaInicioPlanificada(request.getFechaInicioPlanificada());
        obra.setFechaFinPlanificada(request.getFechaFinPlanificada());
        obra.setFechaInicioReal(request.getFechaInicioReal());
        obra.setFechaFinReal(request.getFechaFinReal());
        obra.setMontoContratado(normalizarMonto(request.getMontoContratado()));
        obra.setMoneda(normalizarMoneda(request.getMoneda()));
        obra.setResponsableNombre(trimToNull(request.getResponsableNombre()));
    }

    private void validarListoParaPlanificar(Obra obra) {
        if (isBlank(obra.getCodigo())
                || isBlank(obra.getNombre())
                || isBlank(obra.getClienteNombre())
                || obra.getFechaInicioPlanificada() == null
                || obra.getFechaFinPlanificada() == null) {
            throw new IllegalArgumentException(
                    "Para planificar se requieren código, nombre, cliente y fechas planificadas");
        }
        if (obra.getFechaFinPlanificada().isBefore(obra.getFechaInicioPlanificada())) {
            throw new IllegalArgumentException(
                    "La fecha fin planificada no puede ser anterior al inicio planificado");
        }
    }

    private void validarFechasPlanificadas(ObraRequest request) {
        LocalDate inicio = request.getFechaInicioPlanificada();
        LocalDate fin = request.getFechaFinPlanificada();
        if (inicio != null && fin != null && fin.isBefore(inicio)) {
            throw new IllegalArgumentException(
                    "La fecha fin planificada no puede ser anterior al inicio planificado");
        }
        LocalDate inicioReal = request.getFechaInicioReal();
        LocalDate finReal = request.getFechaFinReal();
        if (inicioReal != null && finReal != null && finReal.isBefore(inicioReal)) {
            throw new IllegalArgumentException(
                    "La fecha fin real no puede ser anterior al inicio real");
        }
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarMoneda(String moneda) {
        if (moneda == null || moneda.isBlank()) {
            return "PEN";
        }
        String value = moneda.trim().toUpperCase(Locale.ROOT);
        if (value.length() != 3) {
            throw new IllegalArgumentException("La moneda debe tener 3 caracteres");
        }
        return value;
    }

    private BigDecimal normalizarMonto(BigDecimal monto) {
        if (monto == null) {
            return null;
        }
        if (monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto contratado no puede ser negativo");
        }
        return monto;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ObraDTO toDto(Obra obra) {
        return new ObraDTO(
                obra.getId(),
                obra.getCodigo(),
                obra.getNombre(),
                obra.getClienteNombre(),
                obra.getContratistaPrincipal(),
                obra.getUbicacion(),
                obra.getFechaInicioPlanificada(),
                obra.getFechaFinPlanificada(),
                obra.getFechaInicioReal(),
                obra.getFechaFinReal(),
                obra.getMontoContratado(),
                obra.getMoneda(),
                obra.getResponsableNombre(),
                obra.getEstado(),
                obra.getCreatedAt(),
                obra.getUpdatedAt());
    }
}
