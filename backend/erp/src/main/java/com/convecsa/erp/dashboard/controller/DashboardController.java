package com.convecsa.erp.dashboard.controller;

import com.convecsa.erp.dashboard.dto.DashboardAsistenciaDTO;
import com.convecsa.erp.dashboard.dto.DashboardContratosDTO;
import com.convecsa.erp.dashboard.dto.DashboardNominaDTO;
import com.convecsa.erp.dashboard.dto.DashboardPersonasDTO;
import com.convecsa.erp.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/personas")
    public DashboardPersonasDTO personas() {
        return dashboardService.personas();
    }

    @GetMapping("/contratos")
    public DashboardContratosDTO contratos() {
        return dashboardService.contratos();
    }

    @GetMapping("/nomina")
    public DashboardNominaDTO nomina() {
        return dashboardService.nomina();
    }

    @GetMapping("/asistencia")
    public DashboardAsistenciaDTO asistencia() {
        return dashboardService.asistencia();
    }
}
