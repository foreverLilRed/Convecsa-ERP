package com.convecsa.erp.vacacion.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.convecsa.erp.vacacion.domain.EstadoVacacion;
import com.convecsa.erp.vacacion.domain.TipoVacacion;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional(readOnly = true)
class VacacionSolicitudRepositoryTest {

    @Autowired
    private VacacionSolicitudRepository repository;

    @Test
    void buscarSinFiltrosSeEjecutaSinParametrosNulos() {
        var resultado = repository.buscar(
                false, "",
                false, TipoVacacion.ORDINARIA,
                false, EstadoVacacion.PENDIENTE,
                PageRequest.of(0, 15));

        assertThat(resultado).isNotNull();
    }

    @Test
    void consultaDeCruceDePeriodoSeEjecutaEnPostgresql() {
        assertThat(repository.existeCrucePeriodo(
                -1L,
                List.of(EstadoVacacion.PENDIENTE, EstadoVacacion.APROBADA),
                LocalDate.of(2026, 10, 1), LocalDate.of(2027, 9, 30))).isFalse();
    }
}
