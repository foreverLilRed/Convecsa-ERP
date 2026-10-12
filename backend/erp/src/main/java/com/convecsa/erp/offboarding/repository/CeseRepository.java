package com.convecsa.erp.offboarding.repository;

import com.convecsa.erp.offboarding.entity.Cese;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CeseRepository extends JpaRepository<Cese, Long> {

    boolean existsByContratoId(Long contratoId);

    @EntityGraph(attributePaths = {"persona", "contrato"})
    Page<Cese> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"persona", "contrato"})
    @Override
    java.util.Optional<Cese> findById(Long id);
}
