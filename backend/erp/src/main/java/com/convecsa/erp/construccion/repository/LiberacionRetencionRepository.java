package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.LiberacionRetencion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiberacionRetencionRepository extends JpaRepository<LiberacionRetencion, Long> {

    List<LiberacionRetencion> findByProyectoIdOrderByFechaDescIdDesc(Long proyectoId);
}
