package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.CuadrillaMiembro;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CuadrillaMiembroRepository extends JpaRepository<CuadrillaMiembro, Long> {

    @Query("""
            select m from CuadrillaMiembro m
            join fetch m.persona
            join fetch m.cuadrilla
            where m.proyecto.id = :proyectoId
            """)
    List<CuadrillaMiembro> findByProyectoId(@Param("proyectoId") Long proyectoId);

    Optional<CuadrillaMiembro> findByProyectoIdAndPersonaId(Long proyectoId, Long personaId);

    Optional<CuadrillaMiembro> findByCuadrillaIdAndPersonaId(Long cuadrillaId, Long personaId);
}
