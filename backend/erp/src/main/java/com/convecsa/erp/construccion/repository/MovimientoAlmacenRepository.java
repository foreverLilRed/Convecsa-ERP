package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.domain.TipoMovimientoAlmacen;
import com.convecsa.erp.construccion.entity.MovimientoAlmacen;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoAlmacenRepository extends JpaRepository<MovimientoAlmacen, Long> {

    @EntityGraph(attributePaths = {"material", "partida"})
    List<MovimientoAlmacen> findByAlmacenIdOrderByIdDesc(Long almacenId);

    @EntityGraph(attributePaths = {"material", "partida"})
    List<MovimientoAlmacen> findByAlmacenIdAndMaterialIdOrderByIdDesc(Long almacenId, Long materialId);

    Optional<MovimientoAlmacen> findFirstByAlmacenIdAndMaterialIdOrderByIdDesc(Long almacenId, Long materialId);

    List<MovimientoAlmacen> findByAlmacenIdAndMaterialIdAndTipo(Long almacenId, Long materialId, TipoMovimientoAlmacen tipo);

    List<MovimientoAlmacen> findByValeIdAndTipo(Long valeId, TipoMovimientoAlmacen tipo);

    List<MovimientoAlmacen> findByAlmacenId(Long almacenId);
}
