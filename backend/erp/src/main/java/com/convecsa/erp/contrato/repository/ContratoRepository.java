package com.convecsa.erp.contrato.repository;

import com.convecsa.erp.contrato.entity.Contrato;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    long countByActivoTrue();

    long countByActivoFalse();

    long countByActivoTrueAndFechaFinBetween(LocalDate inicio, LocalDate fin);

    long countByActivoTrueAndFechaFinIsNull();

    @Query("""
            SELECT COUNT(c) FROM Contrato c
            WHERE c.activo = true
              AND (c.fechaInicio IS NULL OR c.fechaInicio <= :fecha)
              AND (c.fechaFin IS NULL OR c.fechaFin >= :fecha)
            """)
    long countVigentesEnFecha(@Param("fecha") LocalDate fecha);

    @Query("""
            SELECT COALESCE(SUM(c.sueldo), 0) FROM Contrato c
            WHERE c.activo = true
              AND (c.fechaInicio IS NULL OR c.fechaInicio <= :finMes)
              AND (c.fechaFin IS NULL OR c.fechaFin >= :inicioMes)
            """)
    BigDecimal sumSueldosVigentesEnRango(
            @Param("inicioMes") LocalDate inicioMes,
            @Param("finMes") LocalDate finMes);

    long countDistinctPersonaIdByActivoTrue();

    @EntityGraph(attributePaths = {"persona", "tipoPlantilla", "categoriaContrato", "categoriaContrato.regimenLaboral"})
    Page<Contrato> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"persona", "tipoPlantilla", "categoriaContrato", "categoriaContrato.regimenLaboral"})
    Page<Contrato> findByActivoOrderByIdDesc(boolean activo, Pageable pageable);

    @EntityGraph(attributePaths = {"persona", "tipoPlantilla", "categoriaContrato", "categoriaContrato.regimenLaboral"})
    Optional<Contrato> findFirstByPersonaIdAndActivoTrueOrderByIdDesc(Long personaId);

    default Optional<Contrato> findUltimoContratoActivoPorPersonaId(Long personaId) {
        return findFirstByPersonaIdAndActivoTrueOrderByIdDesc(personaId);
    }

    List<Contrato> findByPersonaIdInAndActivoTrueOrderByIdDesc(Collection<Long> personaIds);

    @EntityGraph(attributePaths = {"persona", "persona.cuentasBancarias", "tipoPlantilla", "categoriaContrato", "categoriaContrato.regimenLaboral"})
    @Query("""
            SELECT c FROM Contrato c
            WHERE c.activo = true
              AND (c.fechaInicio IS NULL OR c.fechaInicio <= :finMes)
              AND (c.fechaFin IS NULL OR c.fechaFin >= :inicioMes)
            ORDER BY c.id DESC
            """)
    List<Contrato> findVigentesEnRango(
            @Param("inicioMes") LocalDate inicioMes,
            @Param("finMes") LocalDate finMes);
}
