package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.entity.SomaEntregaEppItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SomaEntregaEppItemRepository extends JpaRepository<SomaEntregaEppItem, Long> {

    @EntityGraph(attributePaths = {"entrega", "entrega.persona", "entrega.proyecto", "material"})
    Optional<SomaEntregaEppItem> findByIdAndEntregaProyectoId(Long id, Long proyectoId);
}
