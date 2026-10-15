package com.convecsa.erp.asistencia.service;

import com.convecsa.erp.asistencia.dto.AsistenciaConfigDTO;
import com.convecsa.erp.asistencia.dto.AsistenciaConfigRequest;
import com.convecsa.erp.asistencia.dto.AsistenciaConceptoVariableDTO;
import com.convecsa.erp.asistencia.dto.AsistenciaConceptoVariableRequest;
import com.convecsa.erp.asistencia.dto.AsistenciaFeriadoDTO;
import com.convecsa.erp.asistencia.dto.AsistenciaFeriadoRequest;
import com.convecsa.erp.asistencia.dto.AsistenciaGenerarRequest;
import com.convecsa.erp.asistencia.dto.AsistenciaPlanillaDTO;
import com.convecsa.erp.asistencia.dto.AsistenciaPlanillaUpdateRequest;
import java.util.List;

public interface InterfazAsistenciaService {

    AsistenciaConfigDTO obtenerConfig();

    AsistenciaConfigDTO guardarConfig(AsistenciaConfigRequest request);

    List<AsistenciaFeriadoDTO> listarFeriados(int anio);

    AsistenciaFeriadoDTO crearFeriado(AsistenciaFeriadoRequest request);

    void eliminarFeriado(Long id);

    AsistenciaPlanillaDTO obtenerPlanilla(int anio, int mes);

    AsistenciaPlanillaDTO generarPlanilla(AsistenciaGenerarRequest request);

    AsistenciaPlanillaDTO actualizarPlanilla(AsistenciaPlanillaUpdateRequest request);

    List<AsistenciaConceptoVariableDTO> listarConceptosVariables(int anio, int mes);

    AsistenciaConceptoVariableDTO registrarConceptoVariable(
            int anio, int mes, AsistenciaConceptoVariableRequest request);

    void eliminarConceptoVariable(Long id);
}
