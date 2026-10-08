package com.convecsa.erp.desempeno.repository;

import com.convecsa.erp.desempeno.entity.EvaluacionDesempeno;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluacionDesempenoRepository extends JpaRepository<EvaluacionDesempeno, Long> {

    Optional<EvaluacionDesempeno> findByPersonaIdAndPeriodo(Long personaId, String periodo);

    @Query("""
            SELECT e FROM EvaluacionDesempeno e
            JOIN FETCH e.persona p
            WHERE p.id IN :ids AND e.periodo = :periodo
            """)
    List<EvaluacionDesempeno> findByPersonaIdInAndPeriodo(
            @Param("ids") Collection<Long> ids, @Param("periodo") String periodo);
}
