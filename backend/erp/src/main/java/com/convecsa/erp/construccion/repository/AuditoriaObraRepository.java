package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.AuditoriaObra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaObraRepository extends JpaRepository<AuditoriaObra, Long> {

    List<AuditoriaObra> findByProyectoIdOrderByCreatedAtDescIdDesc(Long proyectoId);
}
