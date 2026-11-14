package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.entity.SomaCharlaAts;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SomaCharlaAtsRepository extends JpaRepository<SomaCharlaAts, Long> {

    @EntityGraph(attributePaths = {
        "cuadrilla",
        "partida",
        "expositor",
        "supervisorSsoma",
        "asistentes",
        "asistentes.persona"
    })
    List<SomaCharlaAts> findByProyectoIdOrderByFechaDescHoraInicioDesc(Long proyectoId);

    @EntityGraph(attributePaths = {
        "cuadrilla",
        "partida",
        "expositor",
        "supervisorSsoma",
        "asistentes",
        "asistentes.persona"
    })
    List<SomaCharlaAts> findByProyectoIdAndFechaOrderByHoraInicioDesc(Long proyectoId, LocalDate fecha);

    @EntityGraph(attributePaths = {
        "cuadrilla",
        "partida",
        "expositor",
        "supervisorSsoma",
        "asistentes",
        "asistentes.persona"
    })
    Optional<SomaCharlaAts> findByIdAndProyectoId(Long id, Long proyectoId);
}
