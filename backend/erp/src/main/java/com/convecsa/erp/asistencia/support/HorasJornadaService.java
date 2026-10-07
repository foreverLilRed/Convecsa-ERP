package com.convecsa.erp.asistencia.support;

import com.convecsa.erp.contrato.catalogo.Jornada;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class HorasJornadaService {

    public static final BigDecimal HORAS_DEFAULT = new BigDecimal("8.00");

    public BigDecimal horasEsperadasDia(Jornada jornada, int diasLaborablesSemana) {
        if (jornada != null && jornada.getHorasDiarias() != null) {
            return jornada.getHorasDiarias().setScale(2, RoundingMode.HALF_UP);
        }
        if (jornada != null && jornada.getHorasSemanales() != null) {
            int divisor = Math.max(diasLaborablesSemana, 1);
            return jornada.getHorasSemanales()
                    .divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP);
        }
        return HORAS_DEFAULT;
    }
}
