package com.convecsa.erp.construccion.service;

import com.convecsa.erp.construccion.dto.BoletaCcDTO;
import com.convecsa.erp.construccion.dto.BoletaCcResumenDTO;
import com.convecsa.erp.construccion.dto.ConstruccionCatalogosDTO;
import com.convecsa.erp.construccion.dto.PeriodoCcDTO;
import com.convecsa.erp.construccion.dto.PeriodoCcRequest;
import com.convecsa.erp.construccion.dto.PersonalObraDTO;
import com.convecsa.erp.construccion.dto.ProyectoDTO;
import com.convecsa.erp.construccion.dto.ProyectoRequest;
import com.convecsa.erp.construccion.dto.TareoCcGrillaDTO;
import com.convecsa.erp.construccion.dto.TareoCcGuardarRequest;
import java.util.List;

public interface InterfazConstruccionService {

    ConstruccionCatalogosDTO catalogos();

    List<ProyectoDTO> listarProyectos(Boolean activo);

    ProyectoDTO obtenerProyecto(Long id);

    ProyectoDTO crearProyecto(ProyectoRequest request);

    List<PersonalObraDTO> listarPersonal(Long proyectoId);

    List<PeriodoCcDTO> listarPeriodos(Long proyectoId);

    PeriodoCcDTO crearPeriodo(Long proyectoId, PeriodoCcRequest request);

    PeriodoCcDTO obtenerPeriodo(Long proyectoId, Long periodoId);

    PeriodoCcDTO cerrarPeriodo(Long proyectoId, Long periodoId);

    TareoCcGrillaDTO obtenerTareo(Long proyectoId, Long periodoId);

    TareoCcGrillaDTO guardarTareo(Long proyectoId, Long periodoId, TareoCcGuardarRequest request);

    List<BoletaCcResumenDTO> liquidar(Long proyectoId, Long periodoId);

    List<BoletaCcResumenDTO> listarBoletas(Long proyectoId, Long periodoId);

    BoletaCcDTO obtenerBoleta(Long proyectoId, Long periodoId, Long boletaId);

    String documentoBoletaHtml(Long proyectoId, Long periodoId, Long boletaId);

    byte[] documentoBoletaPdf(Long proyectoId, Long periodoId, Long boletaId);
}
