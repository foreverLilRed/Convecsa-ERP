package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.BitacoraObra;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BitacoraObraRepository extends JpaRepository<BitacoraObra, Long> {

    List<BitacoraObra> findByProyectoIdOrderByFechaDescIdDesc(Long proyectoId);

    Optional<BitacoraObra> findByIdAndProyectoId(Long id, Long proyectoId);
}
