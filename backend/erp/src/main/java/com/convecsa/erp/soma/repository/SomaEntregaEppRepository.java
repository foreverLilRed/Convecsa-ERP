package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.domain.MotivoEntregaEpp;
import com.convecsa.erp.soma.entity.SomaEntregaEpp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SomaEntregaEppRepository extends JpaRepository<SomaEntregaEpp, Long> {

    @EntityGraph(attributePaths = {"persona", "items", "items.material", "valeSalida", "proyecto"})
    List<SomaEntregaEpp> findByProyectoIdOrderByFechaEntregaDescIdDesc(Long proyectoId);

    @EntityGraph(attributePaths = {"persona", "items", "items.material", "valeSalida", "proyecto"})
    List<SomaEntregaEpp> findAllByOrderByFechaEntregaDescIdDesc();

    @EntityGraph(attributePaths = {"persona", "items", "items.material", "valeSalida", "proyecto"})
    List<SomaEntregaEpp> findByProyectoIdInOrderByFechaEntregaDescIdDesc(Collection<Long> proyectoIds);

    @EntityGraph(attributePaths = {"persona", "items", "items.material", "valeSalida"})
    Optional<SomaEntregaEpp> findByIdAndProyectoId(Long id, Long proyectoId);

    @EntityGraph(attributePaths = {"persona", "items", "items.material", "valeSalida"})
    List<SomaEntregaEpp> findByProyectoIdAndPersonaIdOrderByFechaEntregaDescIdDesc(Long proyectoId, Long personaId);

    boolean existsByProyectoIdAndPersonaIdAndMotivo(Long proyectoId, Long personaId, MotivoEntregaEpp motivo);
}
