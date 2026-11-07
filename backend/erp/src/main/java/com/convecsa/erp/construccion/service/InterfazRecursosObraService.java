package com.convecsa.erp.construccion.service;

import com.convecsa.erp.construccion.dto.AsignacionEquipoDTO;
import com.convecsa.erp.construccion.dto.AsignacionEquipoRequest;
import com.convecsa.erp.construccion.dto.ImputacionEquipoDTO;
import com.convecsa.erp.construccion.dto.ImputacionEquipoRequest;
import com.convecsa.erp.construccion.dto.SubcontratoAvanceDTO;
import com.convecsa.erp.construccion.dto.SubcontratoAvanceRequest;
import com.convecsa.erp.construccion.dto.SubcontratoObraDTO;
import com.convecsa.erp.construccion.dto.SubcontratoObraRequest;
import java.util.List;

public interface InterfazRecursosObraService {

    List<AsignacionEquipoDTO> listarEquipos(Long proyectoId);

    AsignacionEquipoDTO crearEquipo(Long proyectoId, AsignacionEquipoRequest request);

    AsignacionEquipoDTO actualizarEquipo(Long proyectoId, Long equipoId, AsignacionEquipoRequest request);

    List<ImputacionEquipoDTO> listarImputaciones(Long proyectoId, Long equipoId);

    ImputacionEquipoDTO registrarImputacion(Long proyectoId, Long equipoId, ImputacionEquipoRequest request);

    void eliminarImputacion(Long proyectoId, Long equipoId, Long imputacionId);

    List<SubcontratoObraDTO> listarSubcontratos(Long proyectoId);

    SubcontratoObraDTO crearSubcontrato(Long proyectoId, SubcontratoObraRequest request);

    SubcontratoObraDTO actualizarSubcontrato(Long proyectoId, Long subcontratoId, SubcontratoObraRequest request);

    List<SubcontratoAvanceDTO> listarAvances(Long proyectoId, Long subcontratoId);

    SubcontratoAvanceDTO registrarAvance(Long proyectoId, Long subcontratoId, SubcontratoAvanceRequest request);

    void eliminarAvance(Long proyectoId, Long subcontratoId, Long avanceId);
}
