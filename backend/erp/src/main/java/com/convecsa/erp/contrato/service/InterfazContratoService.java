package com.convecsa.erp.contrato.service;

import com.convecsa.erp.common.web.PagedResponse;
import com.convecsa.erp.contrato.domain.EstadoContrato;
import com.convecsa.erp.contrato.dto.ContratoDTO;
import com.convecsa.erp.contrato.dto.ContratoRequest;
import com.convecsa.erp.contrato.dto.ContratosCatalogosDTO;
import com.convecsa.erp.contrato.dto.ContratosResumeDTO;
import com.convecsa.erp.contrato.dto.PlantillaContratoDTO;
import com.convecsa.erp.contrato.dto.PlantillaContratoDetalleDTO;
import com.convecsa.erp.contrato.dto.PlantillaContratoRequest;
import com.convecsa.erp.contrato.dto.PlantillaContratoUpdateRequest;
import java.util.List;

public interface InterfazContratoService {

    ContratosResumeDTO resume();

    ContratosCatalogosDTO catalogos();

    PagedResponse<ContratoDTO> listar(int page, int size, EstadoContrato estado);

    ContratoDTO crear(ContratoRequest request);

    String documento(Long id);

    List<PlantillaContratoDTO> listarPlantillas();

    PlantillaContratoDTO crearPlantilla(PlantillaContratoRequest request);

    PlantillaContratoDetalleDTO obtenerPlantilla(Long id);

    PlantillaContratoDetalleDTO actualizarPlantilla(Long id, PlantillaContratoUpdateRequest request);

    ContratoDTO obtenerUltimoContrato(Long personaId);
}
