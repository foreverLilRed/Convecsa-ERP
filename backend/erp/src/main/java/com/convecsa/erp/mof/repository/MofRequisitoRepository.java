package com.convecsa.erp.mof.repository;

import com.convecsa.erp.mof.entity.MofRequisito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MofRequisitoRepository extends JpaRepository<MofRequisito, Long> {

    List<MofRequisito> findByPuestoIdOrderByOrdenAsc(Long puestoId);
}
