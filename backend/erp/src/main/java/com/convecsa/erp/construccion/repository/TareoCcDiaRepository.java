package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.TareoCcDia;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TareoCcDiaRepository extends JpaRepository<TareoCcDia, Long> {

    List<TareoCcDia> findByPeriodoIdOrderByPersonaIdAscFechaAsc(Long periodoId);

    @EntityGraph(attributePaths = {"persona", "periodo"})
    List<TareoCcDia> findByPeriodoIdIn(List<Long> periodoIds);

    List<TareoCcDia> findByPeriodoIdAndPersonaIdOrderByFechaAsc(Long periodoId, Long personaId);

    Optional<TareoCcDia> findByPeriodoIdAndPersonaIdAndFecha(Long periodoId, Long personaId, LocalDate fecha);

    @Query("""
            select t from TareoCcDia t
            where t.periodo.proyecto.id = :proyectoId
              and t.fecha >= :desde and t.fecha <= :hasta
            """)
    List<TareoCcDia> findByPeriodoProyectoIdAndFechaBetween(
            @Param("proyectoId") Long proyectoId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    List<TareoCcDia> findByFechaBetween(LocalDate desde, LocalDate hasta);

    void deleteByPeriodoId(Long periodoId);
}
