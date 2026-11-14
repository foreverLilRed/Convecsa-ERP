package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.entity.SomaInspeccion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SomaInspeccionRepository extends JpaRepository<SomaInspeccion, Long> {

    @EntityGraph(attributePaths = {"inspector", "subcontrato"})
    List<SomaInspeccion> findByProyectoIdOrderByFechaDescIdDesc(Long proyectoId);

    @EntityGraph(attributePaths = {"inspector", "subcontrato"})
    Optional<SomaInspeccion> findByIdAndProyectoId(Long id, Long proyectoId);
}
