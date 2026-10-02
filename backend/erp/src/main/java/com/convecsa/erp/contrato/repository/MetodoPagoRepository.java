package com.convecsa.erp.contrato.repository;

import com.convecsa.erp.contrato.catalogo.MetodoPago;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {

    List<MetodoPago> findByActivoTrueOrderByIdAsc();
}
