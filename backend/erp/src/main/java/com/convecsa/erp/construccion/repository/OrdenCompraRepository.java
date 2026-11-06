package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.OrdenCompra;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {

    @EntityGraph(attributePaths = {"items", "items.material", "items.requerimientoItem"})
    List<OrdenCompra> findByProyectoIdOrderByNumeroDesc(Long proyectoId);

    @EntityGraph(attributePaths = {"items", "items.material", "items.requerimientoItem"})
    Optional<OrdenCompra> findByIdAndProyectoId(Long id, Long proyectoId);

    Optional<OrdenCompra> findFirstByProyectoIdOrderByNumeroDesc(Long proyectoId);
}
