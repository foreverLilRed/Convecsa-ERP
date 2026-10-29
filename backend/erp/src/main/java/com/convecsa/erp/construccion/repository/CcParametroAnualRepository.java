package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.CcParametroAnual;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CcParametroAnualRepository extends JpaRepository<CcParametroAnual, Long> {

    Optional<CcParametroAnual> findByAnio(int anio);

    Optional<CcParametroAnual> findFirstByOrderByAnioDesc();
}
