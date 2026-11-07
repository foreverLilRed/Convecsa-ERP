package com.convecsa.erp.construccion.service.impl;

import com.convecsa.erp.common.exception.DocumentoObraNoEncontradoException;
import com.convecsa.erp.common.exception.ProyectoNoEncontradoException;
import com.convecsa.erp.construccion.domain.EstadoFacturaObra;
import com.convecsa.erp.construccion.domain.EstadoFactoring;
import com.convecsa.erp.construccion.domain.EstadoVersionPresupuesto;
import com.convecsa.erp.construccion.domain.SeveridadAlertaObra;
import com.convecsa.erp.construccion.domain.TipoDocumentoObra;
import com.convecsa.erp.construccion.dto.AlertaObraDTO;
import com.convecsa.erp.construccion.dto.AlmacenDTO;
import com.convecsa.erp.construccion.dto.ComparativoPresupuestoDTO;
import com.convecsa.erp.construccion.dto.DocumentoObraDTO;
import com.convecsa.erp.construccion.entity.CcParametroAnual;
import com.convecsa.erp.construccion.entity.DocumentoObra;
import com.convecsa.erp.construccion.entity.FacturaObra;
import com.convecsa.erp.construccion.entity.OperacionFactoring;
import com.convecsa.erp.construccion.entity.Proyecto;
import com.convecsa.erp.construccion.repository.AlmacenObraRepository;
import com.convecsa.erp.construccion.repository.CcParametroAnualRepository;
import com.convecsa.erp.construccion.repository.DocumentoObraRepository;
import com.convecsa.erp.construccion.repository.FacturaObraRepository;
import com.convecsa.erp.construccion.repository.OperacionFactoringRepository;
import com.convecsa.erp.construccion.repository.PresupuestoVersionRepository;
import com.convecsa.erp.construccion.repository.ProyectoRepository;
import com.convecsa.erp.construccion.service.InterfazAlmacenService;
import com.convecsa.erp.construccion.service.InterfazConstruccionService;
import com.convecsa.erp.construccion.service.InterfazTransversalObraService;
import com.convecsa.erp.construccion.support.DocumentoObraStorage;
import com.convecsa.erp.construccion.support.UitObraAlerta;
import com.convecsa.erp.nomina.declaracion.dto.ArchivoDescargaDTO;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TransversalObraServiceImpl implements InterfazTransversalObraService {

    static final long TAMANO_MAXIMO = 15L * 1024 * 1024;
    static final int DIAS_POR_VENCER = 7;
    static final BigDecimal DESVIO_PCT = new BigDecimal("10");
    private static final Set<String> MIMES = Set.of("application/pdf", "image/jpeg", "image/png");
    private static final Set<EstadoFacturaObra> CARTERA = Set.of(
            EstadoFacturaObra.EMITIDA, EstadoFacturaObra.PARCIAL);

    private final ProyectoRepository proyectoRepository;
    private final DocumentoObraRepository documentoObraRepository;
    private final DocumentoObraStorage documentoObraStorage;
    private final CcParametroAnualRepository parametroRepository;
    private final AlmacenObraRepository almacenObraRepository;
    private final InterfazAlmacenService almacenService;
    private final FacturaObraRepository facturaObraRepository;
    private final OperacionFactoringRepository operacionFactoringRepository;
    private final PresupuestoVersionRepository presupuestoVersionRepository;
    private final InterfazConstruccionService construccionService;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoObraDTO> listarDocumentos(Long proyectoId) {
        buscarProyecto(proyectoId);
        return documentoObraRepository.findByProyectoIdOrderByCreatedAtDescIdDesc(proyectoId).stream()
                .map(TransversalObraServiceImpl::toDto)
                .toList();
    }

    @Override
    @Transactional
    public DocumentoObraDTO subirDocumento(
            Long proyectoId, TipoDocumentoObra tipo, MultipartFile archivo, String observaciones) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        if (tipo == null) {
            throw new IllegalArgumentException("Indique el tipo de documento");
        }
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Adjunte un archivo");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("El archivo no puede superar 15 MB");
        }
        String mime = normalizarMime(archivo.getContentType(), archivo.getOriginalFilename());
        byte[] contenido;
        try {
            contenido = archivo.getBytes();
        } catch (IOException ex) {
            throw new IllegalArgumentException("No se pudo leer el archivo");
        }
        if (contenido.length == 0) {
            throw new IllegalArgumentException("Adjunte un archivo");
        }
        String nombre = blankToNull(archivo.getOriginalFilename());
        if (nombre == null) {
            nombre = "documento";
        }
        if (nombre.length() > 240) {
            nombre = nombre.substring(nombre.length() - 240);
        }
        String ruta = documentoObraStorage.guardar(proyectoId, nombre, contenido);
        DocumentoObra documento = new DocumentoObra();
        documento.setProyecto(proyecto);
        documento.setTipo(tipo);
        documento.setNombreOriginal(nombre);
        documento.setMime(mime);
        documento.setTamanoBytes(contenido.length);
        documento.setRutaRelativa(ruta);
        documento.setObservaciones(blankToNull(observaciones));
        return toDto(documentoObraRepository.save(documento));
    }

    @Override
    @Transactional(readOnly = true)
    public ArchivoDescargaDTO descargarDocumento(Long proyectoId, Long documentoId) {
        DocumentoObra documento = buscarDocumento(proyectoId, documentoId);
        byte[] contenido = documentoObraStorage.leer(documento.getRutaRelativa());
        return new ArchivoDescargaDTO(documento.getNombreOriginal(), documento.getMime(), contenido);
    }

    @Override
    @Transactional
    public void eliminarDocumento(Long proyectoId, Long documentoId) {
        DocumentoObra documento = buscarDocumento(proyectoId, documentoId);
        String ruta = documento.getRutaRelativa();
        documentoObraRepository.delete(documento);
        documentoObraStorage.borrar(ruta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaObraDTO> listarAlertas(Long proyectoId) {
        Proyecto proyecto = buscarProyecto(proyectoId);
        List<AlertaObraDTO> alertas = new ArrayList<>();
        agregarUit(alertas, proyecto);
        agregarStock(alertas, proyectoId);
        agregarFacturas(alertas, proyectoId);
        agregarFactoring(alertas, proyectoId);
        agregarDesvio(alertas, proyectoId);
        return alertas;
    }

    private void agregarUit(List<AlertaObraDTO> alertas, Proyecto proyecto) {
        CcParametroAnual parametros = parametroRepository.findByAnio(LocalDate.now().getYear())
                .or(() -> parametroRepository.findFirstByOrderByAnioDesc())
                .orElse(null);
        if (parametros == null || !UitObraAlerta.supera(proyecto.getValorObra(), parametros.getUit())) {
            return;
        }
        BigDecimal umbral = UitObraAlerta.umbral(parametros.getUit());
        alertas.add(alerta(
                "UIT",
                SeveridadAlertaObra.INFO,
                "El valor de esta obra supera 50 UIT (" + umbral.toPlainString() + "). Es informativa: no bloquea el tareo ni la liquidación."));
    }

    private void agregarStock(List<AlertaObraDTO> alertas, Long proyectoId) {
        if (almacenObraRepository.findByProyectoId(proyectoId).isEmpty()) {
            return;
        }
        AlmacenDTO almacen = almacenService.obtenerAlmacen(proyectoId);
        if (almacen.getCriticosSinStock() > 0) {
            alertas.add(alerta(
                    "STOCK",
                    SeveridadAlertaObra.CRITICAL,
                    almacen.getCriticosSinStock() + " material(es) crítico(s) sin stock."));
            return;
        }
        if (almacen.getQuiebres() > 0) {
            alertas.add(alerta(
                    "STOCK",
                    SeveridadAlertaObra.WARNING,
                    almacen.getQuiebres() + " material(es) bajo el stock mínimo."));
        }
    }

    private void agregarFacturas(List<AlertaObraDTO> alertas, Long proyectoId) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(DIAS_POR_VENCER);
        for (FacturaObra factura : facturaObraRepository.findByProyectoIdOrderByNumeroDesc(proyectoId)) {
            if (!CARTERA.contains(factura.getEstado()) || factura.getFechaVencimiento() == null) {
                continue;
            }
            if (factura.getFechaVencimiento().isBefore(hoy)) {
                alertas.add(alerta(
                        "FACTURA_VENCIDA",
                        SeveridadAlertaObra.CRITICAL,
                        "La factura N.º " + factura.getNumero() + " venció el " + factura.getFechaVencimiento() + "."));
            } else if (!factura.getFechaVencimiento().isAfter(limite)) {
                alertas.add(alerta(
                        "FACTURA_POR_VENCER",
                        SeveridadAlertaObra.WARNING,
                        "La factura N.º " + factura.getNumero() + " vence el " + factura.getFechaVencimiento() + "."));
            }
        }
    }

    private void agregarFactoring(List<AlertaObraDTO> alertas, Long proyectoId) {
        for (OperacionFactoring operacion : operacionFactoringRepository
                .findByProyectoIdOrderByFechaCesionDescIdDesc(proyectoId)) {
            if (operacion.getEstado() != EstadoFactoring.IMPAGA) {
                continue;
            }
            int numero = operacion.getFactura() == null ? 0 : operacion.getFactura().getNumero();
            alertas.add(alerta(
                    "FACTORING_IMPAGA",
                    SeveridadAlertaObra.CRITICAL,
                    "La cesión de la factura N.º " + numero + " está marcada como impaga."));
        }
    }

    private void agregarDesvio(List<AlertaObraDTO> alertas, Long proyectoId) {
        if (!presupuestoVersionRepository.existsByProyectoIdAndEstado(proyectoId, EstadoVersionPresupuesto.VIGENTE)) {
            return;
        }
        ComparativoPresupuestoDTO comparativo = construccionService.comparativoPresupuesto(proyectoId);
        if (excedeDesvio(comparativo.getDesvioPct())) {
            alertas.add(alerta(
                    "DESVIO_MO",
                    SeveridadAlertaObra.WARNING,
                    "El desvío de mano de obra es " + comparativo.getDesvioPct().toPlainString() + " % respecto al presupuesto vigente."));
        }
        if (excedeDesvio(comparativo.getDesvioPctMaterial())) {
            alertas.add(alerta(
                    "DESVIO_MATERIAL",
                    SeveridadAlertaObra.WARNING,
                    "El desvío de materiales es " + comparativo.getDesvioPctMaterial().toPlainString() + " % respecto al presupuesto vigente."));
        }
    }

    private Proyecto buscarProyecto(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new ProyectoNoEncontradoException("No se encontró el proyecto"));
    }

    private DocumentoObra buscarDocumento(Long proyectoId, Long documentoId) {
        return documentoObraRepository.findByIdAndProyectoId(documentoId, proyectoId)
                .orElseThrow(() -> new DocumentoObraNoEncontradoException("No se encontró el documento"));
    }

    private static boolean excedeDesvio(BigDecimal porcentaje) {
        return porcentaje != null && porcentaje.abs().compareTo(DESVIO_PCT) > 0;
    }

    private static String normalizarMime(String contentType, String nombre) {
        String mime = contentType == null ? "" : contentType.toLowerCase().trim();
        int corte = mime.indexOf(';');
        if (corte >= 0) {
            mime = mime.substring(0, corte).trim();
        }
        if (mime.equals("image/jpg")) {
            mime = "image/jpeg";
        }
        if (MIMES.contains(mime)) {
            return mime;
        }
        String lower = nombre == null ? "" : nombre.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        throw new IllegalArgumentException("Solo se aceptan archivos PDF, JPEG o PNG");
    }

    private static AlertaObraDTO alerta(String codigo, SeveridadAlertaObra severidad, String mensaje) {
        return AlertaObraDTO.builder()
                .codigo(codigo)
                .severidad(severidad)
                .mensaje(mensaje)
                .build();
    }

    private static DocumentoObraDTO toDto(DocumentoObra documento) {
        return DocumentoObraDTO.builder()
                .id(documento.getId())
                .proyectoId(documento.getProyecto() == null ? null : documento.getProyecto().getId())
                .tipo(documento.getTipo())
                .nombreOriginal(documento.getNombreOriginal())
                .mime(documento.getMime())
                .tamanoBytes(documento.getTamanoBytes())
                .observaciones(documento.getObservaciones())
                .createdAt(documento.getCreatedAt())
                .build();
    }

    private static String blankToNull(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
