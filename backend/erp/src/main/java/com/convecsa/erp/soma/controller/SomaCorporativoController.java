package com.convecsa.erp.soma.controller;

import com.convecsa.erp.nomina.declaracion.dto.ArchivoDescargaDTO;
import com.convecsa.erp.soma.dto.SomaCapacitacionDTO;
import com.convecsa.erp.soma.dto.SomaDashboardDTO;
import com.convecsa.erp.soma.dto.SomaEmoDTO;
import com.convecsa.erp.soma.dto.SomaEmoRequest;
import com.convecsa.erp.soma.dto.SomaEstadisticaAnioDTO;
import com.convecsa.erp.soma.service.InterfazSomaCapacitacionService;
import com.convecsa.erp.soma.service.InterfazSomaDashboardService;
import com.convecsa.erp.soma.service.InterfazSomaEmoService;
import com.convecsa.erp.soma.service.InterfazSomaEstadisticaService;
import com.convecsa.erp.soma.service.InterfazSomaSunafilService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/soma")
@RequiredArgsConstructor
public class SomaCorporativoController {

    private final InterfazSomaEmoService somaEmoService;
    private final InterfazSomaCapacitacionService somaCapacitacionService;
    private final InterfazSomaEstadisticaService somaEstadisticaService;
    private final InterfazSomaDashboardService somaDashboardService;
    private final InterfazSomaSunafilService somaSunafilService;

    @GetMapping("/emos")
    public List<SomaEmoDTO> listarEmos(
            @RequestParam(required = false) Long personaId,
            @RequestParam(required = false) LocalDate vencenAntesDe) {
        return somaEmoService.listarCorporativo(personaId, vencenAntesDe);
    }

    @PostMapping("/emos")
    @ResponseStatus(HttpStatus.CREATED)
    public SomaEmoDTO registrarEmo(@RequestBody SomaEmoRequest request) {
        Long proyectoId = request == null ? null : request.getProyectoId();
        return somaEmoService.registrar(proyectoId, request);
    }

    @GetMapping("/capacitaciones")
    public List<SomaCapacitacionDTO> listarCapacitaciones(@RequestParam Integer anio) {
        return somaCapacitacionService.listarCorporativo(anio);
    }

    @GetMapping("/estadisticas")
    public SomaEstadisticaAnioDTO estadisticas(@RequestParam(required = false) Integer anio) {
        return somaEstadisticaService.corporativo(anio);
    }

    @GetMapping("/dashboard")
    public SomaDashboardDTO dashboard(@RequestParam(required = false) Integer anio) {
        return somaDashboardService.corporativo(anio);
    }

    @GetMapping("/formatos-sunafil/{formato}")
    public ResponseEntity<byte[]> formatoSunafil(
            @PathVariable int formato,
            @RequestParam(required = false) Long proyectoId,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) Integer mes,
            @RequestParam String tipo) {
        return archivo(somaSunafilService.exportar(formato, proyectoId, anio, mes, tipo));
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
