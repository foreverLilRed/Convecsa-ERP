package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.ProyectoResponsable;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProyectoResponsableRepository extends JpaRepository<ProyectoResponsable, Long> {

    List<ProyectoResponsable> findByProyectoIdOrderByRolAsc(Long proyectoId);

    void deleteByProyectoId(Long proyectoId);
}
