package com.convecsa.erp.nomina.repository;

import com.convecsa.erp.nomina.entity.PeriodoNomina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PeriodoNominaRepository extends JpaRepository<PeriodoNomina, Long> {

    boolean existsByAnioAndMes(int anio, int mes);

    Optional<PeriodoNomina> findByAnioAndMes(int anio, int mes);

    Page<PeriodoNomina> findAllByOrderByAnioDescMesDesc(Pageable pageable);
}
