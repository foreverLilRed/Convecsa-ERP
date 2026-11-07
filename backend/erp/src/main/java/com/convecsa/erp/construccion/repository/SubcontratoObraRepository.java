package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.SubcontratoObra;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubcontratoObraRepository extends JpaRepository<SubcontratoObra, Long> {

    List<SubcontratoObra> findByProyectoIdOrderByIdDesc(Long proyectoId);

    Optional<SubcontratoObra> findByIdAndProyectoId(Long id, Long proyectoId);
}
