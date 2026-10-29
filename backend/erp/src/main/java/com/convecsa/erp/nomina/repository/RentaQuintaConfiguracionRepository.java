package com.convecsa.erp.nomina.repository;

import com.convecsa.erp.nomina.entity.RentaQuintaConfiguracion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentaQuintaConfiguracionRepository extends JpaRepository<RentaQuintaConfiguracion, Integer> {

    Optional<RentaQuintaConfiguracion> findFirstByOrderByAnioDesc();
}
