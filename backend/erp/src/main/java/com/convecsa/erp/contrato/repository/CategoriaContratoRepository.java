package com.convecsa.erp.contrato.repository;

import com.convecsa.erp.contrato.catalogo.CategoriaContrato;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaContratoRepository extends JpaRepository<CategoriaContrato, Long> {

    @EntityGraph(attributePaths = "regimenLaboral")
    List<CategoriaContrato> findByActivoTrueOrderByIdAsc();
}
