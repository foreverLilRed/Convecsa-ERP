package com.convecsa.erp.contrato.repository;

import com.convecsa.erp.contrato.catalogo.TipoPlantilla;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoPlantillaRepository extends JpaRepository<TipoPlantilla, Long> {

    List<TipoPlantilla> findByActivoTrueOrderByIdAsc();
}
