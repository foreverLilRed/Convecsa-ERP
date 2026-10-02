package com.convecsa.erp.contrato.controller;

import com.convecsa.erp.contrato.dto.ContratosResumeDTO;
import com.convecsa.erp.contrato.service.InterfazContratoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestion-humana/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final InterfazContratoService contratoService;

    @GetMapping("/resume")
    public ContratosResumeDTO resume() {
        return contratoService.resume();
    }
}
