package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.AvanceObra;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvanceObraRepository extends JpaRepository<AvanceObra, Long> {

    @EntityGraph(attributePaths = {"items", "items.partida"})
    Optional<AvanceObra> findByProyectoIdAndAnioAndMes(Long proyectoId, int anio, int mes);

    @EntityGraph(attributePaths = {"items", "items.partida"})
    Optional<AvanceObra> findByIdAndProyectoId(Long id, Long proyectoId);

    @EntityGraph(attributePaths = {"items", "items.partida"})
    List<AvanceObra> findByProyectoIdOrderByAnioAscMesAsc(Long proyectoId);
}
