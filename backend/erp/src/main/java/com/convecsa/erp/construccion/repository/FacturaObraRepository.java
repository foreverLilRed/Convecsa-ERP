package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.FacturaObra;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacturaObraRepository extends JpaRepository<FacturaObra, Long> {

    boolean existsByValorizacionId(Long valorizacionId);

    Optional<FacturaObra> findFirstByProyectoIdOrderByNumeroDesc(Long proyectoId);

    @EntityGraph(attributePaths = {"valorizacion", "cobranzas", "factoring"})
    List<FacturaObra> findByProyectoIdOrderByNumeroDesc(Long proyectoId);

    @EntityGraph(attributePaths = {"valorizacion", "cobranzas", "factoring"})
    Optional<FacturaObra> findByIdAndProyectoId(Long id, Long proyectoId);
}
