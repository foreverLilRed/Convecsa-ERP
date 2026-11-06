package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.PartidaPresupuesto;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidaPresupuestoRepository extends JpaRepository<PartidaPresupuesto, Long> {

    @EntityGraph(attributePaths = "apu")
    List<PartidaPresupuesto> findByVersionIdOrderByOrdenAsc(Long versionId);

    int countByVersionId(Long versionId);

    void deleteByVersionId(Long versionId);
}
