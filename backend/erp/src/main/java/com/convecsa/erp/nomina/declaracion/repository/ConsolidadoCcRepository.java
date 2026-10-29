package com.convecsa.erp.nomina.declaracion.repository;

import com.convecsa.erp.nomina.declaracion.entity.ConsolidadoCc;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConsolidadoCcRepository extends JpaRepository<ConsolidadoCc, Long> {

    @EntityGraph(attributePaths = {"persona", "proyecto", "conceptos"})
    List<ConsolidadoCc> findByDeclaracionIdOrderByIdAsc(Long declaracionId);

    void deleteByDeclaracionId(Long declaracionId);

    long countByDeclaracionId(Long declaracionId);

    @Query("SELECT COALESCE(SUM(c.neto), 0) FROM ConsolidadoCc c WHERE c.declaracion.id = :declaracionId")
    BigDecimal sumNetoByDeclaracionId(@Param("declaracionId") Long declaracionId);
}
