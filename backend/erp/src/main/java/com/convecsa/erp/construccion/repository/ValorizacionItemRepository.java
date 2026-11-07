package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.ValorizacionItem;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValorizacionItemRepository extends JpaRepository<ValorizacionItem, Long> {

    @EntityGraph(attributePaths = "valorizacion")
    List<ValorizacionItem> findByAdicionalId(Long adicionalId);
}
