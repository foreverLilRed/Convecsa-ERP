package com.convecsa.erp.asistencia.dto;

import com.convecsa.erp.asistencia.domain.EstadoAsistencia;
import com.convecsa.erp.asistencia.domain.CodigoTareo;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AsistenciaRegistroDTO {

    private final Long id;
    private final LocalDate fecha;
    private final BigDecimal horasEsperadas;
    private final BigDecimal horasTrabajadas;
    private final EstadoAsistencia estado;
    private final CodigoTareo codigo;
    private final int minutosExtras25;
    private final int minutosExtras35;
    private final int minutosTardanza;
    private final String observacion;
}
