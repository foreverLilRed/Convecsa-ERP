package com.convecsa.erp.nomina.declaracion.repository;

import com.convecsa.erp.nomina.declaracion.entity.DeclaracionMensual;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeclaracionMensualRepository extends JpaRepository<DeclaracionMensual, Long> {

    boolean existsByAnioAndMes(int anio, int mes);

    Optional<DeclaracionMensual> findByAnioAndMes(int anio, int mes);

    Page<DeclaracionMensual> findAllByOrderByAnioDescMesDesc(Pageable pageable);
}
