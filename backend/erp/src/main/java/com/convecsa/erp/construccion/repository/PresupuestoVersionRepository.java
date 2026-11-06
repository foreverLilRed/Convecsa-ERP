package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.domain.EstadoVersionPresupuesto;
import com.convecsa.erp.construccion.entity.PresupuestoVersion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresupuestoVersionRepository extends JpaRepository<PresupuestoVersion, Long> {

    List<PresupuestoVersion> findByProyectoIdOrderByNumeroDesc(Long proyectoId);

    Optional<PresupuestoVersion> findByIdAndProyectoId(Long id, Long proyectoId);

    Optional<PresupuestoVersion> findByProyectoIdAndEstado(Long proyectoId, EstadoVersionPresupuesto estado);

    Optional<PresupuestoVersion> findFirstByProyectoIdOrderByNumeroDesc(Long proyectoId);

    boolean existsByProyectoIdAndEstado(Long proyectoId, EstadoVersionPresupuesto estado);
}
