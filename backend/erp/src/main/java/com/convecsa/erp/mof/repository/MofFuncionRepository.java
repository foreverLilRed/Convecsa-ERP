package com.convecsa.erp.mof.repository;

import com.convecsa.erp.mof.entity.MofFuncion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MofFuncionRepository extends JpaRepository<MofFuncion, Long> {

    List<MofFuncion> findByPuestoIdOrderByOrdenAsc(Long puestoId);
}
