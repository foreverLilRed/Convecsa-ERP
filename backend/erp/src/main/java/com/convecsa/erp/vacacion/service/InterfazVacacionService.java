package com.convecsa.erp.vacacion.service;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.vacacion.domain.EstadoVacacion;
import com.convecsa.erp.vacacion.domain.TipoVacacion;
import com.convecsa.erp.vacacion.dto.VacacionDTO;
import com.convecsa.erp.vacacion.dto.VacacionRequest;
import com.convecsa.erp.vacacion.dto.VacacionResumenDTO;
import com.convecsa.erp.vacacion.dto.VacacionSaldoDTO;
import java.time.LocalDate;

public interface InterfazVacacionService {

    PagedResponse<VacacionDTO> listar(int page, int size, String q, TipoVacacion tipo, EstadoVacacion estado);

    VacacionResumenDTO resumen();

    VacacionSaldoDTO saldo(Long personaId, LocalDate periodoInicio, LocalDate periodoFin, int diasDerecho);

    VacacionDTO crear(VacacionRequest request);

    VacacionDTO aprobar(Long id);

    VacacionDTO rechazar(Long id);

    void eliminar(Long id);

    String documento(Long id);
}
