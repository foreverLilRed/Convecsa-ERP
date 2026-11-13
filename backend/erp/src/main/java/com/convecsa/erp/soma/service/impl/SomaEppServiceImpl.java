package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.common.exception.PersonaNoEncontradaException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.common.exception.SomaEntregaEppNoEncontradaException;
import com.convecsa.erp.construccion.domain.AccionAuditoriaObra;
import com.convecsa.erp.construccion.dto.AlmacenDTO;
import com.convecsa.erp.construccion.dto.StockItemDTO;
import com.convecsa.erp.construccion.dto.ValeSalidaDTO;
import com.convecsa.erp.construccion.dto.ValeSalidaItemRequest;
import com.convecsa.erp.construccion.dto.ValeSalidaRequest;
import com.convecsa.erp.construccion.entity.Material;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.entity.ValeSalida;
import com.convecsa.erp.construccion.repository.MaterialRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.repository.ValeSalidaRepository;
import com.convecsa.erp.construccion.service.InterfazAlmacenService;
import com.convecsa.erp.construccion.support.AuditoriaObraRegistrar;
import com.convecsa.erp.persona.entity.Persona;
import com.convecsa.erp.persona.repository.PersonaRepository;
import com.convecsa.erp.soma.domain.EstadoItemEpp;
import com.convecsa.erp.soma.dto.SomaEntregaEppDTO;
import com.convecsa.erp.soma.dto.SomaEntregaEppItemDTO;
import com.convecsa.erp.soma.dto.SomaEntregaEppItemRequest;
import com.convecsa.erp.soma.dto.SomaEntregaEppRequest;
import com.convecsa.erp.soma.dto.SomaEppItemEstadoRequest;
import com.convecsa.erp.soma.dto.SomaEppMaterialDTO;
import com.convecsa.erp.soma.entity.SomaEntregaEpp;
import com.convecsa.erp.soma.entity.SomaEntregaEppItem;
import com.convecsa.erp.soma.repository.SomaEntregaEppItemRepository;
import com.convecsa.erp.soma.repository.SomaEntregaEppRepository;
import com.convecsa.erp.soma.service.InterfazSomaEppService;
import com.convecsa.erp.soma.support.SomaEppKardexPdfService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaEppServiceImpl implements InterfazSomaEppService {

    private final SomaEntregaEppRepository somaEntregaEppRepository;
    private final SomaEntregaEppItemRepository somaEntregaEppItemRepository;
    private final ProyectoRepository proyectoRepository;
    private final PersonaRepository personaRepository;
    private final MaterialRepository materialRepository;
    private final ValeSalidaRepository valeSalidaRepository;
    private final InterfazAlmacenService almacenService;
    private final AuditoriaObraRegistrar auditoriaObraRegistrar;
    private final SomaEppKardexPdfService somaEppKardexPdfService;

    @Override
    @Transactional(readOnly = true)
    public List<SomaEntregaEppDTO> listar(Long proyectoId) {
        exigirProyecto(proyectoId);
        return somaEntregaEppRepository.findByProyectoIdOrderByFechaEntregaDescIdDesc(proyectoId).stream()
                .map(SomaEppServiceImpl::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SomaEntregaEppDTO crear(Long proyectoId, SomaEntregaEppRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La entrega de EPP es obligatoria");
        }
        if (request.getPersonaId() == null) {
            throw new IllegalArgumentException("La persona es obligatoria");
        }
        if (request.getFechaEntrega() == null) {
            throw new IllegalArgumentException("La fecha de entrega es obligatoria");
        }
        if (request.getMotivo() == null) {
            throw new IllegalArgumentException("El motivo es obligatorio");
        }
        List<SomaEntregaEppItemRequest> itemsRequest = request.getItems() == null ? List.of() : request.getItems();
        if (itemsRequest.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos un ítem");
        }

        Proyecto proyecto = exigirProyecto(proyectoId);
        Persona persona = personaRepository
                .findById(request.getPersonaId())
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));

        List<Long> materialIds = new ArrayList<>();
        for (SomaEntregaEppItemRequest item : itemsRequest) {
            if (item.getMaterialId() == null) {
                throw new IllegalArgumentException("El material es obligatorio");
            }
            if (item.getCantidad() == null || item.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
            }
            materialIds.add(item.getMaterialId());
        }
        Map<Long, Material> materiales = materialRepository.findAllById(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, Function.identity()));
        for (Long materialId : materialIds) {
            Material material = materiales.get(materialId);
            if (material == null) {
                throw new IllegalArgumentException("No se encontró el material");
            }
            if (!material.isEsEpp()) {
                throw new IllegalArgumentException("El material no está marcado como EPP");
            }
            if (!material.isActivo()) {
                throw new IllegalArgumentException("El material EPP no está activo");
            }
        }

        SomaEntregaEpp entrega = new SomaEntregaEpp();
        entrega.setProyecto(proyecto);
        entrega.setPersona(persona);
        entrega.setFechaEntrega(request.getFechaEntrega());
        entrega.setMotivo(request.getMotivo());
        entrega.setFirmadoTrabajador(Boolean.TRUE.equals(request.getFirmadoTrabajador()));
        entrega.setEntregadoPor(AuditoriaObraRegistrar.usuarioActual());
        entrega.setObservaciones(recortar(request.getObservaciones(), 400));

        for (SomaEntregaEppItemRequest itemRequest : itemsRequest) {
            Material material = materiales.get(itemRequest.getMaterialId());
            SomaEntregaEppItem item = new SomaEntregaEppItem();
            item.setMaterial(material);
            item.setCantidad(itemRequest.getCantidad());
            item.setTalla(recortar(itemRequest.getTalla(), 20));
            item.setEstadoItem(EstadoItemEpp.VIGENTE);
            item.setFechaVencimientoEstimada(calcularVencimiento(request.getFechaEntrega(), material.getVidaUtilDias()));
            entrega.addItem(item);
        }

        boolean descontar = !Boolean.FALSE.equals(request.getDescontarAlmacen());
        if (descontar) {
            if (request.getPartidaId() == null) {
                throw new IllegalArgumentException("La partida es obligatoria para descontar de almacén");
            }
            ValeSalidaRequest valeRequest = new ValeSalidaRequest();
            valeRequest.setFecha(request.getFechaEntrega());
            valeRequest.setPartidaId(request.getPartidaId());
            valeRequest.setObservaciones(observacionesVale(persona, request.getObservaciones()));
            for (SomaEntregaEppItemRequest itemRequest : itemsRequest) {
                ValeSalidaItemRequest valeItem = new ValeSalidaItemRequest();
                valeItem.setMaterialId(itemRequest.getMaterialId());
                valeItem.setCantidad(itemRequest.getCantidad());
                valeRequest.getItems().add(valeItem);
            }
            ValeSalidaDTO vale = almacenService.crearVale(proyectoId, valeRequest);
            ValeSalida valeRef = valeSalidaRepository.getReferenceById(vale.getId());
            entrega.setValeSalida(valeRef);
        }

        SomaEntregaEpp guardada = somaEntregaEppRepository.save(entrega);
        auditoriaObraRegistrar.registrar(
                proyectoId,
                AccionAuditoriaObra.ENTREGAR_EPP_SOMA,
                AuditoriaObraRegistrar.ENTIDAD_EPP_SOMA,
                guardada.getId(),
                "Entrega EPP a " + nombreCompleto(persona));
        return toDto(guardada);
    }

    @Override
    @Transactional
    public SomaEntregaEppDTO cambiarEstadoItem(Long proyectoId, Long itemId, SomaEppItemEstadoRequest request) {
        exigirProyecto(proyectoId);
        if (request == null || request.getEstado() == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        SomaEntregaEppItem item = somaEntregaEppItemRepository
                .findByIdAndEntregaProyectoId(itemId, proyectoId)
                .orElseThrow(() -> new SomaEntregaEppNoEncontradaException("No se encontró el ítem de EPP"));
        item.setEstadoItem(request.getEstado());
        somaEntregaEppItemRepository.save(item);
        return toDto(item.getEntrega());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SomaEppMaterialDTO> listarMateriales(Long proyectoId) {
        exigirProyecto(proyectoId);
        AlmacenDTO almacen = almacenService.obtenerAlmacen(proyectoId);
        Map<Long, BigDecimal> stockPorMaterial = new LinkedHashMap<>();
        if (almacen.getStock() != null) {
            for (StockItemDTO fila : almacen.getStock()) {
                stockPorMaterial.put(fila.getMaterialId(), fila.getCantidad());
            }
        }
        return materialRepository.findByEsEppTrueAndActivoTrueOrderByCodigoAsc().stream()
                .map(material -> SomaEppMaterialDTO.builder()
                        .materialId(material.getId())
                        .codigo(material.getCodigo())
                        .descripcion(material.getDescripcion())
                        .unidad(material.getUnidad())
                        .tipoEpp(material.getTipoEpp())
                        .vidaUtilDias(material.getVidaUtilDias())
                        .stock(stockPorMaterial.getOrDefault(material.getId(), BigDecimal.ZERO))
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] kardexPdf(Long proyectoId, Long personaId) {
        Proyecto proyecto = exigirProyecto(proyectoId);
        Persona persona = personaRepository
                .findById(personaId)
                .orElseThrow(() -> new PersonaNoEncontradaException("No se encontró la persona"));
        List<SomaEntregaEpp> entregas =
                somaEntregaEppRepository.findByProyectoIdAndPersonaIdOrderByFechaEntregaDescIdDesc(proyectoId, personaId);
        return somaEppKardexPdfService.generar(proyecto, persona, entregas);
    }

    private Proyecto exigirProyecto(Long proyectoId) {
        return proyectoRepository
                .findById(proyectoId)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private static LocalDate calcularVencimiento(LocalDate fechaEntrega, Integer vidaUtilDias) {
        if (vidaUtilDias == null) {
            return null;
        }
        return fechaEntrega.plusDays(vidaUtilDias);
    }

    private static String observacionesVale(Persona persona, String observaciones) {
        String base = "EPP " + nombreCompleto(persona);
        String extra = recortar(observaciones, 300);
        if (extra == null) {
            return recortar(base, 400);
        }
        return recortar(base + ". " + extra, 400);
    }

    private static SomaEntregaEppDTO toDto(SomaEntregaEpp entrega) {
        Persona persona = entrega.getPersona();
        return SomaEntregaEppDTO.builder()
                .id(entrega.getId())
                .proyectoId(entrega.getProyecto() == null ? null : entrega.getProyecto().getId())
                .personaId(persona == null ? null : persona.getId())
                .personaNombre(nombreCompleto(persona))
                .numeroDocumento(persona == null ? null : persona.getNumeroDocumento())
                .valeSalidaId(entrega.getValeSalida() == null ? null : entrega.getValeSalida().getId())
                .fechaEntrega(entrega.getFechaEntrega())
                .motivo(entrega.getMotivo())
                .firmadoTrabajador(entrega.isFirmadoTrabajador())
                .entregadoPor(entrega.getEntregadoPor())
                .observaciones(entrega.getObservaciones())
                .createdAt(entrega.getCreatedAt())
                .items(entrega.getItems().stream().map(SomaEppServiceImpl::toItemDto).toList())
                .build();
    }

    private static SomaEntregaEppItemDTO toItemDto(SomaEntregaEppItem item) {
        Material material = item.getMaterial();
        return SomaEntregaEppItemDTO.builder()
                .id(item.getId())
                .materialId(material == null ? null : material.getId())
                .materialCodigo(material == null ? null : material.getCodigo())
                .materialDescripcion(material == null ? null : material.getDescripcion())
                .cantidad(item.getCantidad())
                .talla(item.getTalla())
                .fechaVencimientoEstimada(item.getFechaVencimientoEstimada())
                .estadoItem(item.getEstadoItem())
                .build();
    }

    private static String nombreCompleto(Persona persona) {
        if (persona == null) {
            return "";
        }
        String apellidos = persona.getApellidos() == null ? "" : persona.getApellidos().trim();
        String nombres = persona.getNombres() == null ? "" : persona.getNombres().trim();
        return (apellidos + " " + nombres).trim();
    }

    private static String recortar(String valor, int maximo) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() <= maximo ? trimmed : trimmed.substring(0, maximo);
    }
}
