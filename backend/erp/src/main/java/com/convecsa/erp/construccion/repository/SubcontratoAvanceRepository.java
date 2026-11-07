package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.SubcontratoAvance;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubcontratoAvanceRepository extends JpaRepository<SubcontratoAvance, Long> {

    List<SubcontratoAvance> findByProyectoId(Long proyectoId);

    List<SubcontratoAvance> findBySubcontratoIdOrderByFechaDescIdDesc(Long subcontratoId);

    Optional<SubcontratoAvance> findByIdAndSubcontratoIdAndProyectoId(Long id, Long subcontratoId, Long proyectoId);
}
