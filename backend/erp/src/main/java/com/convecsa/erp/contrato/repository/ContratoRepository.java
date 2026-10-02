package com.convecsa.erp.contrato.repository;

import com.convecsa.erp.contrato.domain.EstadoContrato;
import com.convecsa.erp.contrato.entity.Contrato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    long countDistinctPersonaIdByEstado(EstadoContrato estado);

    @EntityGraph(attributePaths = "persona")
    Page<Contrato> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
