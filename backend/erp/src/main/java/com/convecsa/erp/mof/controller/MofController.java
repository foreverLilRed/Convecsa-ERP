package com.convecsa.erp.mof.controller;

import com.convecsa.erp.mof.dto.MofPuestoDTO;
import com.convecsa.erp.mof.dto.MofPuestoRequest;
import com.convecsa.erp.mof.dto.MofPuestoResumenDTO;
import com.convecsa.erp.mof.service.InterfazMofService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestion-humana/mof")
@RequiredArgsConstructor
public class MofController {

    private final InterfazMofService mofService;

    @GetMapping
    public List<MofPuestoResumenDTO> listar() {
        return mofService.listar();
    }

    @GetMapping(value = "/documento", produces = MediaType.TEXT_HTML_VALUE)
    public String documentoCompleto() {
        return mofService.documentoCompleto();
    }

    @GetMapping("/{id}")
    public MofPuestoDTO obtener(@PathVariable Long id) {
        return mofService.obtener(id);
    }

    @PutMapping("/{id}")
    public MofPuestoDTO guardar(@PathVariable Long id, @Valid @RequestBody MofPuestoRequest request) {
        return mofService.guardar(id, request);
    }

    @GetMapping(value = "/{id}/documento", produces = MediaType.TEXT_HTML_VALUE)
    public String documento(@PathVariable Long id) {
        return mofService.documento(id);
    }
}
