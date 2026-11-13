package com.convecsa.erp.soma.dto;

import com.convecsa.erp.soma.domain.MotivoEntregaEpp;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomaEntregaEppRequest {

    private Long personaId;
    private LocalDate fechaEntrega;
    private MotivoEntregaEpp motivo;
    private Boolean firmadoTrabajador;
    private Boolean descontarAlmacen;
    private Long partidaId;
    private String observaciones;
    private List<SomaEntregaEppItemRequest> items;
}
