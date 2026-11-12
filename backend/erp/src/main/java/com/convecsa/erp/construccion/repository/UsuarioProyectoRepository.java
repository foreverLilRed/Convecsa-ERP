package com.convecsa.erp.construccion.repository;

import com.convecsa.erp.construccion.entity.UsuarioProyecto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioProyectoRepository extends JpaRepository<UsuarioProyecto, Long> {

    @Query("select up from UsuarioProyecto up join fetch up.usuario where up.proyecto.id = :proyectoId order by up.id")
    List<UsuarioProyecto> findByProyectoIdOrderByIdAsc(@Param("proyectoId") Long proyectoId);

    boolean existsByUsuarioIdAndProyectoId(Long usuarioId, Long proyectoId);

    @Query("select up.proyecto.id from UsuarioProyecto up where up.usuario.id = :usuarioId")
    List<Long> findProyectoIdsByUsuarioId(@Param("usuarioId") Long usuarioId);

    void deleteByProyectoId(Long proyectoId);
}
