package com.convecsa.erp.contrato.repository;

import com.convecsa.erp.contrato.entity.PlantillaContrato;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantillaContratoRepository extends JpaRepository<PlantillaContrato, Long> {

    List<PlantillaContrato> findByActivoTrueOrderByIdAsc();

    boolean existsByCodigoIgnoreCase(String codigo);
}
