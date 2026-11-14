package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.domain.ResultadoPreUsoEquipo;
import com.convecsa.erp.soma.entity.SomaInspeccionEquipo;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SomaInspeccionEquipoRepository extends JpaRepository<SomaInspeccionEquipo, Long> {

    @EntityGraph(attributePaths = {"asignacion", "operador"})
    List<SomaInspeccionEquipo> findByProyectoIdOrderByFechaDescIdDesc(Long proyectoId);

    boolean existsByAsignacionIdAndFechaAndResultado(
            Long asignacionId, LocalDate fecha, ResultadoPreUsoEquipo resultado);

    @EntityGraph(attributePaths = {"asignacion"})
    List<SomaInspeccionEquipo> findByProyectoIdAndFechaAndResultado(
            Long proyectoId, LocalDate fecha, ResultadoPreUsoEquipo resultado);
}
