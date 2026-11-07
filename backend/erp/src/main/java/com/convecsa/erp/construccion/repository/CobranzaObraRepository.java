package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.CobranzaObra;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CobranzaObraRepository extends JpaRepository<CobranzaObra, Long> {

    @EntityGraph(attributePaths = "factura")
    List<CobranzaObra> findByProyectoIdOrderByFechaDescIdDesc(Long proyectoId);

    List<CobranzaObra> findByFacturaIdOrderByFechaAscIdAsc(Long facturaId);

    Optional<CobranzaObra> findByIdAndProyectoId(Long id, Long proyectoId);

    boolean existsByFacturaId(Long facturaId);
}
