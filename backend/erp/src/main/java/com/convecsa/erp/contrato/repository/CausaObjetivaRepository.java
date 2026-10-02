package com.convecsa.erp.contrato.repository;

import com.convecsa.erp.contrato.catalogo.CausaObjetiva;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CausaObjetivaRepository extends JpaRepository<CausaObjetiva, Long> {

    List<CausaObjetiva> findByActivoTrueOrderByIdAsc();
}
