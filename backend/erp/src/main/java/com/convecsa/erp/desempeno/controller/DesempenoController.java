package com.convecsa.erp.desempeno.controller;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.desempeno.dto.ColaboradorDesempenoDTO;
import com.convecsa.erp.desempeno.dto.ColaboradorDesempenoDetalleDTO;
import com.convecsa.erp.desempeno.dto.DesempenoListQuery;
import com.convecsa.erp.desempeno.dto.EvaluacionDesempenoRequest;
import com.convecsa.erp.desempeno.service.InterfazDesempenoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestion-humana/desempeno")
@RequiredArgsConstructor
public class DesempenoController {

    private final InterfazDesempenoService desempenoService;

    @GetMapping("/colaboradores")
    public PagedResponse<ColaboradorDesempenoDTO> listar(@Valid @ModelAttribute DesempenoListQuery query) {
        return desempenoService.listarColaboradores(query);
    }

    @GetMapping("/colaboradores/{personaId}")
    public ColaboradorDesempenoDetalleDTO detalle(
            @PathVariable Long personaId, @RequestParam String periodo) {
        return desempenoService.obtenerColaborador(personaId, periodo);
    }

    @PutMapping("/colaboradores/{personaId}")
    public ColaboradorDesempenoDetalleDTO guardar(
            @PathVariable Long personaId, @Valid @RequestBody EvaluacionDesempenoRequest request) {
        return desempenoService.guardarEvaluacion(personaId, request);
    }
}
