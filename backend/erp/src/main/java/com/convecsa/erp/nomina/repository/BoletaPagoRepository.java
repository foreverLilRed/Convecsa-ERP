package com.convecsa.erp.nomina.repository;

import com.convecsa.erp.nomina.entity.BoletaPago;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoletaPagoRepository extends JpaRepository<BoletaPago, Long> {

    @EntityGraph(attributePaths = {"persona", "contrato", "afp"})
    Page<BoletaPago> findByPeriodoIdOrderByPersonaApellidosAscPersonaNombresAsc(Long periodoId, Pageable pageable);

    boolean existsByPeriodoIdAndPersonaId(Long periodoId, Long personaId);

    @EntityGraph(attributePaths = {"persona", "contrato", "afp", "conceptos"})
    List<BoletaPago> findByPeriodoId(Long periodoId);

    long countByPeriodoId(Long periodoId);

    @Query("SELECT COALESCE(SUM(b.neto), 0) FROM BoletaPago b WHERE b.periodo.id = :periodoId")
    BigDecimal sumNetoByPeriodoId(@Param("periodoId") Long periodoId);

    @EntityGraph(attributePaths = {
            "periodo",
            "persona",
            "contrato",
            "afp",
            "conceptos"
    })
    @Query("SELECT b FROM BoletaPago b WHERE b.id = :id")
    Optional<BoletaPago> findWithDetalleById(@Param("id") Long id);

    @EntityGraph(attributePaths = {
            "periodo",
            "persona",
            "contrato",
            "afp",
            "conceptos"
    })
    @Query("""
            SELECT DISTINCT b FROM BoletaPago b
            WHERE b.periodo.id = :periodoId
              AND (:usarFiltro = false OR b.id IN :ids)
            ORDER BY b.persona.apellidos, b.persona.nombres
            """)
    List<BoletaPago> findParaEmision(
            @Param("periodoId") Long periodoId,
            @Param("ids") List<Long> ids,
            @Param("usarFiltro") boolean usarFiltro);
}
