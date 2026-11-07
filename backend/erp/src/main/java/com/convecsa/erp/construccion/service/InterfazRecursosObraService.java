package com.convecsa.erp.construccion.service;

import com.convecsa.erp.construccion.dto.AsignacionEquipoDTO;
import com.convecsa.erp.construccion.dto.AsignacionEquipoRequest;
import com.convecsa.erp.construccion.dto.CuadrillaMiembroRequest;
import com.convecsa.erp.construccion.dto.CuadrillaObraDTO;
import com.convecsa.erp.construccion.dto.CuadrillaObraRequest;
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

    List<CuadrillaObraDTO> listarCuadrillas(Long proyectoId);

    CuadrillaObraDTO crearCuadrilla(Long proyectoId, CuadrillaObraRequest request);

    CuadrillaObraDTO actualizarCuadrilla(Long proyectoId, Long cuadrillaId, CuadrillaObraRequest request);

    void eliminarCuadrilla(Long proyectoId, Long cuadrillaId);

    CuadrillaObraDTO agregarMiembro(Long proyectoId, Long cuadrillaId, CuadrillaMiembroRequest request);

    void quitarMiembro(Long proyectoId, Long cuadrillaId, Long personaId);
}
