package com.convecsa.erp.contrato.repository;

import com.convecsa.erp.contrato.catalogo.RegimenLaboral;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegimenLaboralRepository extends JpaRepository<RegimenLaboral, Long> {

    List<RegimenLaboral> findByActivoTrueOrderByIdAsc();
}
