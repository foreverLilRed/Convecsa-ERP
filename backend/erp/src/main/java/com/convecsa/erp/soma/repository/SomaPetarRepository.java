package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.domain.EstadoPetar;
import com.convecsa.erp.soma.domain.TipoTrabajoPetar;
import com.convecsa.erp.soma.entity.SomaPetar;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SomaPetarRepository extends JpaRepository<SomaPetar, Long> {

    @EntityGraph(attributePaths = {
        "partida",
        "cuadrilla",
        "residente",
        "supervisorSsoma",
        "documento",
        "trabajadores",
        "trabajadores.persona"
    })
    List<SomaPetar> findByProyectoIdOrderByFechaDescNumeroDesc(Long proyectoId);

    @EntityGraph(attributePaths = {
        "partida",
        "cuadrilla",
        "residente",
        "supervisorSsoma",
        "documento",
        "trabajadores",
        "trabajadores.persona"
    })
    List<SomaPetar> findByProyectoIdAndFechaOrderByNumeroDesc(Long proyectoId, LocalDate fecha);

    @EntityGraph(attributePaths = {
        "partida",
        "cuadrilla",
        "residente",
        "supervisorSsoma",
        "documento",
        "trabajadores",
        "trabajadores.persona"
    })
    Optional<SomaPetar> findByIdAndProyectoId(Long id, Long proyectoId);

    @Query("select coalesce(max(p.numero), 0) from SomaPetar p where p.proyecto.id = :proyectoId")
    int findMaxNumero(@Param("proyectoId") Long proyectoId);

    List<SomaPetar> findByProyectoIdAndTipoTrabajoAndEstadoIn(
            Long proyectoId, TipoTrabajoPetar tipoTrabajo, Collection<EstadoPetar> estados);
}