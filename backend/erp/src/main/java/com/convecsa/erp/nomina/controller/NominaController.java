package com.convecsa.erp.nomina.controller;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.nomina.dto.AfpDTO;
import com.convecsa.erp.nomina.dto.BoletaDocumentoHtml;
import com.convecsa.erp.nomina.dto.BoletaPagoDTO;
import com.convecsa.erp.nomina.dto.BoletaPagoResumenDTO;
import com.convecsa.erp.nomina.dto.BoletaUpdateRequest;
import com.convecsa.erp.nomina.dto.PeriodoNominaDTO;
import com.convecsa.erp.nomina.dto.PeriodoNominaRequest;
import com.convecsa.erp.nomina.service.InterfazNominaService;
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
@RequestMapping("/api/gestion-humana/nominas")
@RequiredArgsConstructor
public class NominaController {

    private final InterfazNominaService nominaService;

    @GetMapping("/afp")
    public List<AfpDTO> afp() {
        return nominaService.listarAfp();
    }

    @GetMapping("/periodos")
    public PagedResponse<PeriodoNominaDTO> listarPeriodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return nominaService.listarPeriodos(page, size);
    }

    @PostMapping("/periodos")
    @ResponseStatus(HttpStatus.CREATED)
    public PeriodoNominaDTO crearPeriodo(@Valid @RequestBody PeriodoNominaRequest request) {
        return nominaService.crearPeriodo(request);
    }

    @GetMapping("/periodos/{id}")
    public PeriodoNominaDTO obtenerPeriodo(@PathVariable Long id) {
        return nominaService.obtenerPeriodo(id);
    }

    @PostMapping("/periodos/{id}/generar")
    public PeriodoNominaDTO generarBoletas(@PathVariable Long id) {
        return nominaService.generarBoletas(id);
    }

    @PostMapping("/periodos/{id}/recalcular")
    public PeriodoNominaDTO recalcularPeriodo(@PathVariable Long id) {
        return nominaService.recalcularPeriodo(id);
    }

    @PostMapping("/periodos/{id}/cerrar")
    public PeriodoNominaDTO cerrarPeriodo(@PathVariable Long id) {
        return nominaService.cerrarPeriodo(id);
    }

    @GetMapping("/periodos/{id}/boletas")
    public PagedResponse<BoletaPagoResumenDTO> listarBoletas(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return nominaService.listarBoletas(id, page, size);
    }

    @GetMapping("/boletas/{id}")
    public BoletaPagoDTO obtenerBoleta(@PathVariable Long id) {
        return nominaService.obtenerBoleta(id);
    }

    @PutMapping("/boletas/{id}")
    public BoletaPagoDTO actualizarBoleta(
            @PathVariable Long id,
            @Valid @RequestBody BoletaUpdateRequest request) {
        return nominaService.actualizarBoleta(id, request);
    }

    @PostMapping("/boletas/{id}/recalcular")
    public BoletaPagoDTO recalcularBoleta(@PathVariable Long id) {
        return nominaService.recalcularBoleta(id);
    }

    @GetMapping(value = "/boletas/{id}/documento", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> documentoBoleta(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean descarga,
            @RequestParam(defaultValue = "false") boolean imprimir) {
        BoletaDocumentoHtml documento = nominaService.documentoBoleta(id, descarga, imprimir);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8));
        ContentDisposition disposition = descarga
                ? ContentDisposition.attachment().filename(documento.getFilename(), StandardCharsets.UTF_8).build()
                : ContentDisposition.inline().filename(documento.getFilename(), StandardCharsets.UTF_8).build();
        headers.setContentDisposition(disposition);
        return new ResponseEntity<>(documento.getHtml(), headers, HttpStatus.OK);
    }
}
