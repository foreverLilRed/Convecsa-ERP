package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.entity.SomaResiduoRegistro;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SomaResiduoRegistroRepository extends JpaRepository<SomaResiduoRegistro, Long> {

    @EntityGraph(attributePaths = {"manifiestoDoc"})
    List<SomaResiduoRegistro> findByProyectoIdOrderByFechaDescIdDesc(Long proyectoId);
}
