package com.convecsa.erp.contrato.controller;

import com.convecsa.erp.contrato.dto.PlantillaContratoDTO;
import com.convecsa.erp.contrato.dto.PlantillaContratoDetalleDTO;
import com.convecsa.erp.contrato.dto.PlantillaContratoRequest;
import com.convecsa.erp.contrato.dto.PlantillaContratoUpdateRequest;
import com.convecsa.erp.contrato.service.InterfazContratoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestion-humana/contratos/plantillas")
@RequiredArgsConstructor
public class PlantillaContratoController {

    private final InterfazContratoService contratoService;

    @GetMapping
    public List<PlantillaContratoDTO> listar() {
        return contratoService.listarPlantillas();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlantillaContratoDTO crear(@Valid @RequestBody PlantillaContratoRequest request) {
        return contratoService.crearPlantilla(request);
    }

    @GetMapping("/{id}")
    public PlantillaContratoDetalleDTO obtener(@PathVariable Long id) {
        return contratoService.obtenerPlantilla(id);
    }

    @PutMapping("/{id}")
    public PlantillaContratoDetalleDTO actualizar(
            @PathVariable Long id, @Valid @RequestBody PlantillaContratoUpdateRequest request) {
        return contratoService.actualizarPlantilla(id, request);
    }
}
