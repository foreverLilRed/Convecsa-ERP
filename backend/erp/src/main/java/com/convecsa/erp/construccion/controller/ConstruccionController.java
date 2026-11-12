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
import com.convecsa.erp.construccion.dto.AdicionalObraDTO;
import com.convecsa.erp.construccion.dto.AdicionalObraRequest;
import com.convecsa.erp.construccion.dto.AvanceGuardarRequest;
import com.convecsa.erp.construccion.dto.AvanceObraDTO;
import com.convecsa.erp.construccion.dto.CronogramaGuardarRequest;
import com.convecsa.erp.construccion.dto.CronogramaPartidaDTO;
import com.convecsa.erp.construccion.dto.CurvaSDTO;
import com.convecsa.erp.construccion.dto.ValorizacionCrearRequest;
import com.convecsa.erp.construccion.dto.ValorizacionDTO;
import com.convecsa.erp.construccion.dto.CobranzaObraDTO;
import com.convecsa.erp.construccion.dto.CobranzaObraRequest;
import com.convecsa.erp.construccion.dto.FacturaObraDTO;
import com.convecsa.erp.construccion.dto.FacturaVencimientoRequest;
import com.convecsa.erp.construccion.dto.FlujoCajaDTO;
import com.convecsa.erp.construccion.dto.LiberacionRetencionRequest;
import com.convecsa.erp.construccion.dto.OperacionFactoringDTO;
import com.convecsa.erp.construccion.dto.OperacionFactoringRequest;
import com.convecsa.erp.construccion.dto.RetencionesDTO;
import com.convecsa.erp.construccion.dto.AlertaObraDTO;
import com.convecsa.erp.construccion.dto.AuditoriaObraDTO;
import com.convecsa.erp.construccion.dto.BitacoraObraDTO;
import com.convecsa.erp.construccion.dto.BitacoraObraRequest;
import com.convecsa.erp.construccion.dto.AsientosObraDTO;
import com.convecsa.erp.construccion.dto.AsignacionEquipoDTO;
import com.convecsa.erp.construccion.dto.AsignacionEquipoRequest;
import com.convecsa.erp.construccion.dto.CuadrillaMiembroRequest;
import com.convecsa.erp.construccion.dto.CuadrillaObraDTO;
import com.convecsa.erp.construccion.dto.CuadrillaObraRequest;
import com.convecsa.erp.construccion.dto.DocumentoObraDTO;
import com.convecsa.erp.construccion.dto.ImputacionEquipoDTO;
import com.convecsa.erp.construccion.dto.ImputacionEquipoRequest;
import com.convecsa.erp.construccion.dto.RentabilidadObraDTO;
import com.convecsa.erp.construccion.dto.SubcontratoAvanceDTO;
import com.convecsa.erp.construccion.dto.SubcontratoAvanceRequest;
import com.convecsa.erp.construccion.dto.SubcontratoObraDTO;
import com.convecsa.erp.construccion.dto.SubcontratoObraRequest;
import com.convecsa.erp.construccion.domain.TipoDocumentoObra;
import com.convecsa.erp.construccion.service.InterfazAlmacenService;
import com.convecsa.erp.construccion.service.InterfazConstruccionService;
import com.convecsa.erp.construccion.service.InterfazFinanciamientoService;
import com.convecsa.erp.construccion.service.InterfazRentabilidadObraService;
import com.convecsa.erp.construccion.service.InterfazRecursosObraService;
import com.convecsa.erp.construccion.service.InterfazTransversalObraService;
import com.convecsa.erp.construccion.service.InterfazValorizacionService;
import com.convecsa.erp.nomina.declaracion.domain.BancoDispersion;
import com.convecsa.erp.nomina.declaracion.dto.ArchivoDescargaDTO;
import com.convecsa.erp.nomina.dto.BoletaEnvioResumenDTO;
import com.convecsa.erp.nomina.dto.BoletaLoteRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ConstruccionController {

    private final InterfazConstruccionService construccionService;
    private final InterfazAlmacenService almacenService;
    private final InterfazValorizacionService valorizacionService;
    private final InterfazFinanciamientoService financiamientoService;
    private final InterfazRentabilidadObraService rentabilidadService;
    private final InterfazTransversalObraService transversalService;
    private final InterfazRecursosObraService recursosService;

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

    @GetMapping("/{id:\\d+}/presupuesto/plantilla")
    public ResponseEntity<byte[]> plantillaPresupuesto(
            @PathVariable Long id,
            @RequestParam(required = false) Long versionId) {
        return archivo(construccionService.plantillaPresupuesto(id, versionId));
    }

    @PostMapping(
            value = "/{id:\\d+}/presupuesto/versiones/{versionId}/importar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PresupuestoDTO importarPresupuesto(
            @PathVariable Long id,
            @PathVariable Long versionId,
            @RequestParam("file") MultipartFile file) {
        return construccionService.importarPresupuesto(id, versionId, file);
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

    @GetMapping("/{id:\\d+}/cronograma")
    public List<CronogramaPartidaDTO> cronograma(@PathVariable Long id) {
        return valorizacionService.listarCronograma(id);
    }

    @PutMapping("/{id:\\d+}/cronograma")
    public List<CronogramaPartidaDTO> guardarCronograma(
            @PathVariable Long id,
            @Valid @RequestBody CronogramaGuardarRequest request) {
        return valorizacionService.guardarCronograma(id, request);
    }

    @GetMapping("/{id:\\d+}/avances")
    public AvanceObraDTO avance(
            @PathVariable Long id,
            @RequestParam int anio,
            @RequestParam int mes) {
        return valorizacionService.obtenerAvance(id, anio, mes);
    }

    @PutMapping("/{id:\\d+}/avances")
    public AvanceObraDTO guardarAvance(
            @PathVariable Long id,
            @Valid @RequestBody AvanceGuardarRequest request) {
        return valorizacionService.guardarAvance(id, request);
    }

    @PostMapping("/{id:\\d+}/avances/{avanceId}/cerrar")
    public AvanceObraDTO cerrarAvance(@PathVariable Long id, @PathVariable Long avanceId) {
        return valorizacionService.cerrarAvance(id, avanceId);
    }

    @GetMapping("/{id:\\d+}/adicionales")
    public List<AdicionalObraDTO> adicionales(@PathVariable Long id) {
        return valorizacionService.listarAdicionales(id);
    }

    @PostMapping("/{id:\\d+}/adicionales")
    @ResponseStatus(HttpStatus.CREATED)
    public AdicionalObraDTO crearAdicional(
            @PathVariable Long id,
            @Valid @RequestBody AdicionalObraRequest request) {
        return valorizacionService.crearAdicional(id, request);
    }

    @PutMapping("/{id:\\d+}/adicionales/{adicionalId}")
    public AdicionalObraDTO guardarAdicional(
            @PathVariable Long id,
            @PathVariable Long adicionalId,
            @Valid @RequestBody AdicionalObraRequest request) {
        return valorizacionService.guardarAdicional(id, adicionalId, request);
    }

    @PostMapping("/{id:\\d+}/adicionales/{adicionalId}/vigentar")
    public AdicionalObraDTO vigentarAdicional(@PathVariable Long id, @PathVariable Long adicionalId) {
        return valorizacionService.vigentarAdicional(id, adicionalId);
    }

    @PostMapping("/{id:\\d+}/adicionales/{adicionalId}/anular")
    public AdicionalObraDTO anularAdicional(@PathVariable Long id, @PathVariable Long adicionalId) {
        return valorizacionService.anularAdicional(id, adicionalId);
    }

    @GetMapping("/{id:\\d+}/valorizaciones")
    public List<ValorizacionDTO> valorizaciones(@PathVariable Long id) {
        return valorizacionService.listarValorizaciones(id);
    }

    @PostMapping("/{id:\\d+}/valorizaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public ValorizacionDTO crearValorizacion(
            @PathVariable Long id,
            @Valid @RequestBody ValorizacionCrearRequest request) {
        return valorizacionService.crearValorizacion(id, request);
    }

    @GetMapping("/{id:\\d+}/valorizaciones/{valId}")
    public ValorizacionDTO valorizacion(@PathVariable Long id, @PathVariable Long valId) {
        return valorizacionService.obtenerValorizacion(id, valId);
    }

    @PostMapping("/{id:\\d+}/valorizaciones/{valId}/presentar")
    public ValorizacionDTO presentar(@PathVariable Long id, @PathVariable Long valId) {
        return valorizacionService.presentarValorizacion(id, valId);
    }

    @PostMapping("/{id:\\d+}/valorizaciones/{valId}/observar")
    public ValorizacionDTO observar(@PathVariable Long id, @PathVariable Long valId) {
        return valorizacionService.observarValorizacion(id, valId);
    }

    @PostMapping("/{id:\\d+}/valorizaciones/{valId}/aprobar")
    public ValorizacionDTO aprobar(@PathVariable Long id, @PathVariable Long valId) {
        return valorizacionService.aprobarValorizacion(id, valId);
    }

    @PostMapping("/{id:\\d+}/valorizaciones/{valId}/facturar")
    public ValorizacionDTO facturar(@PathVariable Long id, @PathVariable Long valId) {
        return valorizacionService.facturarValorizacion(id, valId);
    }

    @GetMapping("/{id:\\d+}/curva-s")
    public CurvaSDTO curvaS(@PathVariable Long id) {
        return valorizacionService.curvaS(id);
    }

    @GetMapping("/{id:\\d+}/facturas")
    public List<FacturaObraDTO> facturas(@PathVariable Long id) {
        return financiamientoService.listarFacturas(id);
    }

    @GetMapping("/{id:\\d+}/facturas/{facturaId}")
    public FacturaObraDTO factura(@PathVariable Long id, @PathVariable Long facturaId) {
        return financiamientoService.obtenerFactura(id, facturaId);
    }

    @PutMapping("/{id:\\d+}/facturas/{facturaId}")
    public FacturaObraDTO actualizarFactura(
            @PathVariable Long id,
            @PathVariable Long facturaId,
            @RequestBody FacturaVencimientoRequest request) {
        return financiamientoService.actualizarVencimiento(id, facturaId, request);
    }

    @GetMapping("/{id:\\d+}/cobranzas")
    public List<CobranzaObraDTO> cobranzas(@PathVariable Long id) {
        return financiamientoService.listarCobranzas(id);
    }

    @PostMapping("/{id:\\d+}/cobranzas")
    @ResponseStatus(HttpStatus.CREATED)
    public FacturaObraDTO crearCobranza(
            @PathVariable Long id,
            @Valid @RequestBody CobranzaObraRequest request) {
        return financiamientoService.registrarCobranza(id, request);
    }

    @GetMapping("/{id:\\d+}/retenciones")
    public RetencionesDTO retenciones(@PathVariable Long id) {
        return financiamientoService.obtenerRetenciones(id);
    }

    @PostMapping("/{id:\\d+}/retenciones/liberar")
    public RetencionesDTO liberarRetencion(
            @PathVariable Long id,
            @Valid @RequestBody LiberacionRetencionRequest request) {
        return financiamientoService.liberarRetencion(id, request);
    }

    @GetMapping("/{id:\\d+}/factoring")
    public List<OperacionFactoringDTO> factoring(@PathVariable Long id) {
        return financiamientoService.listarFactoring(id);
    }

    @PostMapping("/{id:\\d+}/factoring")
    @ResponseStatus(HttpStatus.CREATED)
    public OperacionFactoringDTO ceder(
            @PathVariable Long id,
            @Valid @RequestBody OperacionFactoringRequest request) {
        return financiamientoService.cederFactura(id, request);
    }

    @PostMapping("/{id:\\d+}/factoring/{opId}/adelantar")
    public OperacionFactoringDTO adelantar(@PathVariable Long id, @PathVariable Long opId) {
        return financiamientoService.adelantarFactoring(id, opId);
    }

    @PostMapping("/{id:\\d+}/factoring/{opId}/cobrar")
    public OperacionFactoringDTO cobrarFactor(@PathVariable Long id, @PathVariable Long opId) {
        return financiamientoService.cobrarFactoring(id, opId);
    }

    @PostMapping("/{id:\\d+}/factoring/{opId}/impagar")
    public OperacionFactoringDTO impagar(@PathVariable Long id, @PathVariable Long opId) {
        return financiamientoService.impagarFactoring(id, opId);
    }

    @PostMapping("/{id:\\d+}/factoring/{opId}/revertir")
    public OperacionFactoringDTO revertir(@PathVariable Long id, @PathVariable Long opId) {
        return financiamientoService.revertirFactoring(id, opId);
    }

    @GetMapping("/{id:\\d+}/flujo-caja")
    public FlujoCajaDTO flujoCaja(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        return financiamientoService.flujoCaja(id, desde, hasta);
    }

    @GetMapping("/{id:\\d+}/rentabilidad")
    public RentabilidadObraDTO rentabilidad(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        return rentabilidadService.rentabilidad(id, desde, hasta);
    }

    @GetMapping("/{id:\\d+}/asientos")
    public AsientosObraDTO asientos(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        return rentabilidadService.asientos(id, desde, hasta);
    }

    @GetMapping("/{id:\\d+}/documentos")
    public List<DocumentoObraDTO> documentos(@PathVariable Long id) {
        return transversalService.listarDocumentos(id);
    }

    @PostMapping(value = "/{id:\\d+}/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentoObraDTO subirDocumento(
            @PathVariable Long id,
            @RequestParam TipoDocumentoObra tipo,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String observaciones) {
        return transversalService.subirDocumento(id, tipo, file, observaciones);
    }

    @GetMapping("/{id:\\d+}/documentos/{docId}")
    public ResponseEntity<byte[]> descargarDocumento(@PathVariable Long id, @PathVariable Long docId) {
        return archivo(transversalService.descargarDocumento(id, docId));
    }

    @DeleteMapping("/{id:\\d+}/documentos/{docId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarDocumento(@PathVariable Long id, @PathVariable Long docId) {
        transversalService.eliminarDocumento(id, docId);
    }

    @GetMapping("/{id:\\d+}/alertas")
    public List<AlertaObraDTO> alertas(@PathVariable Long id) {
        return transversalService.listarAlertas(id);
    }

    @GetMapping("/{id:\\d+}/bitacora")
    public List<BitacoraObraDTO> bitacora(@PathVariable Long id) {
        return transversalService.listarBitacora(id);
    }

    @PostMapping("/{id:\\d+}/bitacora")
    @ResponseStatus(HttpStatus.CREATED)
    public BitacoraObraDTO crearNotaBitacora(
            @PathVariable Long id, @Valid @RequestBody BitacoraObraRequest request) {
        return transversalService.crearNotaBitacora(id, request);
    }

    @DeleteMapping("/{id:\\d+}/bitacora/{notaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarNotaBitacora(@PathVariable Long id, @PathVariable Long notaId) {
        transversalService.eliminarNotaBitacora(id, notaId);
    }

    @GetMapping("/{id:\\d+}/auditoria")
    public List<AuditoriaObraDTO> auditoria(@PathVariable Long id) {
        return transversalService.listarAuditoria(id);
    }

    @GetMapping("/{id:\\d+}/equipos")
    public List<AsignacionEquipoDTO> equipos(@PathVariable Long id) {
        return recursosService.listarEquipos(id);
    }

    @PostMapping("/{id:\\d+}/equipos")
    @ResponseStatus(HttpStatus.CREATED)
    public AsignacionEquipoDTO crearEquipo(
            @PathVariable Long id, @Valid @RequestBody AsignacionEquipoRequest request) {
        return recursosService.crearEquipo(id, request);
    }

    @PutMapping("/{id:\\d+}/equipos/{eqId}")
    public AsignacionEquipoDTO actualizarEquipo(
            @PathVariable Long id,
            @PathVariable Long eqId,
            @Valid @RequestBody AsignacionEquipoRequest request) {
        return recursosService.actualizarEquipo(id, eqId, request);
    }

    @GetMapping("/{id:\\d+}/equipos/{eqId}/imputaciones")
    public List<ImputacionEquipoDTO> imputaciones(@PathVariable Long id, @PathVariable Long eqId) {
        return recursosService.listarImputaciones(id, eqId);
    }

    @PostMapping("/{id:\\d+}/equipos/{eqId}/imputaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public ImputacionEquipoDTO registrarImputacion(
            @PathVariable Long id,
            @PathVariable Long eqId,
            @Valid @RequestBody ImputacionEquipoRequest request) {
        return recursosService.registrarImputacion(id, eqId, request);
    }

    @DeleteMapping("/{id:\\d+}/equipos/{eqId}/imputaciones/{impId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarImputacion(
            @PathVariable Long id, @PathVariable Long eqId, @PathVariable Long impId) {
        recursosService.eliminarImputacion(id, eqId, impId);
    }

    @GetMapping("/{id:\\d+}/subcontratos")
    public List<SubcontratoObraDTO> subcontratos(@PathVariable Long id) {
        return recursosService.listarSubcontratos(id);
    }

    @PostMapping("/{id:\\d+}/subcontratos")
    @ResponseStatus(HttpStatus.CREATED)
    public SubcontratoObraDTO crearSubcontrato(
            @PathVariable Long id, @Valid @RequestBody SubcontratoObraRequest request) {
        return recursosService.crearSubcontrato(id, request);
    }

    @PutMapping("/{id:\\d+}/subcontratos/{subId}")
    public SubcontratoObraDTO actualizarSubcontrato(
            @PathVariable Long id,
            @PathVariable Long subId,
            @Valid @RequestBody SubcontratoObraRequest request) {
        return recursosService.actualizarSubcontrato(id, subId, request);
    }

    @GetMapping("/{id:\\d+}/subcontratos/{subId}/avances")
    public List<SubcontratoAvanceDTO> avancesSubcontrato(@PathVariable Long id, @PathVariable Long subId) {
        return recursosService.listarAvances(id, subId);
    }

    @PostMapping("/{id:\\d+}/subcontratos/{subId}/avances")
    @ResponseStatus(HttpStatus.CREATED)
    public SubcontratoAvanceDTO registrarAvanceSubcontrato(
            @PathVariable Long id,
            @PathVariable Long subId,
            @Valid @RequestBody SubcontratoAvanceRequest request) {
        return recursosService.registrarAvance(id, subId, request);
    }

    @DeleteMapping("/{id:\\d+}/subcontratos/{subId}/avances/{avId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarAvanceSubcontrato(
            @PathVariable Long id, @PathVariable Long subId, @PathVariable Long avId) {
        recursosService.eliminarAvance(id, subId, avId);
    }

    @GetMapping("/{id:\\d+}/cuadrillas")
    public List<CuadrillaObraDTO> cuadrillas(@PathVariable Long id) {
        return recursosService.listarCuadrillas(id);
    }

    @PostMapping("/{id:\\d+}/cuadrillas")
    @ResponseStatus(HttpStatus.CREATED)
    public CuadrillaObraDTO crearCuadrilla(
            @PathVariable Long id, @Valid @RequestBody CuadrillaObraRequest request) {
        return recursosService.crearCuadrilla(id, request);
    }

    @PutMapping("/{id:\\d+}/cuadrillas/{cuadrillaId}")
    public CuadrillaObraDTO actualizarCuadrilla(
            @PathVariable Long id,
            @PathVariable Long cuadrillaId,
            @Valid @RequestBody CuadrillaObraRequest request) {
        return recursosService.actualizarCuadrilla(id, cuadrillaId, request);
    }

    @DeleteMapping("/{id:\\d+}/cuadrillas/{cuadrillaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCuadrilla(@PathVariable Long id, @PathVariable Long cuadrillaId) {
        recursosService.eliminarCuadrilla(id, cuadrillaId);
    }

    @PostMapping("/{id:\\d+}/cuadrillas/{cuadrillaId}/miembros")
    @ResponseStatus(HttpStatus.CREATED)
    public CuadrillaObraDTO agregarMiembro(
            @PathVariable Long id,
            @PathVariable Long cuadrillaId,
            @Valid @RequestBody CuadrillaMiembroRequest request) {
        return recursosService.agregarMiembro(id, cuadrillaId, request);
    }

    @DeleteMapping("/{id:\\d+}/cuadrillas/{cuadrillaId}/miembros/{personaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitarMiembro(
            @PathVariable Long id, @PathVariable Long cuadrillaId, @PathVariable Long personaId) {
        recursosService.quitarMiembro(id, cuadrillaId, personaId);
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
