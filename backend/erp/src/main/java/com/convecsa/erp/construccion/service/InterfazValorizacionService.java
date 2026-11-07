package com.convecsa.erp.construccion.service;

import com.convecsa.erp.construccion.dto.AdicionalObraDTO;
import com.convecsa.erp.construccion.dto.AdicionalObraRequest;
import com.convecsa.erp.construccion.dto.AvanceGuardarRequest;
import com.convecsa.erp.construccion.dto.AvanceObraDTO;
import com.convecsa.erp.construccion.dto.CronogramaGuardarRequest;
import com.convecsa.erp.construccion.dto.CronogramaPartidaDTO;
import com.convecsa.erp.construccion.dto.CurvaSDTO;
import com.convecsa.erp.construccion.dto.ValorizacionCrearRequest;
import com.convecsa.erp.construccion.dto.ValorizacionDTO;
import java.util.List;

public interface InterfazValorizacionService {

    List<CronogramaPartidaDTO> listarCronograma(Long proyectoId);

    List<CronogramaPartidaDTO> guardarCronograma(Long proyectoId, CronogramaGuardarRequest request);

    AvanceObraDTO obtenerAvance(Long proyectoId, int anio, int mes);

    AvanceObraDTO guardarAvance(Long proyectoId, AvanceGuardarRequest request);

    AvanceObraDTO cerrarAvance(Long proyectoId, Long avanceId);

    List<AdicionalObraDTO> listarAdicionales(Long proyectoId);

    AdicionalObraDTO crearAdicional(Long proyectoId, AdicionalObraRequest request);

    AdicionalObraDTO guardarAdicional(Long proyectoId, Long adicionalId, AdicionalObraRequest request);

    AdicionalObraDTO vigentarAdicional(Long proyectoId, Long adicionalId);

    AdicionalObraDTO anularAdicional(Long proyectoId, Long adicionalId);

    List<ValorizacionDTO> listarValorizaciones(Long proyectoId);

    ValorizacionDTO obtenerValorizacion(Long proyectoId, Long valorizacionId);

    ValorizacionDTO crearValorizacion(Long proyectoId, ValorizacionCrearRequest request);

    ValorizacionDTO presentarValorizacion(Long proyectoId, Long valorizacionId);

    ValorizacionDTO observarValorizacion(Long proyectoId, Long valorizacionId);

    ValorizacionDTO aprobarValorizacion(Long proyectoId, Long valorizacionId);

    ValorizacionDTO facturarValorizacion(Long proyectoId, Long valorizacionId);

    CurvaSDTO curvaS(Long proyectoId);
}
