package com.convecsa.erp.asistencia.repository;

import com.convecsa.erp.asistencia.entity.AsistenciaFeriado;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsistenciaFeriadoRepository extends JpaRepository<AsistenciaFeriado, Long> {

    boolean existsByFecha(LocalDate fecha);

    List<AsistenciaFeriado> findByFechaBetweenOrderByFechaAsc(LocalDate inicio, LocalDate fin);
}
