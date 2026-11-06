package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.ValeSalida;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValeSalidaRepository extends JpaRepository<ValeSalida, Long> {

    @EntityGraph(attributePaths = {"items", "items.material", "partida"})
    List<ValeSalida> findByAlmacenIdOrderByNumeroDesc(Long almacenId);

    @EntityGraph(attributePaths = {"items", "items.material", "partida", "almacen"})
    Optional<ValeSalida> findByIdAndAlmacenProyectoId(Long id, Long proyectoId);

    Optional<ValeSalida> findFirstByAlmacenIdOrderByNumeroDesc(Long almacenId);
}
