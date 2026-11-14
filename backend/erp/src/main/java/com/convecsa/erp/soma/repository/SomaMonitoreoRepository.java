package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.entity.SomaMonitoreo;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SomaMonitoreoRepository extends JpaRepository<SomaMonitoreo, Long> {

    @EntityGraph(attributePaths = {"informeDoc"})
    List<SomaMonitoreo> findByProyectoIdOrderByFechaDescIdDesc(Long proyectoId);
}
