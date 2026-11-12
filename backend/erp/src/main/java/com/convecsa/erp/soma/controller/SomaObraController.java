package com.convecsa.erp.soma.controller;

import com.convecsa.erp.soma.dto.SomaResumenObraDTO;
import com.convecsa.erp.soma.service.InterfazSomaObraService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proyectos/{proyectoId:\\d+}/soma")
@RequiredArgsConstructor
public class SomaObraController {

    private final InterfazSomaObraService somaObraService;

    @GetMapping("/resumen")
    public SomaResumenObraDTO resumen(@PathVariable Long proyectoId) {
        return somaObraService.obtenerResumen(proyectoId);
    }
}
