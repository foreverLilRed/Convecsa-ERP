package com.convecsa.erp.asistencia.controller;

import com.convecsa.erp.asistencia.dto.AsistenciaConfigDTO;
import com.convecsa.erp.asistencia.dto.AsistenciaConfigRequest;
import com.convecsa.erp.asistencia.dto.AsistenciaConceptoVariableDTO;
import com.convecsa.erp.asistencia.dto.AsistenciaConceptoVariableRequest;
import com.convecsa.erp.asistencia.dto.AsistenciaFeriadoDTO;
import com.convecsa.erp.asistencia.dto.AsistenciaFeriadoRequest;
import com.convecsa.erp.asistencia.dto.AsistenciaGenerarRequest;
import com.convecsa.erp.asistencia.dto.AsistenciaPlanillaDTO;
import com.convecsa.erp.asistencia.dto.AsistenciaPlanillaUpdateRequest;
import com.convecsa.erp.asistencia.service.InterfazAsistenciaService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/gestion-humana/asistencia")
@RequiredArgsConstructor
public class AsistenciaController {

    private final InterfazAsistenciaService asistenciaService;

    @GetMapping("/config")
    public AsistenciaConfigDTO config() {
        return asistenciaService.obtenerConfig();
    }

    @PutMapping("/config")
    public AsistenciaConfigDTO guardarConfig(@Valid @RequestBody AsistenciaConfigRequest request) {
        return asistenciaService.guardarConfig(request);
    }

    @GetMapping("/feriados")
    public List<AsistenciaFeriadoDTO> feriados(@RequestParam int anio) {
        return asistenciaService.listarFeriados(anio);
    }

    @PostMapping("/feriados")
    @ResponseStatus(HttpStatus.CREATED)
    public AsistenciaFeriadoDTO crearFeriado(@Valid @RequestBody AsistenciaFeriadoRequest request) {
        return asistenciaService.crearFeriado(request);
    }

    @DeleteMapping("/feriados/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarFeriado(@PathVariable Long id) {
        asistenciaService.eliminarFeriado(id);
    }

    @GetMapping("/planilla")
    public AsistenciaPlanillaDTO planilla(@RequestParam int anio, @RequestParam int mes) {
        return asistenciaService.obtenerPlanilla(anio, mes);
    }

    @PostMapping("/planilla/generar")
    public AsistenciaPlanillaDTO generar(@Valid @RequestBody AsistenciaGenerarRequest request) {
        return asistenciaService.generarPlanilla(request);
    }

    @PutMapping("/planilla")
    public AsistenciaPlanillaDTO actualizar(@Valid @RequestBody AsistenciaPlanillaUpdateRequest request) {
        return asistenciaService.actualizarPlanilla(request);
    }

    @GetMapping("/conceptos-variables")
    public List<AsistenciaConceptoVariableDTO> conceptosVariables(@RequestParam int anio, @RequestParam int mes) {
        return asistenciaService.listarConceptosVariables(anio, mes);
    }

    @PostMapping("/conceptos-variables")
    @ResponseStatus(HttpStatus.CREATED)
    public AsistenciaConceptoVariableDTO registrarConceptoVariable(
            @RequestParam int anio,
            @RequestParam int mes,
            @Valid @RequestBody AsistenciaConceptoVariableRequest request) {
        return asistenciaService.registrarConceptoVariable(anio, mes, request);
    }

    @DeleteMapping("/conceptos-variables/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarConceptoVariable(@PathVariable Long id) {
        asistenciaService.eliminarConceptoVariable(id);
    }
}
