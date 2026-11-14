package com.convecsa.erp.soma.service.impl;

import com.convecsa.erp.common.exception.DocumentoObraNoEncontradoException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.soma.domain.CategoriaResiduoSoma;
import com.convecsa.erp.soma.domain.TipoAgenteMonitoreoSoma;
import com.convecsa.erp.soma.dto.SomaMonitoreoDTO;
import com.convecsa.erp.soma.dto.SomaMonitoreoRequest;
import com.convecsa.erp.soma.dto.SomaResiduoDTO;
import com.convecsa.erp.soma.dto.SomaResiduoRequest;
import com.convecsa.erp.soma.entity.SomaMonitoreo;
import com.convecsa.erp.soma.entity.SomaResiduoRegistro;
import com.convecsa.erp.soma.repository.SomaMonitoreoRepository;
import com.convecsa.erp.soma.repository.SomaResiduoRegistroRepository;
import com.convecsa.erp.soma.service.InterfazSomaAmbientalService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SomaAmbientalServiceImpl implements InterfazSomaAmbientalService {

    private final SomaResiduoRegistroRepository somaResiduoRegistroRepository;
    private final SomaMonitoreoRepository somaMonitoreoRepository;
    private final ProyectoRepository proyectoRepository;
    private final DocumentoObraRepository documentoObraRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SomaResiduoDTO> listarResiduos(Long proyectoId) {
        exigirProyecto(proyectoId);
        return somaResiduoRegistroRepository.findByProyectoIdOrderByFechaDescIdDesc(proyectoId).stream()
                .map(SomaAmbientalServiceImpl::toResiduoDto)
                .toList();
    }

    @Override
    @Transactional
    public SomaResiduoDTO crearResiduo(Long proyectoId, SomaResiduoRequest request) {
        Proyecto proyecto = exigirProyecto(proyectoId);
        if (request == null) {
            throw new IllegalArgumentException("El residuo es obligatorio");
        }
        if (request.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (request.getCategoriaResiduo() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        BigDecimal cantidadKg = cantidadPositivaONula(request.getCantidadKg(), "La cantidad en kg");
        BigDecimal volumenM3 = cantidadPositivaONula(request.getVolumenM3(), "El volumen en m³");
        if (cantidadKg == null && volumenM3 == null) {
            throw new IllegalArgumentException("Indique una cantidad en kg o un volumen en m³ mayor que cero");
        }

        String eorsRazonSocial = limitar(request.getEorsRazonSocial(), 160, "La razón social del EO-RS");
        String eorsRuc = rucOpcional(request.getEorsRuc());
        String numeroManifiesto = limitar(request.getNumeroManifiesto(), 60, "El número de manifiesto");
        if (request.getCategoriaResiduo() == CategoriaResiduoSoma.PELIGROSO_ROJO) {
            if (eorsRazonSocial == null) {
                throw new IllegalArgumentException("La razón social del EO-RS es obligatoria");
            }
            if (eorsRuc == null) {
                throw new IllegalArgumentException("El RUC del EO-RS es obligatorio");
            }
            if (numeroManifiesto == null) {
                throw new IllegalArgumentException("El número de manifiesto es obligatorio");
            }
        }

        SomaResiduoRegistro residuo = new SomaResiduoRegistro();
        residuo.setProyecto(proyecto);
        residuo.setFecha(request.getFecha());
        residuo.setCategoriaResiduo(request.getCategoriaResiduo());
        residuo.setDescripcion(exigirTexto(request.getDescripcion(), "La descripción es obligatoria", 240));
        residuo.setCantidadKg(cantidadKg);
        residuo.setVolumenM3(volumenM3);
        residuo.setEorsRazonSocial(eorsRazonSocial);
        residuo.setEorsRuc(eorsRuc);
        residuo.setNumeroManifiesto(numeroManifiesto);
        residuo.setDestinoFinal(limitar(request.getDestinoFinal(), 160, "El destino final"));
        residuo.setManifiestoDoc(resolverDocumento(proyectoId, request.getManifiestoDocId()));
        return toResiduoDto(somaResiduoRegistroRepository.save(residuo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SomaMonitoreoDTO> listarMonitoreos(Long proyectoId) {
        exigirProyecto(proyectoId);
        return somaMonitoreoRepository.findByProyectoIdOrderByFechaDescIdDesc(proyectoId).stream()
                .map(SomaAmbientalServiceImpl::toMonitoreoDto)
                .toList();
    }

    @Override
    @Transactional
    public SomaMonitoreoDTO crearMonitoreo(Long proyectoId, SomaMonitoreoRequest request) {
        Proyecto proyecto = exigirProyecto(proyectoId);
        if (request == null) {
            throw new IllegalArgumentException("El monitoreo es obligatorio");
        }
        if (request.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (request.getTipoAgente() == null) {
            throw new IllegalArgumentException("El tipo de agente es obligatorio");
        }
        if (request.getValorMedido() == null) {
            throw new IllegalArgumentException("El valor medido es obligatorio");
        }
        if (request.getLimitePermisible() == null) {
            throw new IllegalArgumentException("El límite permisible es obligatorio");
        }

        SomaMonitoreo monitoreo = new SomaMonitoreo();
        monitoreo.setProyecto(proyecto);
        monitoreo.setFecha(request.getFecha());
        monitoreo.setTipoAgente(request.getTipoAgente());
        monitoreo.setParametroMedido(exigirTexto(request.getParametroMedido(), "El parámetro medido es obligatorio", 120));
        monitoreo.setPuntoMedicion(exigirTexto(request.getPuntoMedicion(), "El punto de medición es obligatorio", 160));
        monitoreo.setValorMedido(request.getValorMedido());
        monitoreo.setLimitePermisible(request.getLimitePermisible());
        monitoreo.setUnidad(exigirTexto(request.getUnidad(), "La unidad es obligatoria", 30));
        monitoreo.setCumpleLmp(TipoAgenteMonitoreoSoma.cumpleLmp(
                request.getTipoAgente(), request.getValorMedido(), request.getLimitePermisible()));
        monitoreo.setLaboratorioNombre(limitar(request.getLaboratorioNombre(), 160, "El laboratorio"));
        monitoreo.setInformeDoc(resolverDocumento(proyectoId, request.getInformeDocId()));
        return toMonitoreoDto(somaMonitoreoRepository.save(monitoreo));
    }

    private DocumentoObra resolverDocumento(Long proyectoId, Long documentoObraId) {
        if (documentoObraId == null) {
            return null;
        }
        return documentoObraRepository
                .findByIdAndProyectoId(documentoObraId, proyectoId)
                .orElseThrow(() -> new DocumentoObraNoEncontradoException("No se encontró el documento de obra"));
    }

    private Proyecto exigirProyecto(Long proyectoId) {
        return proyectoRepository
                .findById(proyectoId)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private static SomaResiduoDTO toResiduoDto(SomaResiduoRegistro residuo) {
        Proyecto proyecto = residuo.getProyecto();
        DocumentoObra documento = residuo.getManifiestoDoc();
        return SomaResiduoDTO.builder()
                .id(residuo.getId())
                .proyectoId(proyecto == null ? null : proyecto.getId())
                .fecha(residuo.getFecha())
                .categoriaResiduo(residuo.getCategoriaResiduo())
                .descripcion(residuo.getDescripcion())
                .cantidadKg(residuo.getCantidadKg())
                .volumenM3(residuo.getVolumenM3())
                .eorsRazonSocial(residuo.getEorsRazonSocial())
                .eorsRuc(residuo.getEorsRuc())
                .numeroManifiesto(residuo.getNumeroManifiesto())
                .destinoFinal(residuo.getDestinoFinal())
                .manifiestoDocId(documento == null ? null : documento.getId())
                .createdAt(residuo.getCreatedAt())
                .build();
    }

    private static SomaMonitoreoDTO toMonitoreoDto(SomaMonitoreo monitoreo) {
        Proyecto proyecto = monitoreo.getProyecto();
        DocumentoObra documento = monitoreo.getInformeDoc();
        return SomaMonitoreoDTO.builder()
                .id(monitoreo.getId())
                .proyectoId(proyecto == null ? null : proyecto.getId())
                .fecha(monitoreo.getFecha())
                .tipoAgente(monitoreo.getTipoAgente())
                .parametroMedido(monitoreo.getParametroMedido())
                .puntoMedicion(monitoreo.getPuntoMedicion())
                .valorMedido(monitoreo.getValorMedido())
                .limitePermisible(monitoreo.getLimitePermisible())
                .unidad(monitoreo.getUnidad())
                .cumpleLmp(monitoreo.isCumpleLmp())
                .laboratorioNombre(monitoreo.getLaboratorioNombre())
                .informeDocId(documento == null ? null : documento.getId())
                .createdAt(monitoreo.getCreatedAt())
                .build();
    }

    private static BigDecimal cantidadPositivaONula(BigDecimal valor, String etiqueta) {
        if (valor == null) {
            return null;
        }
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(etiqueta + " debe ser mayor que cero");
        }
        if (valor.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return valor;
    }

    private static String rucOpcional(String ruc) {
        String valor = blankToNull(ruc);
        if (valor == null) {
            return null;
        }
        if (!valor.matches("\\d{11}")) {
            throw new IllegalArgumentException("El RUC del EO-RS debe tener 11 dígitos");
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
