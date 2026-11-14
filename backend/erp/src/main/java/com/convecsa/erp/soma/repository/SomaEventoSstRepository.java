package com.convecsa.erp.soma.repository;

import com.convecsa.erp.soma.entity.SomaEventoSst;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SomaEventoSstRepository extends JpaRepository<SomaEventoSst, Long> {

    @EntityGraph(attributePaths = {
        "partida",
        "personaAfectada",
        "subcontrato",
        "informe",
        "hallazgos",
        "hallazgos.responsable",
        "hallazgos.inspeccion"
    })
    List<SomaEventoSst> findByProyectoIdOrderByFechaHoraDescNumeroDesc(Long proyectoId);

    @EntityGraph(attributePaths = {
        "partida",
        "personaAfectada",
        "subcontrato",
        "informe",
        "hallazgos",
        "hallazgos.responsable",
        "hallazgos.inspeccion"
    })
    Optional<SomaEventoSst> findByIdAndProyectoId(Long id, Long proyectoId);

    @Query("select coalesce(max(e.numero), 0) from SomaEventoSst e where e.proyecto.id = :proyectoId")
    int findMaxNumero(@Param("proyectoId") Long proyectoId);
}
