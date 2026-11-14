package com.convecsa.erp.contrato.repository;

import com.convecsa.erp.contrato.entity.Adenda;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdendaRepository extends JpaRepository<Adenda, Long> {

    long countByContratoId(Long contratoId);

    @EntityGraph(attributePaths = {"contrato", "contrato.persona", "plantillaContrato"})
    Page<Adenda> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"contrato", "contrato.persona", "plantillaContrato"})
    Optional<Adenda> findFirstByContratoIdOrderByIdDesc(Long contratoId);
}
