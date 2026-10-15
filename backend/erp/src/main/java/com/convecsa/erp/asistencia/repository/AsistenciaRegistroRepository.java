package com.convecsa.erp.asistencia.repository;

import com.convecsa.erp.asistencia.entity.AsistenciaRegistro;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsistenciaRegistroRepository extends JpaRepository<AsistenciaRegistro, Long> {

    @EntityGraph(attributePaths = {"persona", "contrato", "contrato.jornada"})
    List<AsistenciaRegistro> findByFechaBetweenOrderByPersonaApellidosAscPersonaNombresAscFechaAsc(
            LocalDate inicio, LocalDate fin);

    List<AsistenciaRegistro> findByPersonaIdAndFechaBetweenOrderByFechaAsc(Long personaId, LocalDate inicio, LocalDate fin);

    boolean existsByPersonaIdAndFecha(Long personaId, LocalDate fecha);
}
