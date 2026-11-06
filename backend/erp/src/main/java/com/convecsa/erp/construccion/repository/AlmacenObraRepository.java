package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.AlmacenObra;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlmacenObraRepository extends JpaRepository<AlmacenObra, Long> {

    Optional<AlmacenObra> findByProyectoId(Long proyectoId);
}
