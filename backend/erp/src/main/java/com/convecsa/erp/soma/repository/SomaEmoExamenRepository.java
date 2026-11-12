package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.entity.SomaEmoExamen;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SomaEmoExamenRepository extends JpaRepository<SomaEmoExamen, Long> {

    Optional<SomaEmoExamen> findFirstByPersonaIdOrderByFechaExamenDescIdDesc(Long personaId);

    @Query("""
            SELECT e FROM SomaEmoExamen e
            JOIN FETCH e.persona
            LEFT JOIN FETCH e.documentoObra
            WHERE e.proyecto.id = :proyectoId
            ORDER BY e.fechaExamen DESC, e.id DESC
            """)
    List<SomaEmoExamen> findByProyectoIdOrderByFechaExamenDescIdDesc(@Param("proyectoId") Long proyectoId);

    @Query("""
            SELECT e FROM SomaEmoExamen e
            JOIN FETCH e.persona
            LEFT JOIN FETCH e.proyecto
            LEFT JOIN FETCH e.documentoObra
            WHERE (:personaId IS NULL OR e.persona.id = :personaId)
              AND (:vencenAntesDe IS NULL OR e.fechaVencimiento <= :vencenAntesDe)
            ORDER BY e.fechaExamen DESC, e.id DESC
            """)
    List<SomaEmoExamen> buscarCorporativo(
            @Param("personaId") Long personaId, @Param("vencenAntesDe") LocalDate vencenAntesDe);
}
