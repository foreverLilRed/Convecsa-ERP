package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.InventarioCierre;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioCierreRepository extends JpaRepository<InventarioCierre, Long> {

    boolean existsByAlmacenIdAndAnioAndMes(Long almacenId, int anio, int mes);

    @EntityGraph(attributePaths = "material")
    List<InventarioCierre> findByAlmacenIdOrderByAnioDescMesDescIdAsc(Long almacenId);

    @EntityGraph(attributePaths = "material")
    List<InventarioCierre> findByAlmacenIdAndAnioAndMesOrderByIdAsc(Long almacenId, int anio, int mes);
}
