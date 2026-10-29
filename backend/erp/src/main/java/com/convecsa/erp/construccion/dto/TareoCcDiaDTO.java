package com.convecsa.erp.construccion.dto;

import com.convecsa.erp.construccion.domain.CodigoTareoCc;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareoCcDiaDTO {

    private Long personaId;
    private LocalDate fecha;
    private BigDecimal horasOrd;
    private BigDecimal he60;
    private BigDecimal he100;
    private CodigoTareoCc codigo;
    private boolean altura;
    private boolean altitud;
    private boolean agua;
    private boolean altasTemp;
    private boolean cotaCero;
    private boolean nocturno;
    private boolean aguasServidas;
    private boolean bae;
    private boolean movilidad;
    private int pisos;
}
