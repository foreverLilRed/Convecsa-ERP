package com.convecsa.erp.nomina.declaracion.controller;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.nomina.declaracion.dto.ArchivoDescargaDTO;
import com.convecsa.erp.nomina.declaracion.dto.DeclaracionDetalleDTO;
import com.convecsa.erp.nomina.declaracion.dto.DeclaracionMensualDTO;
import com.convecsa.erp.nomina.declaracion.dto.DeclaracionMensualRequest;
import com.convecsa.erp.nomina.declaracion.dto.ReciboHonorarioAsignarRequest;
import com.convecsa.erp.nomina.declaracion.dto.ReciboHonorarioDTO;
import com.convecsa.erp.nomina.declaracion.dto.RxhImportacionDTO;
import com.convecsa.erp.nomina.declaracion.service.DeclaracionMensualService;
import com.convecsa.erp.nomina.dto.AsientoContableDTO;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/gestion-humana/nominas/declaraciones")
@RequiredArgsConstructor
public class DeclaracionMensualController {

    private final DeclaracionMensualService declaracionService;

    @GetMapping
    public PagedResponse<DeclaracionMensualDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return declaracionService.listar(page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeclaracionMensualDTO crear(@Valid @RequestBody DeclaracionMensualRequest request) {
        return declaracionService.crear(request);
    }

    @GetMapping("/{id:\\d+}")
    public DeclaracionDetalleDTO obtener(@PathVariable Long id) {
        return declaracionService.obtener(id);
    }

    @PostMapping("/{id:\\d+}/consolidar")
    public DeclaracionDetalleDTO consolidar(@PathVariable Long id) {
        return declaracionService.consolidarRcc(id);
    }

    @PostMapping("/{id:\\d+}/cerrar")
    public DeclaracionMensualDTO cerrar(@PathVariable Long id) {
        return declaracionService.cerrar(id);
    }

    @PostMapping(value = "/{id:\\d+}/rxh", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RxhImportacionDTO importarRxh(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Adjunte el archivo .txt de Recibos por Honorarios");
        }
        String contenido = new String(file.getBytes(), StandardCharsets.UTF_8);
        return declaracionService.importarRxh(id, contenido);
    }

    @PutMapping("/{id:\\d+}/rxh/{reciboId:\\d+}")
    public ReciboHonorarioDTO asignarRxh(
            @PathVariable Long id,
            @PathVariable Long reciboId,
            @RequestBody ReciboHonorarioAsignarRequest request) {
        return declaracionService.asignarRxh(id, reciboId, request);
    }

    @GetMapping("/{id:\\d+}/plame")
    public ResponseEntity<byte[]> plame(@PathVariable Long id) {
        return archivo(declaracionService.descargarPlame(id));
    }

    @GetMapping("/{id:\\d+}/afpnet")
    public ResponseEntity<byte[]> afpnet(@PathVariable Long id) {
        return archivo(declaracionService.descargarAfpnet(id));
    }

    @GetMapping("/{id:\\d+}/asiento-contable")
    public AsientoContableDTO asiento(@PathVariable Long id) {
        return declaracionService.asiento(id);
    }

    @GetMapping("/{id:\\d+}/asiento-contable/pdf")
    public ResponseEntity<byte[]> asientoPdf(@PathVariable Long id) {
        return archivo(declaracionService.descargarAsientoPdf(id));
    }

    @GetMapping("/{id:\\d+}/dispersion")
    public ResponseEntity<byte[]> dispersion(@PathVariable Long id) {
        return archivo(declaracionService.descargarDispersion(id));
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
