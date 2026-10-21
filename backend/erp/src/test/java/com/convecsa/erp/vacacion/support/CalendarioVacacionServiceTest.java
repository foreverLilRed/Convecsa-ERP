package com.convecsa.erp.vacacion.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CalendarioVacacionServiceTest {

    private final CalendarioVacacionService service = new CalendarioVacacionService();

    @Test
    void calculaDiasCalendarioIncluyendoLaFechaInicial() {
        LocalDate inicio = LocalDate.of(2026, 7, 28);

        assertThat(service.fechaFin(inicio, 10)).isEqualTo(LocalDate.of(2026, 8, 6));
        assertThat(service.fechaRetorno(inicio, 10)).isEqualTo(LocalDate.of(2026, 8, 7));
    }

    @Test
    void rechazaCantidadFueraDelRangoLegalDelFormulario() {
        assertThatThrownBy(() -> service.fechaFin(LocalDate.now(), 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.fechaFin(LocalDate.now(), 31))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
