package com.convecsa.erp.nomina.repository;

import com.convecsa.erp.nomina.entity.PlantillaBoleta;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantillaBoletaRepository extends JpaRepository<PlantillaBoleta, Long> {

    Optional<PlantillaBoleta> findByCodigoAndActivoTrue(String codigo);
}
