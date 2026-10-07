package com.convecsa.erp.onboarding.repository;

import com.convecsa.erp.onboarding.entity.GeneracionOnboarding;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneracionOnboardingRepository extends JpaRepository<GeneracionOnboarding, Long> {

    @EntityGraph(attributePaths = {"persona"})
    Page<GeneracionOnboarding> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"persona", "contrato", "contrato.jornada", "contrato.metodoPago", "contrato.causaObjetiva"})
    Optional<GeneracionOnboarding> findById(Long id);
}
