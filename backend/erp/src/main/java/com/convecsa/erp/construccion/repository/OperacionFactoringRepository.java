package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.OperacionFactoring;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperacionFactoringRepository extends JpaRepository<OperacionFactoring, Long> {

    @EntityGraph(attributePaths = "factura")
    List<OperacionFactoring> findByProyectoIdOrderByFechaCesionDescIdDesc(Long proyectoId);

    @EntityGraph(attributePaths = "factura")
    Optional<OperacionFactoring> findByIdAndProyectoId(Long id, Long proyectoId);

    Optional<OperacionFactoring> findByFacturaId(Long facturaId);

    boolean existsByFacturaIdAndEstadoNot(Long facturaId, com.convecsa.erp.construccion.domain.EstadoFactoring estado);
}
