package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.RequerimientoObra;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequerimientoObraRepository extends JpaRepository<RequerimientoObra, Long> {

    @EntityGraph(attributePaths = {"items", "items.material", "items.partida"})
    List<RequerimientoObra> findByProyectoIdOrderByNumeroDesc(Long proyectoId);

    @EntityGraph(attributePaths = {"items", "items.material", "items.partida"})
    Optional<RequerimientoObra> findByIdAndProyectoId(Long id, Long proyectoId);

    Optional<RequerimientoObra> findFirstByProyectoIdOrderByNumeroDesc(Long proyectoId);
}
