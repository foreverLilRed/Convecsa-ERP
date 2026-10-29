package com.convecsa.erp.nomina.declaracion.repository;

import com.convecsa.erp.nomina.declaracion.entity.ReciboHonorario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReciboHonorarioRepository extends JpaRepository<ReciboHonorario, Long> {

    @EntityGraph(attributePaths = {"persona"})
    List<ReciboHonorario> findByDeclaracionIdOrderByIdAsc(Long declaracionId);

    Optional<ReciboHonorario> findByIdAndDeclaracionId(Long id, Long declaracionId);

    long countByDeclaracionId(Long declaracionId);

    long countByDeclaracionIdAndPersonaIsNull(Long declaracionId);

    void deleteByDeclaracionId(Long declaracionId);
}
