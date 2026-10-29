package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.domain.CodigoConceptoCc;
import com.convecsa.erp.construccion.domain.EstadoPeriodoCc;
import com.convecsa.erp.construccion.entity.BoletaCc;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoletaCcRepository extends JpaRepository<BoletaCc, Long> {

    List<BoletaCc> findByPeriodoIdOrderByIdAsc(Long periodoId);

    @EntityGraph(attributePaths = {"persona", "contrato", "periodo", "periodo.proyecto", "conceptos"})
    List<BoletaCc> findByPeriodoIdIn(List<Long> periodoIds);

    Optional<BoletaCc> findByPeriodoIdAndPersonaId(Long periodoId, Long personaId);

    Optional<BoletaCc> findByIdAndPeriodoId(Long id, Long periodoId);

    void deleteByPeriodoId(Long periodoId);

    @Query("""
            select coalesce(sum(b.totalAfecto), 0)
            from BoletaCc b
            join b.periodo p
            join b.persona per
            where per.numeroDocumento = :dni
              and p.estado = :estado
              and p.fechaHasta between :desde and :hasta
            """)
    BigDecimal sumTotalAfectoCerradoPorDni(
            @Param("dni") String dni,
            @Param("estado") EstadoPeriodoCc estado,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    @Query("""
            select coalesce(sum(c.monto), 0)
            from BoletaCcConcepto c
            join c.boleta b
            join b.periodo p
            join b.persona per
            where per.numeroDocumento = :dni
              and c.codigo = :codigo
              and p.estado = :estado
              and p.fechaHasta between :desde and :hasta
            """)
    BigDecimal sumConceptoCerradoPorDni(
            @Param("dni") String dni,
            @Param("codigo") CodigoConceptoCc codigo,
            @Param("estado") EstadoPeriodoCc estado,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);
}
