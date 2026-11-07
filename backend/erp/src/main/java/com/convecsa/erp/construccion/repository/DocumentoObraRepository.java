package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.DocumentoObra;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentoObraRepository extends JpaRepository<DocumentoObra, Long> {

    List<DocumentoObra> findByProyectoIdOrderByCreatedAtDescIdDesc(Long proyectoId);

    Optional<DocumentoObra> findByIdAndProyectoId(Long id, Long proyectoId);
}
