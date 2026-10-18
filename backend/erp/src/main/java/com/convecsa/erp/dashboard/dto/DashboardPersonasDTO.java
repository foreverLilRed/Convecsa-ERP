package com.convecsa.erp.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardPersonasDTO {

    private final long total;
    private final long activos;
    private final long inactivos;
    private final long nuevosEsteMes;
    private final long examenesMedicosPorVencer;
}
