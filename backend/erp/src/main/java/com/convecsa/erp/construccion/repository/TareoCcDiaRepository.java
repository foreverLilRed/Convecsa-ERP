package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.TareoCcDia;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TareoCcDiaRepository extends JpaRepository<TareoCcDia, Long> {

    List<TareoCcDia> findByPeriodoIdOrderByPersonaIdAscFechaAsc(Long periodoId);

    @EntityGraph(attributePaths = {"persona", "periodo"})
    List<TareoCcDia> findByPeriodoIdIn(List<Long> periodoIds);

    List<TareoCcDia> findByPeriodoIdAndPersonaIdOrderByFechaAsc(Long periodoId, Long personaId);

    Optional<TareoCcDia> findByPeriodoIdAndPersonaIdAndFecha(Long periodoId, Long personaId, LocalDate fecha);

    void deleteByPeriodoId(Long periodoId);
}
