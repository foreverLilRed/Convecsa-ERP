package com.convecsa.erp.contrato.repository;

import com.convecsa.erp.contrato.domain.EstadoContrato;
import com.convecsa.erp.contrato.entity.Contrato;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    long countDistinctPersonaIdByEstado(EstadoContrato estado);

    @EntityGraph(attributePaths = {"persona", "plantillaContrato", "afp"})
    Page<Contrato> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"persona", "jornada", "metodoPago", "causaObjetiva"})
    Optional<Contrato> findFirstByPersonaIdAndEstadoOrderByCreatedAtDesc(Long personaId, EstadoContrato estado);

    @EntityGraph(attributePaths = {"persona", "persona.cuentasBancarias", "afp", "jornada"})
    @Query("""
            SELECT c FROM Contrato c
            WHERE c.estado = :estado
              AND (c.fechaInicio IS NULL OR c.fechaInicio <= :finMes)
              AND (c.fechaFin IS NULL OR c.fechaFin >= :inicioMes)
            ORDER BY c.createdAt DESC
            """)
    List<Contrato> findVigentesEnRango(
            @Param("estado") EstadoContrato estado,
            @Param("inicioMes") LocalDate inicioMes,
            @Param("finMes") LocalDate finMes);
}
