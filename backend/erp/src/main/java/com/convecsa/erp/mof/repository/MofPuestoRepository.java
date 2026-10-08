package com.convecsa.erp.mof.repository;

import com.convecsa.erp.mof.entity.MofPuesto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MofPuestoRepository extends JpaRepository<MofPuesto, Long> {

    List<MofPuesto> findAllByOrderByCodigoAsc();
}
