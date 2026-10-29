package com.convecsa.erp.nomina.repository;

import com.convecsa.erp.nomina.entity.RentaQuintaCategoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentaQuintaCategoriaRepository extends JpaRepository<RentaQuintaCategoria, Long> {

    List<RentaQuintaCategoria> findAllByOrderByOrdenAsc();
}
