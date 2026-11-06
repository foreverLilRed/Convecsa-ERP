package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.ValeSalidaItem;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValeSalidaItemRepository extends JpaRepository<ValeSalidaItem, Long> {

    @EntityGraph(attributePaths = {"vale", "material"})
    List<ValeSalidaItem> findByValeAlmacenProyectoId(Long proyectoId);
}
