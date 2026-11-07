package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.CuadrillaObra;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuadrillaObraRepository extends JpaRepository<CuadrillaObra, Long> {

    List<CuadrillaObra> findByProyectoIdOrderByCodigoAsc(Long proyectoId);

    Optional<CuadrillaObra> findByIdAndProyectoId(Long id, Long proyectoId);

    boolean existsByProyectoIdAndCodigoIgnoreCase(Long proyectoId, String codigo);

    boolean existsByProyectoIdAndCodigoIgnoreCaseAndIdNot(Long proyectoId, String codigo, Long id);
}
