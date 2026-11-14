package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.entity.SomaMonitoreo;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SomaMonitoreoRepository extends JpaRepository<SomaMonitoreo, Long> {

    @EntityGraph(attributePaths = {"informeDoc", "proyecto"})
    List<SomaMonitoreo> findByProyectoIdOrderByFechaDescIdDesc(Long proyectoId);

    @EntityGraph(attributePaths = {"informeDoc", "proyecto"})
    List<SomaMonitoreo> findAllByOrderByFechaDescIdDesc();

    @EntityGraph(attributePaths = {"informeDoc", "proyecto"})
    List<SomaMonitoreo> findByProyectoIdInOrderByFechaDescIdDesc(Collection<Long> proyectoIds);
}
