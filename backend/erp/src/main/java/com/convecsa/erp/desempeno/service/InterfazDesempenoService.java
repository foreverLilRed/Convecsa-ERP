package com.convecsa.erp.desempeno.service;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.desempeno.dto.ColaboradorDesempenoDTO;
import com.convecsa.erp.desempeno.dto.ColaboradorDesempenoDetalleDTO;
import com.convecsa.erp.desempeno.dto.DesempenoListQuery;
import com.convecsa.erp.desempeno.dto.EvaluacionDesempenoRequest;

public interface InterfazDesempenoService {

    PagedResponse<ColaboradorDesempenoDTO> listarColaboradores(DesempenoListQuery query);

    ColaboradorDesempenoDetalleDTO obtenerColaborador(Long personaId, String periodo);

    ColaboradorDesempenoDetalleDTO guardarEvaluacion(Long personaId, EvaluacionDesempenoRequest request);
}
