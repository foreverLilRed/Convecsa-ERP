package com.convecsa.erp.soma.service;

import com.convecsa.erp.soma.domain.TipoTrabajoPetar;
import com.convecsa.erp.soma.dto.SomaPetarChecklistItemDTO;
import com.convecsa.erp.soma.dto.SomaPetarDTO;
import com.convecsa.erp.soma.dto.SomaPetarFaltanteTareoDTO;
import com.convecsa.erp.soma.dto.SomaPetarRequest;
import java.time.LocalDate;
import java.util.List;

public interface InterfazSomaPetarService {

    List<SomaPetarDTO> listar(Long proyectoId, LocalDate fecha);

    SomaPetarDTO crear(Long proyectoId, SomaPetarRequest request);

    SomaPetarDTO actualizar(Long proyectoId, Long petarId, SomaPetarRequest request);

    SomaPetarDTO autorizar(Long proyectoId, Long petarId);

    SomaPetarDTO cerrar(Long proyectoId, Long petarId);

    SomaPetarDTO suspender(Long proyectoId, Long petarId);

    SomaPetarDTO reanudar(Long proyectoId, Long petarId);

    List<SomaPetarChecklistItemDTO> checklist(TipoTrabajoPetar tipo);

    List<SomaPetarFaltanteTareoDTO> faltantesTareo(Long proyectoId);
}
