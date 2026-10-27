package com.convecsa.erp.obra.controller;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.obra.domain.EstadoObra;
import com.convecsa.erp.obra.dto.ObraDTO;
import com.convecsa.erp.obra.dto.ObraEstadoRequest;
import com.convecsa.erp.obra.dto.ObraRequest;
import com.convecsa.erp.obra.dto.ObraResumenDTO;
import com.convecsa.erp.obra.service.InterfazObraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proyectos/obras")
@RequiredArgsConstructor
public class ObraController {

    private final InterfazObraService obraService;

    @GetMapping
    public PagedResponse<ObraDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EstadoObra estado) {
        return obraService.listar(page, size, q, estado);
    }

    @GetMapping("/resumen")
    public ObraResumenDTO resumen() {
        return obraService.resumen();
    }

    @GetMapping("/{id}")
    public ObraDTO obtener(@PathVariable Long id) {
        return obraService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ObraDTO crear(@Valid @RequestBody ObraRequest request) {
        return obraService.crear(request);
    }

    @PutMapping("/{id}")
    public ObraDTO actualizar(@PathVariable Long id, @Valid @RequestBody ObraRequest request) {
        return obraService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public ObraDTO cambiarEstado(
            @PathVariable Long id, @Valid @RequestBody ObraEstadoRequest request) {
        return obraService.cambiarEstado(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        obraService.eliminar(id);
    }
}
