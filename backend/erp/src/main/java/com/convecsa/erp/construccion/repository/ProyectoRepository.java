package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.Proyecto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    Optional<Proyecto> findByCodigoIgnoreCase(String codigo);

    List<Proyecto> findByActivoTrueOrderByCodigoAsc();

    List<Proyecto> findAllByOrderByCodigoAsc();
}
