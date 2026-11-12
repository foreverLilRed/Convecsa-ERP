package com.convecsa.erp.soma.controller;

import com.convecsa.erp.soma.dto.SomaEmoDTO;
import com.convecsa.erp.soma.dto.SomaEmoRequest;
import com.convecsa.erp.soma.service.InterfazSomaEmoService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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
}
