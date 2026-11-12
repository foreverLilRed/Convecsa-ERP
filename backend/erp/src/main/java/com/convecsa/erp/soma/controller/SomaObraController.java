package com.convecsa.erp.soma.controller;

import com.convecsa.erp.soma.dto.SomaEmoDTO;
import com.convecsa.erp.soma.dto.SomaEmoRequest;
import com.convecsa.erp.soma.dto.SomaResumenObraDTO;
import com.convecsa.erp.soma.service.InterfazSomaEmoService;
import com.convecsa.erp.soma.service.InterfazSomaObraService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proyectos/{proyectoId:\\d+}/soma")
@RequiredArgsConstructor
public class SomaObraController {

    private final InterfazSomaObraService somaObraService;
    private final InterfazSomaEmoService somaEmoService;

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
}
