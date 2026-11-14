package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.entity.SomaComiteMiembro;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SomaComiteMiembroRepository extends JpaRepository<SomaComiteMiembro, Long> {

    @EntityGraph(attributePaths = {"persona"})
    List<SomaComiteMiembro> findByProyectoIdOrderByActivoDescFechaInicioDescIdDesc(Long proyectoId);

    @EntityGraph(attributePaths = {"persona"})
    Optional<SomaComiteMiembro> findByIdAndProyectoId(Long id, Long proyectoId);
}
