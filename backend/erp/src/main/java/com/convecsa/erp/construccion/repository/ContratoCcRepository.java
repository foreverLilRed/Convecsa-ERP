package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.ContratoCc;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContratoCcRepository extends JpaRepository<ContratoCc, Long> {

    @Query("""
            select cc from ContratoCc cc
            join fetch cc.contrato c
            join fetch c.persona
            left join fetch cc.proyecto
            where cc.proyecto.id = :proyectoId and c.activo = true
            order by c.persona.apellidos, c.persona.nombres
            """)
    List<ContratoCc> findActivosByProyectoId(@Param("proyectoId") Long proyectoId);

    @Query("""
            select cc from ContratoCc cc
            join fetch cc.contrato c
            join fetch c.persona
            where c.id = :contratoId
            """)
    Optional<ContratoCc> findByContratoId(@Param("contratoId") Long contratoId);

    @Query("select cc.contratoId from ContratoCc cc")
    List<Long> findAllContratoIds();
}
