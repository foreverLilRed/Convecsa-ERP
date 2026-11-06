package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.RecepcionAlmacen;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecepcionAlmacenRepository extends JpaRepository<RecepcionAlmacen, Long> {

    @EntityGraph(attributePaths = {"items", "items.ordenCompraItem"})
    List<RecepcionAlmacen> findByOrdenCompraIdOrderByIdDesc(Long ordenCompraId);
}
