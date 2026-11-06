package com.convecsa.erp.construccion.controller;

import com.convecsa.erp.construccion.dto.BoletaCcDTO;
import com.convecsa.erp.construccion.dto.BoletaCcExtrasRequest;
import com.convecsa.erp.construccion.dto.BoletaCcResumenDTO;
import com.convecsa.erp.construccion.dto.ComparativoPresupuestoDTO;
import com.convecsa.erp.construccion.dto.ConstruccionCatalogosDTO;
import com.convecsa.erp.construccion.dto.PartidaPresupuestoRequest;
import com.convecsa.erp.construccion.dto.PartidasPresupuestoGuardarRequest;
import com.convecsa.erp.construccion.dto.PeriodoCcDTO;
import com.convecsa.erp.construccion.dto.PeriodoCcRequest;
import com.convecsa.erp.construccion.dto.PersonalObraDTO;
import com.convecsa.erp.construccion.dto.PresupuestoDTO;
import com.convecsa.erp.construccion.dto.PresupuestoVersionRequest;
import com.convecsa.erp.construccion.dto.ProyectoDTO;
import com.convecsa.erp.construccion.dto.ProyectoRequest;
import com.convecsa.erp.construccion.dto.ProyectoResponsableRequest;
import com.convecsa.erp.construccion.dto.ProyectoResponsablesGuardarRequest;
import com.convecsa.erp.construccion.dto.ResponsableObraDTO;
import com.convecsa.erp.construccion.dto.AjusteAlmacenRequest;
import com.convecsa.erp.construccion.dto.AlmacenDTO;
import com.convecsa.erp.construccion.dto.CierreInventarioRequest;
import com.convecsa.erp.construccion.dto.DevolucionRequest;
import com.convecsa.erp.construccion.dto.InventarioCierreDTO;
import com.convecsa.erp.construccion.dto.MaterialDTO;
import com.convecsa.erp.construccion.dto.MaterialRequest;
import com.convecsa.erp.construccion.dto.MovimientoAlmacenDTO;
import com.convecsa.erp.construccion.dto.OrdenCompraDTO;
import com.convecsa.erp.construccion.dto.OrdenCompraRequest;
import com.convecsa.erp.construccion.dto.RecepcionRequest;
import com.convecsa.erp.construccion.dto.RequerimientoDTO;
import com.convecsa.erp.construccion.dto.RequerimientoRequest;
import com.convecsa.erp.construccion.dto.SugeridoApuDTO;
import com.convecsa.erp.construccion.dto.TareoCcGrillaDTO;
import com.convecsa.erp.construccion.dto.TareoCcGuardarRequest;
import com.convecsa.erp.construccion.dto.ValeSalidaDTO;
import com.convecsa.erp.construccion.dto.ValeSalidaRequest;
import com.convecsa.erp.construccion.service.InterfazAlmacenService;
import com.convecsa.erp.construccion.service.InterfazConstruccionService;
import com.convecsa.erp.nomina.declaracion.domain.BancoDispersion;
import com.convecsa.erp.nomina.declaracion.dto.ArchivoDescargaDTO;
import com.convecsa.erp.nomina.dto.BoletaEnvioResumenDTO;
import com.convecsa.erp.nomina.dto.BoletaLoteRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ConstruccionController {

    private final InterfazConstruccionService construccionService;
    private final InterfazAlmacenService almacenService;

    @GetMapping("/catalogos")
    public ConstruccionCatalogosDTO catalogos() {
        return construccionService.catalogos();
    }

    @GetMapping
    public List<ProyectoDTO> listar(@RequestParam(required = false) Boolean activo) {
        return construccionService.listarProyectos(activo);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProyectoDTO crear(@Valid @RequestBody ProyectoRequest request) {
        return construccionService.crearProyecto(request);
    }

    @GetMapping("/{id:\\d+}")
    public ProyectoDTO obtener(@PathVariable Long id) {
        return construccionService.obtenerProyecto(id);
    }

    @PutMapping("/{id:\\d+}")
    public ProyectoDTO actualizar(@PathVariable Long id, @Valid @RequestBody ProyectoRequest request) {
        return construccionService.actualizarProyecto(id, request);
    }

    @GetMapping("/{id:\\d+}/responsables")
    public List<ResponsableObraDTO> responsables(@PathVariable Long id) {
        return construccionService.listarResponsables(id);
    }

    @PutMapping("/{id:\\d+}/responsables")
    public List<ResponsableObraDTO> guardarResponsables(
            @PathVariable Long id,
            @Valid @RequestBody ProyectoResponsablesGuardarRequest request) {
        List<ProyectoResponsableRequest> filas =
                request.getResponsables() == null ? List.of() : request.getResponsables();
        return construccionService.guardarResponsables(id, filas);
    }

    @GetMapping("/{id:\\d+}/presupuesto")
    public PresupuestoDTO presupuesto(
            @PathVariable Long id,
            @RequestParam(required = false) Long versionId) {
        return construccionService.obtenerPresupuesto(id, versionId);
    }

    @PostMapping("/{id:\\d+}/presupuesto/versiones")
    @ResponseStatus(HttpStatus.CREATED)
    public PresupuestoDTO crearVersion(
            @PathVariable Long id,
            @RequestBody(required = false) PresupuestoVersionRequest request) {
        return construccionService.crearVersionPresupuesto(id, request == null ? new PresupuestoVersionRequest() : request);
    }

    @PutMapping("/{id:\\d+}/presupuesto/versiones/{versionId}/partidas")
    public PresupuestoDTO guardarPartidas(
            @PathVariable Long id,
            @PathVariable Long versionId,
            @Valid @RequestBody PartidasPresupuestoGuardarRequest request) {
        List<PartidaPresupuestoRequest> filas =
                request.getPartidas() == null ? List.of() : request.getPartidas();
        return construccionService.guardarPartidasPresupuesto(id, versionId, filas);
    }

    @PostMapping("/{id:\\d+}/presupuesto/versiones/{versionId}/vigentar")
    public PresupuestoDTO vigentar(@PathVariable Long id, @PathVariable Long versionId) {
        return construccionService.vigentarPresupuesto(id, versionId);
    }

    @GetMapping("/{id:\\d+}/presupuesto/comparativo")
    public ComparativoPresupuestoDTO comparativo(@PathVariable Long id) {
        return construccionService.comparativoPresupuesto(id);
    }

    @GetMapping("/materiales")
    public List<MaterialDTO> materiales() {
        return almacenService.listarMateriales();
    }

    @PostMapping("/materiales")
    @ResponseStatus(HttpStatus.CREATED)
    public MaterialDTO crearMaterial(@Valid @RequestBody MaterialRequest request) {
        return almacenService.crearMaterial(request);
    }

    @PutMapping("/materiales/{materialId}")
    public MaterialDTO actualizarMaterial(
            @PathVariable Long materialId,
            @Valid @RequestBody MaterialRequest request) {
        return almacenService.actualizarMaterial(materialId, request);
    }

    @GetMapping("/{id:\\d+}/almacen")
    public AlmacenDTO almacen(@PathVariable Long id) {
        return almacenService.obtenerAlmacen(id);
    }

    @GetMapping("/{id:\\d+}/almacen/kardex")
    public List<MovimientoAlmacenDTO> kardex(
            @PathVariable Long id,
            @RequestParam(required = false) Long materialId) {
        return almacenService.listarKardex(id, materialId);
    }

    @PostMapping("/{id:\\d+}/almacen/ajustes")
    public AlmacenDTO ajuste(@PathVariable Long id, @Valid @RequestBody AjusteAlmacenRequest request) {
        return almacenService.registrarAjuste(id, request);
    }

    @PostMapping("/{id:\\d+}/almacen/cierre")
    public InventarioCierreDTO cierre(@PathVariable Long id, @Valid @RequestBody CierreInventarioRequest request) {
        return almacenService.cerrarInventario(id, request);
    }

    @GetMapping("/{id:\\d+}/almacen/cierres")
    public List<InventarioCierreDTO> cierres(@PathVariable Long id) {
        return almacenService.listarCierres(id);
    }

    @GetMapping("/{id:\\d+}/almacen/vales")
    public List<ValeSalidaDTO> vales(@PathVariable Long id) {
        return almacenService.listarVales(id);
    }

    @PostMapping("/{id:\\d+}/almacen/vales")
    @ResponseStatus(HttpStatus.CREATED)
    public ValeSalidaDTO crearVale(@PathVariable Long id, @Valid @RequestBody ValeSalidaRequest request) {
        return almacenService.crearVale(id, request);
    }

    @PostMapping("/{id:\\d+}/almacen/vales/{valeId}/devolver")
    public ValeSalidaDTO devolverVale(
            @PathVariable Long id,
            @PathVariable Long valeId,
            @RequestBody(required = false) DevolucionRequest request) {
        return almacenService.devolverVale(id, valeId, request == null ? new DevolucionRequest() : request);
    }

    @GetMapping("/{id:\\d+}/requerimientos/sugeridos-apu")
    public List<SugeridoApuDTO> sugeridosApu(@PathVariable Long id) {
        return almacenService.sugeridosApu(id);
    }

    @GetMapping("/{id:\\d+}/requerimientos")
    public List<RequerimientoDTO> requerimientos(@PathVariable Long id) {
        return almacenService.listarRequerimientos(id);
    }

    @PostMapping("/{id:\\d+}/requerimientos")
    @ResponseStatus(HttpStatus.CREATED)
    public RequerimientoDTO crearRequerimiento(
            @PathVariable Long id,
            @Valid @RequestBody RequerimientoRequest request) {
        return almacenService.crearRequerimiento(id, request);
    }

    @GetMapping("/{id:\\d+}/requerimientos/{reqId}")
    public RequerimientoDTO requerimiento(@PathVariable Long id, @PathVariable Long reqId) {
        return almacenService.obtenerRequerimiento(id, reqId);
    }

    @PutMapping("/{id:\\d+}/requerimientos/{reqId}")
    public RequerimientoDTO guardarRequerimiento(
            @PathVariable Long id,
            @PathVariable Long reqId,
            @Valid @RequestBody RequerimientoRequest request) {
        return almacenService.guardarRequerimiento(id, reqId, request);
    }

    @PostMapping("/{id:\\d+}/requerimientos/{reqId}/aprobar")
    public RequerimientoDTO aprobarRequerimiento(@PathVariable Long id, @PathVariable Long reqId) {
        return almacenService.aprobarRequerimiento(id, reqId);
    }

    @PostMapping("/{id:\\d+}/requerimientos/{reqId}/anular")
    public RequerimientoDTO anularRequerimiento(@PathVariable Long id, @PathVariable Long reqId) {
        return almacenService.anularRequerimiento(id, reqId);
    }

    @GetMapping("/{id:\\d+}/compras")
    public List<OrdenCompraDTO> compras(@PathVariable Long id) {
        return almacenService.listarOrdenesCompra(id);
    }

    @PostMapping("/{id:\\d+}/compras")
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenCompraDTO crearCompra(@PathVariable Long id, @Valid @RequestBody OrdenCompraRequest request) {
        return almacenService.crearOrdenCompra(id, request);
    }

    @GetMapping("/{id:\\d+}/compras/{ocId}")
    public OrdenCompraDTO compra(@PathVariable Long id, @PathVariable Long ocId) {
        return almacenService.obtenerOrdenCompra(id, ocId);
    }

    @PutMapping("/{id:\\d+}/compras/{ocId}")
    public OrdenCompraDTO guardarCompra(
            @PathVariable Long id,
            @PathVariable Long ocId,
            @Valid @RequestBody OrdenCompraRequest request) {
        return almacenService.guardarOrdenCompra(id, ocId, request);
    }

    @PostMapping("/{id:\\d+}/compras/{ocId}/emitir")
    public OrdenCompraDTO emitirCompra(@PathVariable Long id, @PathVariable Long ocId) {
        return almacenService.emitirOrdenCompra(id, ocId);
    }

    @PostMapping("/{id:\\d+}/compras/{ocId}/cerrar")
    public OrdenCompraDTO cerrarCompra(@PathVariable Long id, @PathVariable Long ocId) {
        return almacenService.cerrarOrdenCompra(id, ocId);
    }

    @PostMapping("/{id:\\d+}/compras/{ocId}/recepciones")
    public OrdenCompraDTO recepcionar(
            @PathVariable Long id,
            @PathVariable Long ocId,
            @Valid @RequestBody RecepcionRequest request) {
        return almacenService.recepcionar(id, ocId, request);
    }

    @PostMapping("/{id:\\d+}/compras/{ocId}/devolver")
    public OrdenCompraDTO devolverProveedor(
            @PathVariable Long id,
            @PathVariable Long ocId,
            @Valid @RequestBody DevolucionRequest request) {
        return almacenService.devolverProveedor(id, ocId, request);
    }

    @GetMapping("/{id:\\d+}/personal")
    public List<PersonalObraDTO> personal(@PathVariable Long id) {
        return construccionService.listarPersonal(id);
    }

    @GetMapping("/{id:\\d+}/periodos")
    public List<PeriodoCcDTO> periodos(@PathVariable Long id) {
        return construccionService.listarPeriodos(id);
    }

    @PostMapping("/{id:\\d+}/periodos")
    @ResponseStatus(HttpStatus.CREATED)
    public PeriodoCcDTO crearPeriodo(
            @PathVariable Long id,
            @Valid @RequestBody PeriodoCcRequest request) {
        return construccionService.crearPeriodo(id, request);
    }

    @GetMapping("/{id:\\d+}/periodos/{periodoId}")
    public PeriodoCcDTO obtenerPeriodo(@PathVariable Long id, @PathVariable Long periodoId) {
        return construccionService.obtenerPeriodo(id, periodoId);
    }

    @PostMapping("/{id:\\d+}/periodos/{periodoId}/cerrar")
    public PeriodoCcDTO cerrarPeriodo(@PathVariable Long id, @PathVariable Long periodoId) {
        return construccionService.cerrarPeriodo(id, periodoId);
    }

    @GetMapping("/{id:\\d+}/periodos/{periodoId}/tareo")
    public TareoCcGrillaDTO tareo(@PathVariable Long id, @PathVariable Long periodoId) {
        return construccionService.obtenerTareo(id, periodoId);
    }

    @PutMapping("/{id:\\d+}/periodos/{periodoId}/tareo")
    public TareoCcGrillaDTO guardarTareo(
            @PathVariable Long id,
            @PathVariable Long periodoId,
            @Valid @RequestBody TareoCcGuardarRequest request) {
        return construccionService.guardarTareo(id, periodoId, request);
    }

    @PostMapping("/{id:\\d+}/periodos/{periodoId}/liquidar")
    public List<BoletaCcResumenDTO> liquidar(@PathVariable Long id, @PathVariable Long periodoId) {
        return construccionService.liquidar(id, periodoId);
    }

    @GetMapping("/{id:\\d+}/periodos/{periodoId}/boletas")
    public List<BoletaCcResumenDTO> boletas(@PathVariable Long id, @PathVariable Long periodoId) {
        return construccionService.listarBoletas(id, periodoId);
    }

    @GetMapping("/{id:\\d+}/periodos/{periodoId}/boletas/{boletaId}")
    public BoletaCcDTO boleta(
            @PathVariable Long id,
            @PathVariable Long periodoId,
            @PathVariable Long boletaId) {
        return construccionService.obtenerBoleta(id, periodoId, boletaId);
    }

    @GetMapping(value = "/{id:\\d+}/periodos/{periodoId}/boletas/{boletaId}/documento", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> documentoHtml(
            @PathVariable Long id,
            @PathVariable Long periodoId,
            @PathVariable Long boletaId,
            @RequestParam(defaultValue = "false") boolean descarga) {
        String html = construccionService.documentoBoletaHtml(id, periodoId, boletaId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8));
        ContentDisposition disposition = descarga
                ? ContentDisposition.attachment().filename("boleta-rcc-" + boletaId + ".html", StandardCharsets.UTF_8).build()
                : ContentDisposition.inline().filename("boleta-rcc-" + boletaId + ".html", StandardCharsets.UTF_8).build();
        headers.setContentDisposition(disposition);
        return new ResponseEntity<>(html, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/{id:\\d+}/periodos/{periodoId}/boletas/{boletaId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> documentoPdf(
            @PathVariable Long id,
            @PathVariable Long periodoId,
            @PathVariable Long boletaId,
            @RequestParam(defaultValue = "true") boolean descarga) {
        byte[] pdf = construccionService.documentoBoletaPdf(id, periodoId, boletaId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        ContentDisposition disposition = descarga
                ? ContentDisposition.attachment().filename("boleta-rcc-" + boletaId + ".pdf", StandardCharsets.UTF_8).build()
                : ContentDisposition.inline().filename("boleta-rcc-" + boletaId + ".pdf", StandardCharsets.UTF_8).build();
        headers.setContentDisposition(disposition);
        headers.setContentLength(pdf.length);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @PutMapping("/{id:\\d+}/periodos/{periodoId}/boletas/{boletaId}/extras")
    public BoletaCcDTO guardarExtras(
            @PathVariable Long id,
            @PathVariable Long periodoId,
            @PathVariable Long boletaId,
            @Valid @RequestBody BoletaCcExtrasRequest request) {
        return construccionService.guardarExtras(id, periodoId, boletaId, request);
    }

    @PostMapping(value = "/{id:\\d+}/periodos/{periodoId}/boletas/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> documentoBoletasPdf(
            @PathVariable Long id,
            @PathVariable Long periodoId,
            @RequestParam(defaultValue = "true") boolean descarga,
            @RequestBody(required = false) BoletaLoteRequest request) {
        byte[] pdf = construccionService.documentoBoletasPdf(id, periodoId, request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        ContentDisposition disposition = descarga
                ? ContentDisposition.attachment().filename("boletas-rcc-" + periodoId + ".pdf", StandardCharsets.UTF_8).build()
                : ContentDisposition.inline().filename("boletas-rcc-" + periodoId + ".pdf", StandardCharsets.UTF_8).build();
        headers.setContentDisposition(disposition);
        headers.setContentLength(pdf.length);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @PostMapping("/{id:\\d+}/periodos/{periodoId}/boletas/correo")
    public BoletaEnvioResumenDTO enviarBoletas(
            @PathVariable Long id,
            @PathVariable Long periodoId,
            @RequestBody(required = false) BoletaLoteRequest request) {
        return construccionService.enviarBoletas(id, periodoId, request);
    }

    @GetMapping("/{id:\\d+}/periodos/{periodoId}/dispersion")
    public ResponseEntity<byte[]> dispersion(
            @PathVariable Long id,
            @PathVariable Long periodoId,
            @RequestParam(defaultValue = "CCI") BancoDispersion banco) {
        return archivo(construccionService.descargarDispersion(id, periodoId, banco));
    }

    private static ResponseEntity<byte[]> archivo(ArchivoDescargaDTO archivo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(archivo.getContentType().split(";")[0]));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(archivo.getFilename(), StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(archivo.getContenido().length);
        return new ResponseEntity<>(archivo.getContenido(), headers, HttpStatus.OK);
    }
}
