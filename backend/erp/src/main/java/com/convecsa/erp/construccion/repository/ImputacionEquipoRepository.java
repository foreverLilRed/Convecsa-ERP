package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.ImputacionEquipo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImputacionEquipoRepository extends JpaRepository<ImputacionEquipo, Long> {

    List<ImputacionEquipo> findByProyectoId(Long proyectoId);

    List<ImputacionEquipo> findByAsignacionIdOrderByFechaDescIdDesc(Long asignacionId);

    Optional<ImputacionEquipo> findByIdAndAsignacionIdAndProyectoId(Long id, Long asignacionId, Long proyectoId);
}
