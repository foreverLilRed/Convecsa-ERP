package com.convecsa.erp.obra.service;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.obra.domain.EstadoObra;
import com.convecsa.erp.obra.dto.ObraDTO;
import com.convecsa.erp.obra.dto.ObraEstadoRequest;
import com.convecsa.erp.obra.dto.ObraRequest;
import com.convecsa.erp.obra.dto.ObraResumenDTO;

public interface InterfazObraService {

    PagedResponse<ObraDTO> listar(int page, int size, String q, EstadoObra estado);

    ObraResumenDTO resumen();

    ObraDTO obtener(Long id);

    ObraDTO crear(ObraRequest request);

    ObraDTO actualizar(Long id, ObraRequest request);

    ObraDTO cambiarEstado(Long id, ObraEstadoRequest request);

    void eliminar(Long id);
}
