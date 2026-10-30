package com.convecsa.erp.construccion.controller;

import com.convecsa.erp.construccion.dto.BoletaCcDTO;
import com.convecsa.erp.construccion.dto.BoletaCcExtrasRequest;
import com.convecsa.erp.construccion.dto.BoletaCcResumenDTO;
import com.convecsa.erp.construccion.dto.ConstruccionCatalogosDTO;
import com.convecsa.erp.construccion.dto.PeriodoCcDTO;
import com.convecsa.erp.construccion.dto.PeriodoCcRequest;
import com.convecsa.erp.construccion.dto.PersonalObraDTO;
import com.convecsa.erp.construccion.dto.ProyectoDTO;
import com.convecsa.erp.construccion.dto.ProyectoRequest;
import com.convecsa.erp.construccion.dto.TareoCcGrillaDTO;
import com.convecsa.erp.construccion.dto.TareoCcGuardarRequest;
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
