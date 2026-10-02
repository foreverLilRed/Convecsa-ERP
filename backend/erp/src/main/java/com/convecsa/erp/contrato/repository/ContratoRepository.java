package com.convecsa.erp.contrato.repository;

import com.convecsa.erp.contrato.domain.EstadoContrato;
import com.convecsa.erp.contrato.entity.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    long countDistinctPersonaIdByEstado(EstadoContrato estado);
}
