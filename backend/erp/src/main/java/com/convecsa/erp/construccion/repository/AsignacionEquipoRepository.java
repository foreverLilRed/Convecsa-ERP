package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.AsignacionEquipo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsignacionEquipoRepository extends JpaRepository<AsignacionEquipo, Long> {

    List<AsignacionEquipo> findByProyectoIdOrderByFechaIngresoDescIdDesc(Long proyectoId);

    Optional<AsignacionEquipo> findByIdAndProyectoId(Long id, Long proyectoId);
}
