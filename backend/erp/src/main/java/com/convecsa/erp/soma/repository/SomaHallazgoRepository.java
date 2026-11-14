package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.entity.SomaHallazgo;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SomaHallazgoRepository extends JpaRepository<SomaHallazgo, Long> {

    @EntityGraph(attributePaths = {"inspeccion", "responsable", "evidenciaAntes", "evidenciaDespues", "proyecto"})
    List<SomaHallazgo> findByProyectoIdOrderByFechaDeteccionDescIdDesc(Long proyectoId);

    @EntityGraph(attributePaths = {"inspeccion", "responsable", "evidenciaAntes", "evidenciaDespues", "proyecto"})
    List<SomaHallazgo> findAllByOrderByFechaDeteccionDescIdDesc();

    @EntityGraph(attributePaths = {"inspeccion", "responsable", "evidenciaAntes", "evidenciaDespues", "proyecto"})
    List<SomaHallazgo> findByProyectoIdInOrderByFechaDeteccionDescIdDesc(Collection<Long> proyectoIds);

    @EntityGraph(attributePaths = {"inspeccion", "responsable", "evidenciaAntes", "evidenciaDespues"})
    List<SomaHallazgo> findByProyectoIdAndInspeccionId(Long proyectoId, Long inspeccionId);

    @EntityGraph(attributePaths = {"inspeccion", "responsable", "evidenciaAntes", "evidenciaDespues"})
    Optional<SomaHallazgo> findByIdAndProyectoId(Long id, Long proyectoId);
}
