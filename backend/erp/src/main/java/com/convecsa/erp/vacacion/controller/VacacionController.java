package com.convecsa.erp.vacacion.controller;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.vacacion.domain.EstadoVacacion;
import com.convecsa.erp.vacacion.domain.TipoVacacion;
import com.convecsa.erp.vacacion.dto.VacacionDTO;
import com.convecsa.erp.vacacion.dto.VacacionRequest;
import com.convecsa.erp.vacacion.dto.VacacionResumenDTO;
import com.convecsa.erp.vacacion.dto.VacacionSaldoDTO;
import com.convecsa.erp.vacacion.service.InterfazVacacionService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestion-humana/vacaciones")
@RequiredArgsConstructor
public class VacacionController {

    private final InterfazVacacionService vacacionService;

    @GetMapping
    public PagedResponse<VacacionDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) TipoVacacion tipo,
            @RequestParam(required = false) EstadoVacacion estado) {
        return vacacionService.listar(page, size, q, tipo, estado);
    }

    @GetMapping("/resumen")
    public VacacionResumenDTO resumen() {
        return vacacionService.resumen();
    }

    @GetMapping("/personas/{personaId}/saldo")
    public VacacionSaldoDTO saldo(
            @PathVariable Long personaId,
            @RequestParam LocalDate periodoInicio,
            @RequestParam LocalDate periodoFin,
            @RequestParam(defaultValue = "30") int diasDerecho) {
        return vacacionService.saldo(personaId, periodoInicio, periodoFin, diasDerecho);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VacacionDTO crear(@Valid @RequestBody VacacionRequest request) {
        return vacacionService.crear(request);
    }

    @PatchMapping("/{id}/aprobar")
    public VacacionDTO aprobar(@PathVariable Long id) {
        return vacacionService.aprobar(id);
    }

    @PatchMapping("/{id}/rechazar")
    public VacacionDTO rechazar(@PathVariable Long id) {
        return vacacionService.rechazar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        vacacionService.eliminar(id);
    }

    @GetMapping(value = "/{id}/documento", produces = MediaType.TEXT_HTML_VALUE)
    public String documento(@PathVariable Long id) {
        return vacacionService.documento(id);
    }
}
