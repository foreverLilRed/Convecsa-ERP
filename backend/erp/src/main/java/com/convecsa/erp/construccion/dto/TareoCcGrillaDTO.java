package com.convecsa.erp.construccion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TareoCcGrillaDTO {

    private final PeriodoCcDTO periodo;
    private final List<LocalDate> fechas;
    private final List<PersonalObraDTO> personal;
    private final List<TareoCcDiaDTO> dias;
    private final BigDecimal jornadaLv;
    private final BigDecimal jornadaSabado;
}
