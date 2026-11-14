package com.convecsa.erp.soma.controller;

import com.convecsa.erp.soma.dto.SomaCapacitacionAsistenteDTO;
import com.convecsa.erp.soma.dto.SomaCapacitacionDTO;
import com.convecsa.erp.soma.dto.SomaCapacitacionRequest;
import com.convecsa.erp.soma.dto.SomaHabilitacionDTO;
import com.convecsa.erp.soma.dto.SomaEmoDTO;
import com.convecsa.erp.soma.dto.SomaEmoRequest;
import com.convecsa.erp.soma.dto.SomaEntregaEppDTO;
import com.convecsa.erp.soma.dto.SomaEntregaEppRequest;
import com.convecsa.erp.soma.dto.SomaEppItemEstadoRequest;
import com.convecsa.erp.soma.dto.SomaEppMaterialDTO;
import com.convecsa.erp.soma.dto.SomaResumenObraDTO;
import com.convecsa.erp.soma.dto.SomaSctrNominaGuardarRequest;
import com.convecsa.erp.soma.dto.SomaSctrPolizaDTO;
import com.convecsa.erp.soma.dto.SomaSctrPolizaRequest;
import com.convecsa.erp.soma.dto.SomaTrabajadorObraDTO;
import com.convecsa.erp.soma.service.InterfazSomaCapacitacionService;
import com.convecsa.erp.soma.service.InterfazSomaEmoService;
import com.convecsa.erp.soma.service.InterfazSomaEppService;
import com.convecsa.erp.soma.service.InterfazSomaHabilitacionService;
import com.convecsa.erp.soma.service.InterfazSomaObraService;
import com.convecsa.erp.soma.service.InterfazSomaSctrService;
import java.time.LocalDate;
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
@RequestMapping("/api/proyectos/{proyectoId:\\d+}/soma")
@RequiredArgsConstructor
public class SomaObraController {

    private final InterfazSomaObraService somaObraService;
    private final InterfazSomaEmoService somaEmoService;
    private final InterfazSomaSctrService somaSctrService;
    private final InterfazSomaEppService somaEppService;
    private final InterfazSomaCapacitacionService somaCapacitacionService;
    private final InterfazSomaHabilitacionService somaHabilitacionService;

    @GetMapping("/resumen")
    public SomaResumenObraDTO resumen(@PathVariable Long proyectoId) {
        return somaObraService.obtenerResumen(proyectoId);
    }

    @GetMapping("/habilitacion")
    public SomaHabilitacionDTO habilitacion(
            @PathVariable Long proyectoId, @RequestParam(required = false) LocalDate fecha) {
        return somaHabilitacionService.evaluarObra(proyectoId, fecha);
    }

    @GetMapping("/emos")
    public List<SomaEmoDTO> listarEmos(@PathVariable Long proyectoId) {
        return somaEmoService.listarPorObra(proyectoId);
    }

    @PostMapping("/emos")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaEmoDTO registrarEmo(@PathVariable Long proyectoId, @RequestBody SomaEmoRequest request) {
        return somaEmoService.registrar(proyectoId, request);
    }

    @GetMapping("/sctr")
    public List<SomaSctrPolizaDTO> listarSctr(@PathVariable Long proyectoId) {
        return somaSctrService.listar(proyectoId);
    }

    @PostMapping("/sctr")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaSctrPolizaDTO crearSctr(@PathVariable Long proyectoId, @RequestBody SomaSctrPolizaRequest request) {
        return somaSctrService.crear(proyectoId, request);
    }

    @GetMapping("/sctr/sugerir-nomina")
    public List<SomaTrabajadorObraDTO> sugerirNominaSctr(
            @PathVariable Long proyectoId, @RequestParam Integer anio, @RequestParam Integer mes) {
        return somaSctrService.sugerirNomina(proyectoId, anio, mes);
    }

    @PutMapping("/sctr/{polizaId}/nomina")
    public SomaSctrPolizaDTO guardarNominaSctr(
            @PathVariable Long proyectoId,
            @PathVariable Long polizaId,
            @RequestBody SomaSctrNominaGuardarRequest request) {
        return somaSctrService.guardarNomina(proyectoId, polizaId, request);
    }

    @PostMapping("/sctr/{polizaId}/anular")
    public SomaSctrPolizaDTO anularSctr(@PathVariable Long proyectoId, @PathVariable Long polizaId) {
        return somaSctrService.anular(proyectoId, polizaId);
    }

    @GetMapping("/epps")
    public List<SomaEntregaEppDTO> listarEpps(@PathVariable Long proyectoId) {
        return somaEppService.listar(proyectoId);
    }

    @PostMapping("/epps")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaEntregaEppDTO crearEpp(@PathVariable Long proyectoId, @RequestBody SomaEntregaEppRequest request) {
        return somaEppService.crear(proyectoId, request);
    }

    @PutMapping("/epps/items/{itemId}/estado")
    public SomaEntregaEppDTO cambiarEstadoItemEpp(
            @PathVariable Long proyectoId,
            @PathVariable Long itemId,
            @RequestBody SomaEppItemEstadoRequest request) {
        return somaEppService.cambiarEstadoItem(proyectoId, itemId, request);
    }

    @GetMapping("/epps/materiales")
    public List<SomaEppMaterialDTO> listarMaterialesEpp(@PathVariable Long proyectoId) {
        return somaEppService.listarMateriales(proyectoId);
    }

    @GetMapping(value = "/epps/kardex/{personaId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> kardexEppPdf(@PathVariable Long proyectoId, @PathVariable Long personaId) {
        byte[] pdf = somaEppService.kardexPdf(proyectoId, personaId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("kardex-epp-" + personaId + ".pdf", StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(pdf.length);
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping("/capacitaciones")
    public List<SomaCapacitacionDTO> listarCapacitaciones(@PathVariable Long proyectoId) {
        return somaCapacitacionService.listar(proyectoId);
    }

    @PostMapping("/capacitaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaCapacitacionDTO crearCapacitacion(
            @PathVariable Long proyectoId, @RequestBody SomaCapacitacionRequest request) {
        return somaCapacitacionService.crear(proyectoId, request);
    }

    @GetMapping("/capacitaciones/precargar-cuadrilla")
    public List<SomaCapacitacionAsistenteDTO> precargarCuadrilla(
            @PathVariable Long proyectoId, @RequestParam Long cuadrillaId) {
        return somaCapacitacionService.precargarCuadrilla(proyectoId, cuadrillaId);
    }
}
