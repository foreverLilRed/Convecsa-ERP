package com.convecsa.erp.soma.controller;

import com.convecsa.erp.soma.dto.SomaEmoDTO;
import com.convecsa.erp.soma.dto.SomaEmoRequest;
import com.convecsa.erp.soma.dto.SomaResumenObraDTO;
import com.convecsa.erp.soma.dto.SomaSctrNominaGuardarRequest;
import com.convecsa.erp.soma.dto.SomaSctrPolizaDTO;
import com.convecsa.erp.soma.dto.SomaSctrPolizaRequest;
import com.convecsa.erp.soma.dto.SomaTrabajadorObraDTO;
import com.convecsa.erp.soma.service.InterfazSomaEmoService;
import com.convecsa.erp.soma.service.InterfazSomaObraService;
import com.convecsa.erp.soma.service.InterfazSomaSctrService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/resumen")
    public SomaResumenObraDTO resumen(@PathVariable Long proyectoId) {
        return somaObraService.obtenerResumen(proyectoId);
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
}
