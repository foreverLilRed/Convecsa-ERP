package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.BoletaCc;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoletaCcRepository extends JpaRepository<BoletaCc, Long> {

    List<BoletaCc> findByPeriodoIdOrderByIdAsc(Long periodoId);

    @EntityGraph(attributePaths = {"persona", "contrato", "periodo", "periodo.proyecto", "conceptos"})
    List<BoletaCc> findByPeriodoIdIn(List<Long> periodoIds);

    Optional<BoletaCc> findByPeriodoIdAndPersonaId(Long periodoId, Long personaId);

    Optional<BoletaCc> findByIdAndPeriodoId(Long id, Long periodoId);

    void deleteByPeriodoId(Long periodoId);
}
