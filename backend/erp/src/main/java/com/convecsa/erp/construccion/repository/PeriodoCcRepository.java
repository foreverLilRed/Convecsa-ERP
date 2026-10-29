package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.domain.EstadoPeriodoCc;
import com.convecsa.erp.construccion.entity.PeriodoCc;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeriodoCcRepository extends JpaRepository<PeriodoCc, Long> {

    List<PeriodoCc> findByProyectoIdOrderByFechaDesdeDesc(Long proyectoId);

    Optional<PeriodoCc> findByIdAndProyectoId(Long id, Long proyectoId);

    boolean existsByProyectoIdAndFechaDesdeAndFechaHasta(Long proyectoId, LocalDate fechaDesde, LocalDate fechaHasta);

    List<PeriodoCc> findByFechaHastaBetween(LocalDate desde, LocalDate hasta);

    boolean existsByFechaHastaBetweenAndEstado(LocalDate desde, LocalDate hasta, EstadoPeriodoCc estado);
}
