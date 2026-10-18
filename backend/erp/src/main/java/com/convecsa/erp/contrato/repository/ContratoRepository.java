package com.convecsa.erp.contrato.repository;

import com.convecsa.erp.contrato.entity.Contrato;
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

    long countDistinctPersonaIdByActivoTrue();

    @EntityGraph(attributePaths = {"persona", "tipoPlantilla", "categoriaContrato"})
    Page<Contrato> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"persona", "tipoPlantilla", "categoriaContrato"})
    Page<Contrato> findByActivoOrderByIdDesc(boolean activo, Pageable pageable);

    @EntityGraph(attributePaths = {"persona", "tipoPlantilla", "categoriaContrato"})
    Optional<Contrato> findFirstByPersonaIdAndActivoTrueOrderByIdDesc(Long personaId);

    default Optional<Contrato> findUltimoContratoActivoPorPersonaId(Long personaId) {
        return findFirstByPersonaIdAndActivoTrueOrderByIdDesc(personaId);
    }

    List<Contrato> findByPersonaIdInAndActivoTrueOrderByIdDesc(Collection<Long> personaIds);

    @EntityGraph(attributePaths = {"persona", "persona.cuentasBancarias", "tipoPlantilla", "categoriaContrato"})
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
